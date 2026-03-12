package com.howlstudio.chestshop;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
public class ShopManager {
    record Listing(String id,String seller,String item,int qty,int price){}
    private final Path dataDir;
    private final Map<String,Listing> listings=new LinkedHashMap<>();
    private int nextId=1;
    public ShopManager(Path d){dataDir=d;try{Files.createDirectories(d);}catch(Exception e){}load();}
    public int getListingCount(){return listings.size();}
    public void save(){try{StringBuilder sb=new StringBuilder();for(var e:listings.entrySet()){Listing l=e.getValue();sb.append(l.id()+"|"+l.seller()+"|"+l.item()+"|"+l.qty()+"|"+l.price()+"\n");}Files.writeString(dataDir.resolve("listings.txt"),sb.toString());}catch(Exception e){}}
    private void load(){try{Path f=dataDir.resolve("listings.txt");if(!Files.exists(f))return;for(String l:Files.readAllLines(f)){String[]p=l.split("\\|",5);if(p.length<5)continue;listings.put(p[0],new Listing(p[0],p[1],p[2],Integer.parseInt(p[3]),Integer.parseInt(p[4])));try{nextId=Math.max(nextId,Integer.parseInt(p[0].substring(1))+1);}catch(Exception e2){}}}catch(Exception e){}}
    public AbstractPlayerCommand getShopCommand(){
        return new AbstractPlayerCommand("shop","List item for sale. /shop <item> <qty> <price>  |  /shop remove <id>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",3);
                if(args.length>0&&args[0].equalsIgnoreCase("remove")){if(args.length<2){playerRef.sendMessage(Message.raw("Usage: /shop remove <id>"));return;}Listing l=listings.get(args[1]);if(l==null){playerRef.sendMessage(Message.raw("[Shop] Listing not found: "+args[1]));return;}if(!l.seller().equalsIgnoreCase(playerRef.getUsername())){playerRef.sendMessage(Message.raw("[Shop] Not your listing."));return;}listings.remove(args[1]);save();playerRef.sendMessage(Message.raw("[Shop] Removed listing: "+args[1]));return;}
                if(args.length<3){playerRef.sendMessage(Message.raw("Usage: /shop <item> <qty> <price>"));return;}
                try{int qty=Integer.parseInt(args[1]);int price=Integer.parseInt(args[2]);String id="L"+nextId++;listings.put(id,new Listing(id,playerRef.getUsername(),args[0],qty,price));save();playerRef.sendMessage(Message.raw("[Shop] Listed §6"+qty+"x "+args[0]+"§r for §e"+price+" coins§r. ID: §7"+id));}catch(Exception e){playerRef.sendMessage(Message.raw("[Shop] Invalid qty/price."));}
            }
        };
    }
    public AbstractPlayerCommand getBrowseCommand(){
        return new AbstractPlayerCommand("shopbrowse","Browse player shop listings. /shopbrowse [item] [page]"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);String filter=args.length>0&&!args[0].isEmpty()?args[0].toLowerCase():null;
                var show=new ArrayList<Listing>();for(Listing l:listings.values())if(filter==null||l.item().toLowerCase().contains(filter))show.add(l);
                if(show.isEmpty()){playerRef.sendMessage(Message.raw("[Shop] No listings"+(filter!=null?" for "+filter:"")+"."));return;}
                playerRef.sendMessage(Message.raw("[Shop] "+show.size()+" listings"+(filter!=null?" for §6"+filter:"")+"§r:"));
                for(int i=0;i<Math.min(10,show.size());i++){Listing l=show.get(i);playerRef.sendMessage(Message.raw("  §7"+l.id()+"§r §6"+l.qty()+"x "+l.item()+"§r → §e"+l.price()+" coins§r ("+l.seller()+")"));}
                if(show.size()>10)playerRef.sendMessage(Message.raw("  §7...and "+(show.size()-10)+" more. Filter by item name to narrow down."));
            }
        };
    }
    public AbstractPlayerCommand getMyShopCommand(){
        return new AbstractPlayerCommand("myshop","View your shop listings. /myshop"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String pname=playerRef.getUsername();var mine=new ArrayList<Listing>();for(Listing l:listings.values())if(l.seller().equalsIgnoreCase(pname))mine.add(l);
                if(mine.isEmpty()){playerRef.sendMessage(Message.raw("[Shop] You have no listings. Use /shop to create one."));return;}
                playerRef.sendMessage(Message.raw("[Shop] Your listings ("+mine.size()+"):"));for(Listing l:mine)playerRef.sendMessage(Message.raw("  §7"+l.id()+"§r "+l.qty()+"x §6"+l.item()+"§r @ §e"+l.price()+"§r coins"));
            }
        };
    }
}
