package com.life.inventorylogger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import java.util.List;

public class ContainerSnapshotList extends Container {
    private final List<InventorySnapshot> snapshots;
    
    public ContainerSnapshotList(List<InventorySnapshot> snapshots) {
        this.snapshots = snapshots;
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
    
    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        
        if (playerIn instanceof net.minecraft.entity.player.EntityPlayerMP) {
            net.minecraft.entity.player.EntityPlayerMP playerMP = (net.minecraft.entity.player.EntityPlayerMP) playerIn;
            playerMP.openContainer = playerMP.inventoryContainer;
            if (playerMP.openContainer != null) {
                playerMP.openContainer.detectAndSendChanges();
            }
        }
    }
    
    @Override
    public void detectAndSendChanges() {
        if (this.inventoryItemStacks == null) {
            this.inventoryItemStacks = new java.util.ArrayList<net.minecraft.item.ItemStack>();
        }
    }
    
    public List<InventorySnapshot> getSnapshots() {
        return snapshots;
    }
}

