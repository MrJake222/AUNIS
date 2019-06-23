package mrjake.aunis.item;

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
	 * Provides basic analyzing info
	 */
	public static Item analyzerAncient = ItemHelper.createGenericItem("analyzer_ancient");
	
	/**
	 * DHD power/control crystal
	 */
	public static CrystalControlDHDItem crystalControlDhd = new CrystalControlDHDItem();
	
	/**
	 * These allow for dialing 8th glyph(cross dimension travel)
	 */
	public static Item crystalGlyphDhd = ItemHelper.createGenericItem("crystal_glyph_dhd");
//	public static Item crystalGlyphEmpty = ItemHelper.createGenericItem("crystal_glyph_empty");
	public static Item crystalGlyphStargate = ItemHelper.createGenericItem("crystal_glyph_stargate");
	
	/**
	 * Used for fast dialing the gate
	 * To be removed in final edition
	 */
	public static Item dialerFast = ItemHelper.createGenericItem("dialer_fast");
	
	/**
	 * Diffrent Naquadah(main Stargate building material) stages of purity
	 */
	public static Item naquadahShard = ItemHelper.createGenericItem("naquadah_shard");
	public static Item naquadahAlloy = ItemHelper.createGenericItem("naquadah_alloy");
	
	/**
	 * Crafting items
	 */
	public static Item crystalSeed = ItemHelper.createGenericItem("crystal_fragment");
	public static Item crystalBlue = ItemHelper.createGenericItem("crystal_blue");
	public static Item crystalRed = ItemHelper.createGenericItem("crystal_red");
	public static Item crystalEnder = ItemHelper.createGenericItem("crystal_ender");
	public static Item crystalYellow = ItemHelper.createGenericItem("crystal_yellow");
	public static Item crystalWhite = ItemHelper.createGenericItem("crystal_white");
	
	public static Item circuitControlBase = ItemHelper.createGenericItem("circuit_control_base");
	public static Item circuitControlCrystal = ItemHelper.createGenericItem("circuit_control_crystal");
	public static Item circuitControlNaquadah = ItemHelper.createGenericItem("circuit_control_naquadah");
		
	public static Item stargateRingFragment = ItemHelper.createGenericItem("stargate_ring_fragment");
	public static Item trRingFragment = ItemHelper.createGenericItem("transportrings_ring_fragment");
	public static Item holderCrystal = ItemHelper.createGenericItem("holder_crystal");
	
	public static Item dhdBrb = ItemHelper.createGenericItem("dhd_brb");
	
	public static NotebookPageItem notebookPageItem = new NotebookPageItem();
	
	private static Item[] items = {
		analyzerAncient,
		
		crystalControlDhd,
		
		crystalGlyphDhd,
		crystalGlyphStargate,
		
		dialerFast,
		
		naquadahShard,
		naquadahAlloy,
		
		crystalSeed,
		crystalBlue,
		crystalRed,
		crystalEnder,
		crystalYellow,
		crystalWhite,
		
		circuitControlBase,
		circuitControlCrystal,
		circuitControlNaquadah,
		
		stargateRingFragment,
		trRingFragment,
		holderCrystal,
		
		dhdBrb
	};
	
	@SubscribeEvent
	public static void onRegisterItems(Register<Item> event) {	
		for (Item item : items) {
			event.getRegistry().register(item);
		}
		
		event.getRegistry().register(notebookPageItem);
	}
	
	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {		
		for (Item item : items) {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
		
		ModelLoader.setCustomModelResourceLocation(notebookPageItem, 0, new ModelResourceLocation(notebookPageItem.getRegistryName() + "_empty", "inventory"));
		ModelLoader.setCustomModelResourceLocation(notebookPageItem, 1, new ModelResourceLocation(notebookPageItem.getRegistryName() + "_filled", "inventory"));

	}
}
