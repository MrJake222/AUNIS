package mrjake.aunis;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import mrjake.aunis.capability.endpoint.ItemEndpointCapability;
import mrjake.aunis.chunkloader.ChunkLoadingCallback;
import mrjake.aunis.command.AunisCommands;
import mrjake.aunis.config.StargateDimensionConfig;
import mrjake.aunis.datafixer.TileNamesFixer;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.gui.AunisGuiHandler;
import mrjake.aunis.integration.OCWrapperInterface;
import mrjake.aunis.integration.ThermalIntegration;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.proxy.IProxy;
import mrjake.aunis.sound.MODSoundCategory;
import mrjake.aunis.worldgen.AunisWorldGen;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod( modid = Aunis.ModID, name = Aunis.Name, version = Aunis.Version, acceptedMinecraftVersions = Aunis.MCVersion, dependencies = "required-after:cofhcore@[4.6.0,);after:opencomputers" )
public class Aunis {	
    public static final String ModID = "aunis";
    public static final String Name = "AUNIS";
    public static final String Version = "1.9.9-beta";
    public static final int DATA_VERSION = 7;

    public static final String MCVersion = "[1.12.2]";
 
    public static final boolean DEBUG = false;
    public static final String CLIENT = "mrjake.aunis.proxy.ProxyClient";
    public static final String SERVER = "mrjake.aunis.proxy.ProxyServer";
    
    public static final AunisCreativeTab aunisCreativeTab = new AunisCreativeTab();
    
    @Instance(ModID)
	public static Aunis instance;
    
    @SidedProxy(clientSide = Aunis.CLIENT, serverSide = Aunis.SERVER)
    public static IProxy proxy;
    public static Logger logger;
    
    public static SoundCategory soundCategory;
    
    // ------------------------------------------------------------------------
    // OpenComputers
    
    private static final String OC_WRAPPER_LOADED = "mrjake.aunis.integration.OCWrapperLoaded";
    private static final String OC_WRAPPER_NOT_LOADED = "mrjake.aunis.integration.OCWrapperNotLoaded";
    
    public static OCWrapperInterface ocWrapper;
    
	// ------------------------------------------------------------------------
    static {
    	FluidRegistry.enableUniversalBucket();
    }
        	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        logger = event.getModLog();
        
        AunisPacketHandler.registerPackets();
        AunisFluids.registerFluids();
        
    	StargateDimensionConfig.load(event.getModConfigurationDirectory());
    	soundCategory = MODSoundCategory.add("Aunis");
    	
        proxy.preInit(event);
    }
 
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	GameRegistry.registerWorldGenerator(new AunisWorldGen(), 0);
    	ThermalIntegration.registerRecipes();
    	NetworkRegistry.INSTANCE.registerGuiHandler(instance, new AunisGuiHandler());
    	ItemEndpointCapability.register();
		ForgeChunkManager.setForcedChunkLoadingCallback(Aunis.instance, ChunkLoadingCallback.INSTANCE);
    	
    	// ----------------------------------------------------------------------------------------------------------------
    	// OpenComputers
    	
    	try {
	    	if (Loader.isModLoaded("opencomputers"))
	    		ocWrapper = (OCWrapperInterface) Class.forName(OC_WRAPPER_LOADED).newInstance();
	    	else
	    		ocWrapper = (OCWrapperInterface) Class.forName(OC_WRAPPER_NOT_LOADED).newInstance();
    	}
    	
    	catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
    		logger.error("Exception loading OpenComputers wrapper");
    		e.printStackTrace();
    	}
    	
    	
    	// ----------------------------------------------------------------------------------------------------------------
    	// Data fixers
    	
		ModFixs modFixs = ((CompoundDataFixer) FMLCommonHandler.instance().getDataFixer()).init(ModID, DATA_VERSION);
		modFixs.registerFix(FixTypes.BLOCK_ENTITY, new TileNamesFixer());
		
    	proxy.init(event);
    }
 
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) throws IOException {    	
    	proxy.postInit(event);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    	AunisCommands.registerCommands(event);
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) throws IOException {    	
    	StargateDimensionConfig.update();
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
