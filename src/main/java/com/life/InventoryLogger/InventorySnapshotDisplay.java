package com.life.inventorylogger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class InventorySnapshotDisplay implements IInventory {
    private final InventorySnapshot snapshot;
    private final ItemStack[] inventory;
    
    public InventorySnapshotDisplay(InventorySnapshot snapshot) {
        this.snapshot = snapshot;
        this.inventory = new ItemStack[40];
        
        for (int i = 0; i < 40; i++) {
            this.inventory[i] = null;
        }
        
        List<net.minecraft.item.ItemStack> mainInv = snapshot.getMainInventory();
        if (mainInv != null) {
            for (int i = 0; i < 36 && i < mainInv.size(); i++) {
                net.minecraft.item.ItemStack stack = mainInv.get(i);
                if (stack != null) {
                    net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
                    stack.writeToNBT(nbt);
                    this.inventory[i] = net.minecraft.item.ItemStack.loadItemStackFromNBT(nbt);
                } else {
                    this.inventory[i] = null;
                }
            }
        }
        
        List<net.minecraft.item.ItemStack> armorInv = snapshot.getArmorInventory();
        if (armorInv != null) {
            for (int i = 0; i < 4 && i < armorInv.size(); i++) {
                net.minecraft.item.ItemStack stack = armorInv.get(i);
                if (stack != null) {
                    net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
                    stack.writeToNBT(nbt);
                    this.inventory[36 + i] = net.minecraft.item.ItemStack.loadItemStackFromNBT(nbt);
                } else {
                    this.inventory[36 + i] = null;
                }
            }
        }
    }
    
    @Override
    public int getSizeInventory() {
        return 40;
    }
    
    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < inventory.length ? inventory[index] : null;
    }
    
    @Override
    public ItemStack decrStackSize(int index, int count) {
        return null;
    }
    
    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }
    
    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
    }
    
    @Override
    public int getInventoryStackLimit() {
        return 64;
    }
    
    @Override
    public void markDirty() {
    }
    
    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }
    
    @Override
    public void openInventory(EntityPlayer player) {
    }
    
    @Override
    public void closeInventory(EntityPlayer player) {
    }
    
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }
    
    @Override
    public int getField(int id) {
        return 0;
    }
    
    @Override
    public void setField(int id, int value) {
    }
    
    @Override
    public int getFieldCount() {
        return 0;
    }
    
    @Override
    public void clear() {
        // Read only
    }
    
    @Override
    public String getName() {
        return "Last Inventory";
    }
    
    @Override
    public boolean hasCustomName() {
        return true;
    }
    
    @Override
    public IChatComponent getDisplayName() {
        return new ChatComponentText(this.getName());
    }
}

