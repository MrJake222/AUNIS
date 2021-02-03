package mrjake.aunis;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.capability.endpoint.ItemEndpointCapability;
import mrjake.aunis.chunkloader.ChunkLoadingCallback;
import mrjake.aunis.command.AunisCommands;
import mrjake.aunis.config.StargateDimensionConfig;
import mrjake.aunis.datafixer.TileNamesFixer;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.gui.AunisGuiHandler;
import mrjake.aunis.integration.OCWrapperInterface;
import mrjake.aunis.integration.ThermalIntegration;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.proxy.IProxy;
import mrjake.aunis.worldgen.AunisWorldGen;
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
import net.minecraftforge.oredict.OreDictionary;

@Mod( modid = Aunis.ModID, name = Aunis.Name, version = Aunis.Version, acceptedMinecraftVersions = Aunis.MCVersion, dependencies = "after:cofhcore@[4.6.0,);after:opencomputers" )
public class Aunis {	
    public static final String ModID = "aunis";
    public static final String Name = "Aunis";
    public static final String Version = "${version}"; // It works only in final builds.
    public static final int DATA_VERSION = 7;

    public static final String MCVersion = "${mcversion}";

    public static final String CLIENT = "mrjake.aunis.proxy.ProxyClient";
    public static final String SERVER = "mrjake.aunis.proxy.ProxyServer";
    
    public static final AunisCreativeTab aunisCreativeTab = new AunisCreativeTab();
    
    @Instance(ModID)
	public static Aunis instance;
    
    @SidedProxy(clientSide = Aunis.CLIENT, serverSide = Aunis.SERVER)
    public static IProxy proxy;
    public static Logger logger;
        
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
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog(); // This is the recommended way of getting a logger
        
        AunisPacketHandler.registerPackets();
        AunisFluids.registerFluids();
        
    	StargateDimensionConfig.load(event.getModConfigurationDirectory());
    	
        proxy.preInit(event);
    }
 
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	GameRegistry.registerWorldGenerator(new AunisWorldGen(), 0);

    	// ThermalExpansion recipes
    	if(Loader.isModLoaded("thermalexpansion"))
    	    ThermalIntegration.registerRecipes();

    	NetworkRegistry.INSTANCE.registerGuiHandler(instance, new AunisGuiHandler());
    	ItemEndpointCapability.register();
		ForgeChunkManager.setForcedChunkLoadingCallback(Aunis.instance, ChunkLoadingCallback.INSTANCE);

        OreDictionary.registerOre("oreNaquadah", AunisBlocks.ORE_NAQUADAH_BLOCK);
        OreDictionary.registerOre("oreNaquadah", AunisBlocks.ORE_NAQUADAH_BLOCK_STONE);
        OreDictionary.registerOre("ingotNaquadah", AunisItems.NAQUADAH_ALLOY_RAW);
        OreDictionary.registerOre("ingotRefinedNaquadah", AunisItems.NAQUADAH_ALLOY);
        OreDictionary.registerOre("blockRefinedNaquadah", AunisBlocks.NAQUADAH_BLOCK);
    	
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

    /**
     * Shorthand for {@code Aunis.logger.info}.
     * Only for temporary logging info.
     */
	public static void info(String string) {
		logger.info(string);
	}
}
