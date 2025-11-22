package com.life.inventorylogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;


@SideOnly(Side.CLIENT)
public class KeyInputHandler {
    private static final String KEY_DESCRIPTION = "key.inventorylogger.opensnapshot";
    private static final String KEY_CATEGORY = "key.categories.inventory";
    private static final int DEFAULT_KEY = Keyboard.KEY_L;
    
    private static KeyBinding openSnapshotGuiKey;
    private static boolean registered = false;
    private boolean wasKeyDown = false;
    
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        
        openSnapshotGuiKey = new KeyBinding(KEY_DESCRIPTION, DEFAULT_KEY, KEY_CATEGORY);
        ClientRegistry.registerKeyBinding(openSnapshotGuiKey);
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openSnapshotGuiKey == null) {
            return;
        }
        
        boolean isKeyDown = openSnapshotGuiKey.isKeyDown();
        
        if (isKeyDown && !wasKeyDown) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null) {
                wasKeyDown = isKeyDown;
                return;
            }
            
            EntityPlayer player = mc.thePlayer;
            InventoryTracker tracker = InventoryLoggerMod.getInventoryTracker();
            if (tracker != null) {
                List<InventorySnapshot> snapshots = tracker.getRecentSnapshots(player);
                
                if (snapshots == null || snapshots.isEmpty()) {
                    player.addChatMessage(new net.minecraft.util.ChatComponentText(
                        net.minecraft.util.EnumChatFormatting.RED + "No inventory snapshots found. " +
                        "The mod needs to track your inventory for at least 15 seconds before this works."
                    ));
                    wasKeyDown = isKeyDown;
                    return;
                }
                
                mc.displayGuiScreen(new GuiSnapshotList(player, snapshots));
            }
        }
        wasKeyDown = isKeyDown;
    }
}

