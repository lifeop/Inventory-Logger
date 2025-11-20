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

@SideOnly(Side.CLIENT)
public class InventorySnapshotStorage {
    private static final String SNAPSHOT_DIR = "inventory_snapshots";
    private static final long MAX_AGE_MS = 604800000;
    
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
        
        try {
            List<InventorySnapshot> snapshots = loadSnapshots(world, player.getUniqueID());
            if (snapshots == null) {
                snapshots = new ArrayList<InventorySnapshot>();
            }
            
            boolean exists = false;
            for (InventorySnapshot s : snapshots) {
                if (s.getTimestamp() == snapshot.getTimestamp()) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                snapshots.add(snapshot);
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
                        NBTTagCompound stackTag = new NBTTagCompound();
                        stack.writeToNBT(stackTag);
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
                        NBTTagCompound stackTag = new NBTTagCompound();
                        stack.writeToNBT(stackTag);
                        armorInv.appendTag(stackTag);
                    } else {
                        armorInv.appendTag(new NBTTagCompound());
                    }
                }
                snapshotTag.setTag("armorInventory", armorInv);
                
                snapshotList.appendTag(snapshotTag);
            }
            
            root.setTag("snapshots", snapshotList);
            
            FileOutputStream fos = new FileOutputStream(snapshotFile);
            net.minecraft.nbt.CompressedStreamTools.writeCompressed(root, fos);
            fos.close();
            
        } catch (IOException e) {
            System.err.println("Failed to save inventory snapshot for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @SideOnly(Side.CLIENT)
    public static List<InventorySnapshot> loadSnapshots(World world, UUID playerUUID) {
        if (world == null || playerUUID == null) {
            return new ArrayList<InventorySnapshot>();
        }
        
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
                        mainInv.add(net.minecraft.item.ItemStack.loadItemStackFromNBT(stackTag));
                    }
                }
                
                List<net.minecraft.item.ItemStack> armorInv = new ArrayList<net.minecraft.item.ItemStack>();
                NBTTagList armorInvList = snapshotTag.getTagList("armorInventory", 10);
                for (int j = 0; j < armorInvList.tagCount(); j++) {
                    NBTTagCompound stackTag = armorInvList.getCompoundTagAt(j);
                    if (stackTag.hasNoTags()) {
                        armorInv.add(null);
                    } else {
                        armorInv.add(net.minecraft.item.ItemStack.loadItemStackFromNBT(stackTag));
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
}
