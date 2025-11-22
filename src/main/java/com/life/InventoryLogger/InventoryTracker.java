package com.life.inventorylogger;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SideOnly(Side.CLIENT)
public class InventoryTracker {
    private static final int TICKS_PER_SNAPSHOT = 300;
    private static final long MAX_AGE_MS = 300000;
    
    private final Map<UUID, List<InventorySnapshot>> playerSnapshots = new ConcurrentHashMap<UUID, List<InventorySnapshot>>();
    private final Map<UUID, Integer> playerTickCounters = new ConcurrentHashMap<UUID, Integer>();
    private final Set<UUID> loadedPlayers = new HashSet<UUID>();
    
    public InventoryTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        Minecraft mc;
        try {
            mc = Minecraft.getMinecraft();
        } catch (NoSuchMethodError e) {
            return;
        } catch (Exception e) {
            return;
        }
        
        if (mc == null || mc.thePlayer == null || event.player != mc.thePlayer) {
            return;
        }
        
        EntityPlayer player = event.player;
        UUID uuid = player.getUniqueID();
        
        if (!loadedPlayers.contains(uuid)) {
            loadPlayerSnapshots(player);
            loadedPlayers.add(uuid);
        }
        
        Integer tickCount = playerTickCounters.get(uuid);
        if (tickCount == null) {
            tickCount = 0;
        }
        tickCount++;
        
        if (tickCount >= TICKS_PER_SNAPSHOT) {
            tickCount = 0;
            takeSnapshot(player);
            cleanupOldSnapshots();
        }
        
        playerTickCounters.put(uuid, tickCount);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerDeath(LivingDeathEvent event) {
        Minecraft mc;
        try {
            mc = Minecraft.getMinecraft();
        } catch (NoSuchMethodError e) {
            return;
        } catch (Exception e) {
            return;
        }
        
        if (mc == null || mc.thePlayer == null || event.entity != mc.thePlayer) {
            return;
        }
        
        if (event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entity;
            World world = player.worldObj;
            if (world != null) {
                InventorySnapshot snapshot = new InventorySnapshot(player);
                InventorySnapshotStorage.saveSnapshot(world, player, snapshot);
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    private void loadPlayerSnapshots(EntityPlayer player) {
        UUID uuid = player.getUniqueID();
        World world = player.worldObj;
        if (world == null) {
            return;
        }
        List<InventorySnapshot> savedSnapshots = InventorySnapshotStorage.loadSnapshots(world, uuid);
        
        if (savedSnapshots != null && !savedSnapshots.isEmpty()) {
            List<InventorySnapshot> currentSnapshots = playerSnapshots.get(uuid);
            if (currentSnapshots == null) {
                currentSnapshots = new ArrayList<InventorySnapshot>();
                playerSnapshots.put(uuid, currentSnapshots);
            }
            
            java.util.Set<Long> existingTimestamps = new java.util.HashSet<Long>();
            for (InventorySnapshot s : currentSnapshots) {
                existingTimestamps.add(s.getTimestamp());
            }
            
            long currentTime = System.currentTimeMillis();
            for (InventorySnapshot snapshot : savedSnapshots) {
                if (currentTime - snapshot.getTimestamp() <= MAX_AGE_MS) {
                    if (!existingTimestamps.contains(snapshot.getTimestamp())) {
                        currentSnapshots.add(snapshot);
                        existingTimestamps.add(snapshot.getTimestamp());
                    }
                }
            }
            
            Collections.sort(currentSnapshots, new Comparator<InventorySnapshot>() {
                @Override
                public int compare(InventorySnapshot a, InventorySnapshot b) {
                    return Long.compare(a.getTimestamp(), b.getTimestamp());
                }
            });
        }
    }
    
    @SideOnly(Side.CLIENT)
    private void takeSnapshot(EntityPlayer player) {
        UUID uuid = player.getUniqueID();
        InventorySnapshot snapshot = new InventorySnapshot(player);
        
        List<InventorySnapshot> snapshots = playerSnapshots.get(uuid);
        if (snapshots == null) {
            snapshots = new ArrayList<InventorySnapshot>();
            playerSnapshots.put(uuid, snapshots);
        }
        
        boolean duplicate = false;
        for (InventorySnapshot existing : snapshots) {
            if (existing.getTimestamp() == snapshot.getTimestamp()) {
                duplicate = true;
                break;
            }
        }
        
        if (!duplicate) {
            snapshots.add(snapshot);
            World world = player.worldObj;
            if (world != null) {
                InventorySnapshotStorage.saveSnapshot(world, player, snapshot);
            }
        }
    }
    
    private void cleanupOldSnapshots() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<UUID, List<InventorySnapshot>> entry : playerSnapshots.entrySet()) {
            List<InventorySnapshot> snapshots = entry.getValue();
            Iterator<InventorySnapshot> iterator = snapshots.iterator();
            
            while (iterator.hasNext()) {
                InventorySnapshot snapshot = iterator.next();
                if (currentTime - snapshot.getTimestamp() > MAX_AGE_MS) {
                    iterator.remove();
                }
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    public List<InventorySnapshot> getRecentSnapshots(EntityPlayer player) {
        UUID uuid = player.getUniqueID();
        List<InventorySnapshot> snapshots = playerSnapshots.get(uuid);
        
        if (snapshots == null || snapshots.isEmpty()) {
            loadPlayerSnapshots(player);
            snapshots = playerSnapshots.get(uuid);
            if (snapshots == null || snapshots.isEmpty()) {
                return new ArrayList<InventorySnapshot>();
            }
        }
        
        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - 300000;
        
        List<InventorySnapshot> recentSnapshots = new ArrayList<InventorySnapshot>();
        for (InventorySnapshot snapshot : snapshots) {
            if (snapshot.getTimestamp() >= fiveMinutesAgo) {
                List<net.minecraft.item.ItemStack> mainInvCopy = new ArrayList<net.minecraft.item.ItemStack>();
                List<net.minecraft.item.ItemStack> armorInvCopy = new ArrayList<net.minecraft.item.ItemStack>();
                
                for (net.minecraft.item.ItemStack stack : snapshot.getMainInventory()) {
                    if (stack != null) {
                        mainInvCopy.add(stack.copy());
                    } else {
                        mainInvCopy.add(null);
                    }
                }
                
                for (net.minecraft.item.ItemStack stack : snapshot.getArmorInventory()) {
                    if (stack != null) {
                        armorInvCopy.add(stack.copy());
                    } else {
                        armorInvCopy.add(null);
                    }
                }
                
                InventorySnapshot snapshotCopy = new InventorySnapshot(
                    snapshot.getTimestamp(),
                    mainInvCopy,
                    armorInvCopy
                );
                recentSnapshots.add(snapshotCopy);
            }
        }
        
        Collections.sort(recentSnapshots, new Comparator<InventorySnapshot>() {
            @Override
            public int compare(InventorySnapshot a, InventorySnapshot b) {
                return Long.compare(b.getTimestamp(), a.getTimestamp());
            }
        });
        
        if (recentSnapshots.size() > 20) {
            return recentSnapshots.subList(0, 20);
        }
        
        return recentSnapshots;
    }
    
    @SideOnly(Side.CLIENT)
    public InventorySnapshot getSnapshotByIndex(EntityPlayer player, int index) {
        List<InventorySnapshot> snapshots = getRecentSnapshots(player);
        if (index >= 0 && index < snapshots.size()) {
            return snapshots.get(index);
        }
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    public InventorySnapshot getSnapshotBeforeTime(EntityPlayer player, long beforeTime) {
        UUID uuid = player.getUniqueID();
        List<InventorySnapshot> snapshots = playerSnapshots.get(uuid);
        
        if (snapshots == null || snapshots.isEmpty()) {
            loadPlayerSnapshots(player);
            snapshots = playerSnapshots.get(uuid);
            if (snapshots == null || snapshots.isEmpty()) {
                return null;
            }
        }
        
        InventorySnapshot latest = null;
        for (InventorySnapshot snapshot : snapshots) {
            if (snapshot.getTimestamp() < beforeTime) {
                if (latest == null || snapshot.getTimestamp() > latest.getTimestamp()) {
                    latest = snapshot;
                }
            }
        }
        
        return latest;
    }
}
