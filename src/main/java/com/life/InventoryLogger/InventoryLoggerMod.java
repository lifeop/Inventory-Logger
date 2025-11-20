package com.life.inventorylogger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = InventoryLoggerMod.MODID, version = InventoryLoggerMod.VERSION)
public class InventoryLoggerMod
{
    public static final String MODID = "inventorylogger";
    public static final String VERSION = "1.0";
    
    @Mod.Instance(MODID)
    public static InventoryLoggerMod instance;
    
    private InventoryTracker inventoryTracker;
    public static SimpleNetworkWrapper network;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        if (event.getSide() == Side.CLIENT) {
        inventoryTracker = new InventoryTracker();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
            initClient();
        }
    }
    
    @SideOnly(Side.CLIENT)
    private void initClient() {
        try {
            Class<?> keyInputHandlerClass = this.getClass().getClassLoader().loadClass("com.life.inventorylogger.KeyInputHandler");
            java.lang.reflect.Method registerMethod = keyInputHandlerClass.getMethod("register");
            registerMethod.invoke(null);
        } catch (Exception e) {
            System.err.println("[InventoryLogger] ERROR: Failed to register KeyInputHandler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
    }
    
    @SideOnly(Side.CLIENT)
    public static InventoryTracker getInventoryTracker() {
        return instance != null ? instance.inventoryTracker : null;
    }
}
