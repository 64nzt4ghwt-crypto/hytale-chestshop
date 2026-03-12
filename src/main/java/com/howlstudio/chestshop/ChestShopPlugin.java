package com.howlstudio.chestshop;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
/** ChestShop — Register shop listings with /shop and browse/buy via /shopbrowse. */
public final class ChestShopPlugin extends JavaPlugin {
    private ShopManager mgr;
    public ChestShopPlugin(JavaPluginInit init){super(init);}
    @Override protected void setup(){
        System.out.println("[ChestShop] Loading...");
        mgr=new ShopManager(getDataDirectory());
        CommandManager.get().register(mgr.getShopCommand());
        CommandManager.get().register(mgr.getBrowseCommand());
        CommandManager.get().register(mgr.getMyShopCommand());
        System.out.println("[ChestShop] Ready. "+mgr.getListingCount()+" listings.");
    }
    @Override protected void shutdown(){if(mgr!=null)mgr.save();System.out.println("[ChestShop] Stopped.");}
}
