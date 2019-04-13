package mrjake.aunis;

import org.apache.logging.log4j.Logger;

import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.proxy.IProxy;
import mrjake.aunis.worldgen.AunisWorldGen;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod( modid = Aunis.ModID, name = Aunis.Name, version = Aunis.Version, acceptedMinecraftVersions = Aunis.MCVersion )
public class Aunis {	
    public static final String ModID = "aunis";
    public static final String Name = "AUNIS";
    public static final String Version = "0.1";
    public static final String MCVersion = "[1.12.2]";
 
    public static final boolean DEBUG = false;
    public static final String CLIENT = "mrjake.aunis.proxy.ClientProxy";
    public static final String SERVER = "mrjake.aunis.proxy.ServerProxy";
    
    public static final AunisCreativeTab aunisCreativeTab = new AunisCreativeTab();
    
    @Instance(ModID)
	public static Aunis instance;
    
    @SidedProxy(clientSide = Aunis.CLIENT, serverSide = Aunis.SERVER)
    public static IProxy proxy;
    public static Logger logger;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        
        AunisPacketHandler.registerPackets();
        
        proxy.preInit(event);
    }
 
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	GameRegistry.registerWorldGenerator(new AunisWorldGen(), 0);
    	
    	proxy.init(event);
    }
 
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	proxy.postInit(event);
    }

    
	public static void log(String msg) {
		if (DEBUG) {
			logger.info(msg);
		}
	}
	
	public static void info(String msg) {
    	logger.info(msg);
    }
}
