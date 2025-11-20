package com.life.inventorylogger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.util.List;

public class GuiHandler implements IGuiHandler {
    private static final int GUI_SNAPSHOT_LIST = 0;
    private static final int GUI_LAST_INV = 1;
    
    public static int getSnapshotListGuiId() {
        return GUI_SNAPSHOT_LIST;
    }
    
    public static int getLastInvGuiId() {
        return GUI_LAST_INV;
    }
    
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
    
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        InventoryTracker tracker = InventoryLoggerMod.getInventoryTracker();
        if (tracker == null) {
            return null;
        }
        
        if (ID == GUI_SNAPSHOT_LIST) {
            List<InventorySnapshot> snapshots = tracker.getRecentSnapshots(player);
            return new GuiSnapshotList(player, snapshots);
        } else if (ID == GUI_LAST_INV) {
            InventorySnapshot snapshot = tracker.getSnapshotByIndex(player, x);
            if (snapshot != null) {
                return new GuiLastInv(player, snapshot);
            }
        }
        return null;
    }
}

