package com.life.inventorylogger;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSnapshotList extends GuiScreen {
    private final EntityPlayer player;
    private final List<InventorySnapshot> snapshots;
    private final List<GuiButton> snapshotButtons;
    private int scrollOffset = 0;
    private static final int BUTTONS_PER_PAGE = 10;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_X_OFFSET = 10;
    private int scrollbarHeight = 220;
    private boolean isDraggingScrollbar = false;
    
    public GuiSnapshotList(EntityPlayer player, List<InventorySnapshot> snapshots) {
        this.player = player;
        this.snapshots = snapshots != null ? snapshots : new ArrayList<InventorySnapshot>();
        this.snapshotButtons = new ArrayList<GuiButton>();
    }
    
    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.snapshotButtons.clear();
        
        int buttonWidth = 200;
        int buttonHeight = 20;
        int startX = (this.width - buttonWidth) / 2;
        int startY = 50;
        int spacing = 22;
        
        for (int i = 0; i < snapshots.size() && i < 20; i++) {
            InventorySnapshot snapshot = snapshots.get(i);
            String timeText = formatTimestamp(snapshot.getTimestamp());
            GuiButton button = new GuiButton(i, startX, startY + (i * spacing), buttonWidth, buttonHeight, timeText);
            this.buttonList.add(button);
            this.snapshotButtons.add(button);
        }
        
        updateButtonPositions();
        
        int closeButtonY = startY + (Math.min(snapshots.size(), BUTTONS_PER_PAGE) * spacing) + 10;
        this.buttonList.add(new GuiButton(200, startX, closeButtonY, buttonWidth, 20, "Close"));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= 0 && button.id < snapshots.size()) {
            InventorySnapshot selectedSnapshot = snapshots.get(button.id);
            
            if (selectedSnapshot != null) {
                List<net.minecraft.item.ItemStack> mainInvCopy = new ArrayList<net.minecraft.item.ItemStack>();
                List<net.minecraft.item.ItemStack> armorInvCopy = new ArrayList<net.minecraft.item.ItemStack>();
                
                for (net.minecraft.item.ItemStack stack : selectedSnapshot.getMainInventory()) {
                    if (stack != null) {
                        net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
                        stack.writeToNBT(nbt);
                        mainInvCopy.add(net.minecraft.item.ItemStack.loadItemStackFromNBT(nbt));
                    } else {
                        mainInvCopy.add(null);
                    }
                }
                
                for (net.minecraft.item.ItemStack stack : selectedSnapshot.getArmorInventory()) {
                    if (stack != null) {
                        net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
                        stack.writeToNBT(nbt);
                        armorInvCopy.add(net.minecraft.item.ItemStack.loadItemStackFromNBT(nbt));
                    } else {
                        armorInvCopy.add(null);
                    }
                }
                
                InventorySnapshot displaySnapshot = new InventorySnapshot(
                    selectedSnapshot.getTimestamp(),
                    mainInvCopy,
                    armorInvCopy
                );
                
                this.mc.displayGuiScreen(new GuiLastInv(player, displaySnapshot));
            }
        } else if (button.id == 200) {
            this.mc.displayGuiScreen(null);
        }
    }
    
    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0 && snapshots.size() > BUTTONS_PER_PAGE) {
            if (wheel > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else {
                scrollOffset = Math.min(snapshots.size() - BUTTONS_PER_PAGE, scrollOffset + 1);
            }
            updateButtonPositions();
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (snapshots.size() > BUTTONS_PER_PAGE) {
            int buttonWidth = 200;
            int startX = (this.width - buttonWidth) / 2;
            int scrollbarX = startX + buttonWidth + SCROLLBAR_X_OFFSET;
            int scrollbarStartY = 50;
            int scrollbarEndY = scrollbarStartY + scrollbarHeight;
            
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH &&
                mouseY >= scrollbarStartY && mouseY <= scrollbarEndY && mouseButton == 0) {
                isDraggingScrollbar = true;
            }
        }
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        isDraggingScrollbar = false;
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        
        if (isDraggingScrollbar && snapshots.size() > BUTTONS_PER_PAGE) {
            int buttonWidth = 200;
            int startX = (this.width - buttonWidth) / 2;
            int scrollbarX = startX + buttonWidth + SCROLLBAR_X_OFFSET;
            int scrollbarStartY = 50;
            int scrollbarEndY = scrollbarStartY + scrollbarHeight;
            
            if (mouseY >= scrollbarStartY && mouseY <= scrollbarEndY) {
                float scrollRatio = (mouseY - scrollbarStartY) / (float) scrollbarHeight;
                int maxScroll = Math.max(0, snapshots.size() - BUTTONS_PER_PAGE);
                scrollOffset = (int) (scrollRatio * maxScroll);
                scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
                updateButtonPositions();
            }
        }
    }
    
    private void updateButtonPositions() {
        int startY = 50;
        int spacing = 22;
        
        int maxScroll = Math.max(0, snapshots.size() - BUTTONS_PER_PAGE);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        
        for (int i = 0; i < snapshotButtons.size(); i++) {
            GuiButton button = snapshotButtons.get(i);
            if (i >= scrollOffset && i < scrollOffset + BUTTONS_PER_PAGE) {
                button.yPosition = startY + ((i - scrollOffset) * spacing);
                button.visible = true;
            } else {
                button.visible = false;
            }
        }
        
        GuiButton closeButton = null;
        for (GuiButton btn : this.buttonList) {
            if (btn.id == 200) {
                closeButton = btn;
                break;
            }
        }
        if (closeButton != null) {
            closeButton.yPosition = startY + (Math.min(snapshots.size(), BUTTONS_PER_PAGE) * spacing) + 10;
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        String title = "Select Inventory Snapshot";
        int titleWidth = this.fontRendererObj.getStringWidth(title);
        this.fontRendererObj.drawString(title, (this.width - titleWidth) / 2, 20, 16777215);
        
        String countText = snapshots.size() + " snapshots available";
        int countWidth = this.fontRendererObj.getStringWidth(countText);
        this.fontRendererObj.drawString(countText, (this.width - countWidth) / 2, 35, 8421504);
        
        if (snapshots.size() > BUTTONS_PER_PAGE) {
            drawScrollbar(mouseX, mouseY);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawScrollbar(int mouseX, int mouseY) {
        int buttonWidth = 200;
        int startX = (this.width - buttonWidth) / 2;
        int scrollbarX = startX + buttonWidth + SCROLLBAR_X_OFFSET;
        int scrollbarStartY = 50;
        int scrollbarEndY = scrollbarStartY + scrollbarHeight;
        
        drawRect(scrollbarX, scrollbarStartY, scrollbarX + SCROLLBAR_WIDTH, scrollbarEndY, 0xFF333333);
        
        int maxScroll = Math.max(1, snapshots.size() - BUTTONS_PER_PAGE);
        float visibleRatio = (float) BUTTONS_PER_PAGE / snapshots.size();
        int thumbHeight = Math.max(20, (int) (scrollbarHeight * visibleRatio));
        float scrollRatio = maxScroll > 0 ? scrollOffset / (float) maxScroll : 0;
        int thumbY = scrollbarStartY + (int) ((scrollbarHeight - thumbHeight) * scrollRatio);
        
        boolean isHovering = mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH &&
                            mouseY >= thumbY && mouseY <= thumbY + thumbHeight;
        int thumbColor = isHovering ? 0xFFAAAAAA : 0xFF777777;
        drawRect(scrollbarX + 1, thumbY, scrollbarX + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight, thumbColor);
        
        drawRect(scrollbarX, scrollbarStartY, scrollbarX + SCROLLBAR_WIDTH, scrollbarStartY + 1, 0xFF000000);
        drawRect(scrollbarX, scrollbarEndY - 1, scrollbarX + SCROLLBAR_WIDTH, scrollbarEndY, 0xFF000000);
        drawRect(scrollbarX, scrollbarStartY, scrollbarX + 1, scrollbarEndY, 0xFF000000);
        drawRect(scrollbarX + SCROLLBAR_WIDTH - 1, scrollbarStartY, scrollbarX + SCROLLBAR_WIDTH, scrollbarEndY, 0xFF000000);
    }
    
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy - HH:mm:ss");
        return formatter.format(new Date(timestamp));
    }
}

