package com.life.inventorylogger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerLastInv extends Container {
    private final InventorySnapshotDisplay inventory;
    
    public ContainerLastInv(EntityPlayer player, InventorySnapshot snapshot) {
        this.inventory = new InventorySnapshotDisplay(snapshot);
        
        for (int i = 0; i < 4; ++i) {
            int armorArrayIndex = 3 - i;
            int inventorySlotIndex = 36 + armorArrayIndex;
            this.addSlotToContainer(new SlotReadOnly(inventory, inventorySlotIndex, 8, 8 + i * 18));
        }
        
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                int slotIndex = j + i * 9 + 9;
                this.addSlotToContainer(new SlotReadOnly(inventory, slotIndex, 8 + j * 18, 84 + i * 18));
            }
        }
        
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new SlotReadOnly(inventory, i, 8 + i * 18, 142));
        }
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
    
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return null;
    }
    
    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
        if (slotId < 0) {
            return null;
        }
        
        if (slotId >= 0 && slotId < this.inventorySlots.size()) {
            Slot slot = this.inventorySlots.get(slotId);
            return slot != null ? slot.getStack() : null;
        }
        
        return null;
    }
    
    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return false;
    }
    
    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.inventoryItemStacks != null) {
            this.inventoryItemStacks.clear();
        }
    }
    
    @Override
    public void detectAndSendChanges() {
        if (this.inventoryItemStacks == null) {
            this.inventoryItemStacks = new java.util.ArrayList<ItemStack>();
            for (int i = 0; i < this.inventorySlots.size(); ++i) {
                this.inventoryItemStacks.add(null);
            }
        }
    }
    
    private static class SlotReadOnly extends Slot {
        public SlotReadOnly(InventorySnapshotDisplay inv, int slotIndex, int x, int y) {
            super(inv, slotIndex, x, y);
        }
        
        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
        
        @Override
        public void putStack(ItemStack stack) {
        }
        
        @Override
        public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
        }
        
        @Override
        public ItemStack decrStackSize(int amount) {
            return null;
        }
        
        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }
    }
}

