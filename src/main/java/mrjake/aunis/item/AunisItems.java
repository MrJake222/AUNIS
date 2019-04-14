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
	public static Item crystalGlyphEmpty = ItemHelper.createGenericItem("crystal_glyph_empty");
	public static Item crystalGlyphStargate = ItemHelper.createGenericItem("crystal_glyph_stargate");
	
	/**
	 * Used for fast dialing the gate
	 * To be removed in final edition
	 */
	public static Item dialerFast = ItemHelper.createGenericItem("dialer_fast");
	
	/**
	 * Diffrent Naquadah(main Stargate building material) stages of purity
	 */
	public static Item naquadahOrePure = ItemHelper.createGenericItem("naquadah_ore_pure");
	public static Item naquadahOreShard = ItemHelper.createGenericItem("naquadah_ore_shard");
	public static Item naquadahRefined = ItemHelper.createGenericItem("naquadah_refined");
	
	/**
	 * Crafting items
	 */
	public static Item crystalFragment = ItemHelper.createGenericItem("crystal_fragment");
	public static Item crystalBlank = ItemHelper.createGenericItem("crystal_blank");
	public static Item crystalBlue = ItemHelper.createGenericItem("crystal_blue");
	public static Item crystalRed = ItemHelper.createGenericItem("crystal_red");
	public static Item crystalArray = ItemHelper.createGenericItem("crystal_array");
		
	public static Item stargateRingFragment = ItemHelper.createGenericItem("stargate_ring_fragment");
	public static Item holderCrystal = ItemHelper.createGenericItem("holder_crystal");
	
	public static Item dhdBrb = ItemHelper.createGenericItem("dhd_brb");
	
	private static Item[] items = {
		analyzerAncient,
		
		crystalControlDhd,
		
		crystalGlyphDhd,
		crystalGlyphEmpty,
		crystalGlyphStargate,
		
		dialerFast,
		
		naquadahOrePure,
		naquadahOreShard,
		naquadahRefined,
		
		crystalFragment,
		crystalBlank,
		crystalBlue,
		crystalRed,
		crystalArray,
		
		stargateRingFragment,
		holderCrystal,
		
		dhdBrb
	};
	
	@SubscribeEvent
	public static void onRegisterItems(Register<Item> event) {	
		for (Item item : items) {
			event.getRegistry().register(item);
		}
	}
	
	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {		
		for (Item item : items) {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}
}
