package com.life.inventorylogger;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiLastInv extends GuiScreen {
    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("textures/gui/container/inventory.png");
    private final EntityPlayer player;
    private final InventorySnapshot snapshot;
    private int xSize = 176;
    private int ySize = 166;
    
    public GuiLastInv(EntityPlayer player, InventorySnapshot snapshot) {
        this.player = player;
        this.snapshot = snapshot;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(INVENTORY_TEXTURE);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0);
        
        List<ItemStack> mainInv = snapshot.getMainInventory();
        List<ItemStack> armorInv = snapshot.getArmorInventory();
        
        for (int i = 0; i < 4; ++i) {
            int armorArrayIndex = 3 - i;
            if (armorArrayIndex < armorInv.size()) {
                ItemStack stack = armorInv.get(armorArrayIndex);
                if (stack != null) {
                    this.itemRender.renderItemAndEffectIntoGUI(stack, 8, 8 + i * 18);
                    this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, stack, 8, 8 + i * 18, null);
                }
            }
        }
        
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                int slotIndex = j + i * 9 + 9;
                if (slotIndex < mainInv.size()) {
                    ItemStack stack = mainInv.get(slotIndex);
                    if (stack != null) {
                        this.itemRender.renderItemAndEffectIntoGUI(stack, 8 + j * 18, 84 + i * 18);
                        this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, stack, 8 + j * 18, 84 + i * 18, null);
                    }
                }
            }
        }
        
        for (int i = 0; i < 9; ++i) {
            if (i < mainInv.size()) {
                ItemStack stack = mainInv.get(i);
                if (stack != null) {
                    this.itemRender.renderItemAndEffectIntoGUI(stack, 8 + i * 18, 142);
                    this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, stack, 8 + i * 18, 142, null);
                }
            }
        }
        
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        
        ItemStack hoveredStack = getHoveredItem(mouseX, mouseY, guiLeft, guiTop);
        if (hoveredStack != null) {
            this.renderToolTip(hoveredStack, mouseX, mouseY);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private ItemStack getHoveredItem(int mouseX, int mouseY, int guiLeft, int guiTop) {
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;
        
        List<ItemStack> mainInv = snapshot.getMainInventory();
        List<ItemStack> armorInv = snapshot.getArmorInventory();
        
        for (int i = 0; i < 4; ++i) {
            int slotX = 8;
            int slotY = 8 + i * 18;
            if (relX >= slotX && relX < slotX + 16 && relY >= slotY && relY < slotY + 16) {
                int armorArrayIndex = 3 - i;
                if (armorArrayIndex < armorInv.size()) {
                    ItemStack stack = armorInv.get(armorArrayIndex);
                    if (stack != null) {
                        return stack;
                    }
                }
                return null;
            }
        }
        
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                int slotX = 8 + j * 18;
                int slotY = 84 + i * 18;
                if (relX >= slotX && relX < slotX + 16 && relY >= slotY && relY < slotY + 16) {
                    int slotIndex = j + i * 9 + 9;
                    if (slotIndex < mainInv.size()) {
                        ItemStack stack = mainInv.get(slotIndex);
                        if (stack != null) {
                            return stack;
                        }
                    }
                    return null;
                }
            }
        }
        
        for (int i = 0; i < 9; ++i) {
            int slotX = 8 + i * 18;
            int slotY = 142;
            if (relX >= slotX && relX < slotX + 16 && relY >= slotY && relY < slotY + 16) {
                if (i < mainInv.size()) {
                    ItemStack stack = mainInv.get(i);
                    if (stack != null) {
                        return stack;
                    }
                }
                return null;
            }
        }
        
        return null;
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 || (this.mc.gameSettings.keyBindInventory != null && this.mc.gameSettings.keyBindInventory.getKeyCode() == keyCode)) {
            this.closeGui();
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            
            if (mouseX < guiLeft || mouseX >= guiLeft + this.xSize ||
                mouseY < guiTop || mouseY >= guiTop + this.ySize) {
                this.closeGui();
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    private void closeGui() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }
    
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

