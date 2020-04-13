package mrjake.aunis.item;

import mrjake.aunis.item.dialer.UniverseDialerItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class AunisItems {
	
	/**
	 * DHD power/control crystal
	 */
	public static final Item CRYSTAL_CONTROL_DHD = ItemHelper.createGenericItem("crystal_control_dhd");
	
	/**
	 * These allow for dialing 8th glyph(cross dimension travel) and show different address spaces
	 */
	public static final Item CRYSTAL_GLYPH_DHD = ItemHelper.createGenericItem("crystal_glyph_dhd");
	public static final Item CRYSTAL_GLYPH_STARGATE = ItemHelper.createGenericItem("crystal_glyph_stargate");
	public static final Item CRYSTAL_GLYPH_MILKYWAY = ItemHelper.createGenericItem("crystal_glyph_milkyway");
	public static final Item CRYSTAL_GLYPH_PEGASUS = ItemHelper.createGenericItem("crystal_glyph_pegasus");
	public static final Item CRYSTAL_GLYPH_UNIVERSE = ItemHelper.createGenericItem("crystal_glyph_universe");
	
	/**
	 * Diffrent Naquadah(main Stargate building material) stages of purity
	 */
	public static final Item NAQUADAH_SHARD = ItemHelper.createGenericItem("naquadah_shard");
	public static final Item NAQUADAH_ALLOY_RAW = ItemHelper.createGenericItem("naquadah_alloy_raw");
	public static final Item NAQUADAH_ALLOY = ItemHelper.createGenericItem("naquadah_alloy");
	
	/**
	 * Crafting items
	 */
	public static final Item CRYSTAL_SEED = ItemHelper.createGenericItem("crystal_fragment");
	public static final Item CRYSTAL_BLUE = ItemHelper.createGenericItem("crystal_blue");
	public static final Item CRYSTAL_RED = ItemHelper.createGenericItem("crystal_red");
	public static final Item CRYSTAL_ENDER = ItemHelper.createGenericItem("crystal_ender");
	public static final Item CRYSTAL_YELLOW = ItemHelper.createGenericItem("crystal_yellow");
	public static final Item CRYSTAL_WHITE = ItemHelper.createGenericItem("crystal_white");
	
	public static final Item CIRCUIT_CONTROL_BASE = ItemHelper.createGenericItem("circuit_control_base");
	public static final Item CIRCUIT_CONTROL_CRYSTAL = ItemHelper.createGenericItem("circuit_control_crystal");
	public static final Item CIRCUIT_CONTROL_NAQUADAH = ItemHelper.createGenericItem("circuit_control_naquadah");
		
	public static final Item STARGATE_RING_FRAGMENT = ItemHelper.createGenericItem("stargate_ring_fragment");
	public static final Item UNIVERSE_RING_FRAGMENT = ItemHelper.createGenericItem("universe_ring_fragment");
	public static final Item TR_RING_FRAGMENT = ItemHelper.createGenericItem("transportrings_ring_fragment");
	public static final Item HOLDER_CRYSTAL = ItemHelper.createGenericItem("holder_crystal");
	
	public static final Item DHD_BRB = ItemHelper.createGenericItem("dhd_brb");
	
	public static final PageNotebookItem PAGE_NOTEBOOK_ITEM = new PageNotebookItem();
	public static final PageMysteriousItem PAGE_MYSTERIOUS_ITEM = new PageMysteriousItem();
	public static final UniverseDialerItem UNIVERSE_DIALER = new UniverseDialerItem();
	
	public static final Item BEAMER_CRYSTAL_POWER = ItemHelper.createGenericItem("beamer_crystal_power");
	public static final Item BEAMER_CRYSTAL_FLUID = ItemHelper.createGenericItem("beamer_crystal_fluid");
	public static final Item BEAMER_CRYSTAL_ITEMS = ItemHelper.createGenericItem("beamer_crystal_items");
	
	private static Item[] items = {		
		CRYSTAL_CONTROL_DHD,
		
		CRYSTAL_GLYPH_DHD,
		CRYSTAL_GLYPH_STARGATE,
		CRYSTAL_GLYPH_MILKYWAY,
		CRYSTAL_GLYPH_PEGASUS,
		CRYSTAL_GLYPH_UNIVERSE,
				
		NAQUADAH_SHARD,
		NAQUADAH_ALLOY,
		NAQUADAH_ALLOY_RAW,
		
		CRYSTAL_SEED,
		CRYSTAL_BLUE,
		CRYSTAL_RED,
		CRYSTAL_ENDER,
		CRYSTAL_YELLOW,
		CRYSTAL_WHITE,
		
		CIRCUIT_CONTROL_BASE,
		CIRCUIT_CONTROL_CRYSTAL,
		CIRCUIT_CONTROL_NAQUADAH,
		
		STARGATE_RING_FRAGMENT,
		UNIVERSE_RING_FRAGMENT,
		TR_RING_FRAGMENT,
		HOLDER_CRYSTAL,
		
		DHD_BRB,
		PAGE_MYSTERIOUS_ITEM,
		UNIVERSE_DIALER,
		
		BEAMER_CRYSTAL_POWER,
		BEAMER_CRYSTAL_FLUID,
		BEAMER_CRYSTAL_ITEMS
	};
	
	@SubscribeEvent
	public static void onRegisterItems(Register<Item> event) {	
		for (Item item : items) {
			event.getRegistry().register(item);
		}
		
		event.getRegistry().register(PAGE_NOTEBOOK_ITEM);
	}
	
	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {		
		for (Item item : items) {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
		
		ModelLoader.setCustomModelResourceLocation(PAGE_NOTEBOOK_ITEM, 0, new ModelResourceLocation(PAGE_NOTEBOOK_ITEM.getRegistryName() + "_empty", "inventory"));
		ModelLoader.setCustomModelResourceLocation(PAGE_NOTEBOOK_ITEM, 1, new ModelResourceLocation(PAGE_NOTEBOOK_ITEM.getRegistryName() + "_filled", "inventory"));

	}
}
