package com.life.inventorylogger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventorySnapshot {
    private final long timestamp;
    private final List<ItemStack> mainInventory;
    private final List<ItemStack> armorInventory;
    private final ItemStack offhand;
    
    public InventorySnapshot(EntityPlayer player) {
        this.timestamp = System.currentTimeMillis();
        this.mainInventory = new ArrayList<ItemStack>();
        this.armorInventory = new ArrayList<ItemStack>();
        
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null) {
                this.mainInventory.add(stack.copy());
            } else {
                this.mainInventory.add(null);
            }
        }
        
        for (int i = 0; i < player.inventory.armorInventory.length; i++) {
            ItemStack stack = player.inventory.armorInventory[i];
            if (stack != null) {
                this.armorInventory.add(stack.copy());
            } else {
                this.armorInventory.add(null);
            }
        }
        
        this.offhand = null;
    }
    
    public InventorySnapshot(long timestamp, List<ItemStack> mainInventory, List<ItemStack> armorInventory) {
        this.timestamp = timestamp;
        this.mainInventory = new ArrayList<ItemStack>();
        this.armorInventory = new ArrayList<ItemStack>();
        
        if (mainInventory != null) {
            for (ItemStack stack : mainInventory) {
                if (stack != null) {
                    this.mainInventory.add(stack.copy());
                } else {
                    this.mainInventory.add(null);
                }
            }
        }
        
        if (armorInventory != null) {
            for (ItemStack stack : armorInventory) {
                if (stack != null) {
                    this.armorInventory.add(stack.copy());
                } else {
                    this.armorInventory.add(null);
                }
            }
        }
        
        this.offhand = null;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public List<ItemStack> getMainInventory() {
        return mainInventory;
    }
    
    public List<ItemStack> getArmorInventory() {
        return armorInventory;
    }
    
    public ItemStack getOffhand() {
        return offhand;
    }
}

