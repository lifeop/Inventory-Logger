package com.life.inventorylogger;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@SideOnly(Side.CLIENT)
public class InventorySnapshotStorage {
    private static final String SNAPSHOT_DIR = "inventory_snapshots";
    private static final long MAX_AGE_MS = 604800000;
    private static final ExecutorService fileIOExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "InventoryLogger-FileIO");
            t.setDaemon(true);
            return t;
        }
    });
    
    @SideOnly(Side.CLIENT)
    private static File getSnapshotDirectory() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.mcDataDir == null) {
            return null;
        }
        
        File snapshotDir = new File(mc.mcDataDir, SNAPSHOT_DIR);
        if (!snapshotDir.exists()) {
            snapshotDir.mkdirs();
        }
        return snapshotDir;
    }
    
    @SideOnly(Side.CLIENT)
    private static File getPlayerSnapshotFile(UUID playerUUID) {
        File snapshotDir = getSnapshotDirectory();
        if (snapshotDir == null) {
            return null;
        }
        return new File(snapshotDir, playerUUID.toString() + ".dat");
    }
    
    @SideOnly(Side.CLIENT)
    public static void saveSnapshot(World world, EntityPlayer player, InventorySnapshot snapshot) {
        if (world == null || player == null || snapshot == null) {
            return;
        }
        
        File snapshotFile = getPlayerSnapshotFile(player.getUniqueID());
        if (snapshotFile == null) {
            return;
        }
        
        final UUID playerUUID = player.getUniqueID();
        final String playerName = player.getName();
        final File finalSnapshotFile = snapshotFile;
        final InventorySnapshot finalSnapshot = snapshot;
        fileIOExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<InventorySnapshot> snapshots = loadSnapshotsInternal(playerUUID);
                    if (snapshots == null) {
                        snapshots = new ArrayList<InventorySnapshot>();
                    }
                    
                    boolean exists = false;
                    for (InventorySnapshot s : snapshots) {
                        if (s.getTimestamp() == finalSnapshot.getTimestamp()) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (!exists) {
                        snapshots.add(finalSnapshot);
                    }
                    
                    long currentTime = System.currentTimeMillis();
                    List<InventorySnapshot> validSnapshots = new ArrayList<InventorySnapshot>();
                    for (InventorySnapshot s : snapshots) {
                        if (currentTime - s.getTimestamp() <= MAX_AGE_MS) {
                            validSnapshots.add(s);
                        }
                    }
                    
                    java.util.Collections.sort(validSnapshots, new java.util.Comparator<InventorySnapshot>() {
                        @Override
                        public int compare(InventorySnapshot a, InventorySnapshot b) {
                            return Long.compare(a.getTimestamp(), b.getTimestamp());
                        }
                    });
                    
                    NBTTagCompound root = new NBTTagCompound();
                    NBTTagList snapshotList = new NBTTagList();
                    
                    for (InventorySnapshot s : validSnapshots) {
                        NBTTagCompound snapshotTag = new NBTTagCompound();
                        snapshotTag.setLong("timestamp", s.getTimestamp());
                        
                        NBTTagList mainInv = new NBTTagList();
                        for (int i = 0; i < s.getMainInventory().size(); i++) {
                            net.minecraft.item.ItemStack stack = s.getMainInventory().get(i);
                            if (stack != null) {
                                // Only save enchantments and basic item info (item name is determined by item ID)
                                NBTTagCompound stackTag = serializeItemStackMinimal(stack);
                                mainInv.appendTag(stackTag);
                            } else {
                                mainInv.appendTag(new NBTTagCompound());
                            }
                        }
                        snapshotTag.setTag("mainInventory", mainInv);
                        
                        NBTTagList armorInv = new NBTTagList();
                        for (int i = 0; i < s.getArmorInventory().size(); i++) {
                            net.minecraft.item.ItemStack stack = s.getArmorInventory().get(i);
                            if (stack != null) {
                                // Only save enchantments and basic item info (item name is determined by item ID)
                                NBTTagCompound stackTag = serializeItemStackMinimal(stack);
                                armorInv.appendTag(stackTag);
                            } else {
                                armorInv.appendTag(new NBTTagCompound());
                            }
                        }
                        snapshotTag.setTag("armorInventory", armorInv);
                        
                        snapshotList.appendTag(snapshotTag);
                    }
                    
                    root.setTag("snapshots", snapshotList);
                    
                    FileOutputStream fos = new FileOutputStream(finalSnapshotFile);
                    net.minecraft.nbt.CompressedStreamTools.writeCompressed(root, fos);
                    fos.close();
                    
                } catch (IOException e) {
                    System.err.println("Failed to save inventory snapshot for player " + playerName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    @SideOnly(Side.CLIENT)
    public static List<InventorySnapshot> loadSnapshots(World world, UUID playerUUID) {
        if (world == null || playerUUID == null) {
            return new ArrayList<InventorySnapshot>();
        }
        return loadSnapshotsInternal(playerUUID);
    }
    
    @SideOnly(Side.CLIENT)
    private static List<InventorySnapshot> loadSnapshotsInternal(UUID playerUUID) {
        File snapshotFile = getPlayerSnapshotFile(playerUUID);
        if (snapshotFile == null || !snapshotFile.exists()) {
            return new ArrayList<InventorySnapshot>();
        }
        
        try {
            FileInputStream fis = new FileInputStream(snapshotFile);
            NBTTagCompound root = net.minecraft.nbt.CompressedStreamTools.readCompressed(fis);
            fis.close();
            
            List<InventorySnapshot> snapshots = new ArrayList<InventorySnapshot>();
            NBTTagList snapshotList = root.getTagList("snapshots", 10);
            
            for (int i = 0; i < snapshotList.tagCount(); i++) {
                NBTTagCompound snapshotTag = snapshotList.getCompoundTagAt(i);
                long timestamp = snapshotTag.getLong("timestamp");
                
                List<net.minecraft.item.ItemStack> mainInv = new ArrayList<net.minecraft.item.ItemStack>();
                NBTTagList mainInvList = snapshotTag.getTagList("mainInventory", 10);
                for (int j = 0; j < mainInvList.tagCount(); j++) {
                    NBTTagCompound stackTag = mainInvList.getCompoundTagAt(j);
                    if (stackTag.hasNoTags()) {
                        mainInv.add(null);
                    } else {
                        // Load only enchantments and basic item info (item name is determined by item ID)
                        mainInv.add(deserializeItemStackMinimal(stackTag));
                    }
                }
                
                List<net.minecraft.item.ItemStack> armorInv = new ArrayList<net.minecraft.item.ItemStack>();
                NBTTagList armorInvList = snapshotTag.getTagList("armorInventory", 10);
                for (int j = 0; j < armorInvList.tagCount(); j++) {
                    NBTTagCompound stackTag = armorInvList.getCompoundTagAt(j);
                    if (stackTag.hasNoTags()) {
                        armorInv.add(null);
                    } else {
                        // Load only enchantments and basic item info (item name is determined by item ID)
                        armorInv.add(deserializeItemStackMinimal(stackTag));
                    }
                }
                
                InventorySnapshot snapshot = new InventorySnapshot(timestamp, mainInv, armorInv);
                snapshots.add(snapshot);
            }
            
            return snapshots;
            
        } catch (IOException e) {
            System.err.println("Failed to load inventory snapshots for player " + playerUUID + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<InventorySnapshot>();
        }
    }
    
    /**
     * Serialize an ItemStack to NBT with only enchantments and basic item info.
     * Item name is determined by item ID, so it doesn't need to be stored.
     */
    @SideOnly(Side.CLIENT)
    private static NBTTagCompound serializeItemStackMinimal(net.minecraft.item.ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return new NBTTagCompound();
        }
        
        NBTTagCompound tag = new NBTTagCompound();
        
        // Store basic item info (item ID determines the item name)
        tag.setShort("id", (short) net.minecraft.item.Item.getIdFromItem(stack.getItem()));
        tag.setByte("Count", (byte) stack.stackSize);
        tag.setShort("Damage", (short) stack.getItemDamage());
        
        // Get the stack's NBT compound (create if it doesn't exist)
        NBTTagCompound stackNBT = stack.getTagCompound();
        if (stackNBT != null) {
            // Copy only enchantments if they exist
            if (stackNBT.hasKey("ench", 9)) {
                NBTTagList enchants = stackNBT.getTagList("ench", 10);
                tag.setTag("ench", enchants.copy());
            }
        }
        
        return tag;
    }
    
    /**
     * Deserialize an ItemStack from NBT with only enchantments and basic item info.
     * Item name is determined by the item ID.
     */
    @SideOnly(Side.CLIENT)
    private static net.minecraft.item.ItemStack deserializeItemStackMinimal(NBTTagCompound tag) {
        if (tag == null || tag.hasNoTags()) {
            return null;
        }
        
        // Load basic item info
        short id = tag.getShort("id");
        byte count = tag.getByte("Count");
        short damage = tag.getShort("Damage");
        
        // Check if item ID is valid
        net.minecraft.item.Item item = net.minecraft.item.Item.getItemById(id);
        if (item == null) {
            return null;
        }
        
        net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(item, count, damage);
        
        // Create NBT compound for the stack
        NBTTagCompound stackNBT = new NBTTagCompound();
        
        // Copy only enchantments if they exist
        if (tag.hasKey("ench", 9)) {
            NBTTagList enchants = tag.getTagList("ench", 10);
            stackNBT.setTag("ench", enchants.copy());
        }
        
        // Set the NBT compound on the stack
        if (!stackNBT.hasNoTags()) {
            stack.setTagCompound(stackNBT);
        }
        
        return stack;
    }
}
