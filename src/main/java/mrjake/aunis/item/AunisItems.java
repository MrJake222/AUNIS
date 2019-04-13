package mrjake.aunis.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
	 * Crafting of the Stargate
	 */
	public static CrystalControlDHDItem crystalControlDhd = new CrystalControlDHDItem();
	public static Item crystalControlEmpty = ItemHelper.createGenericItem("crystal_control_empty");
	public static Item crystalControlChevron = ItemHelper.createGenericItem("crystal_control_chevron");
	public static Item crystalControlStargate = ItemHelper.createGenericItem("crystal_control_stargate");
	
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
	
	public static Item stargateRingFragment = ItemHelper.createGenericItem("stargate_ring_fragment");
	
	private static Item[] items = {
		analyzerAncient,
		
		crystalControlDhd,
		crystalControlEmpty,
		crystalControlChevron,
		crystalControlStargate,
		
		crystalGlyphDhd,
		crystalGlyphEmpty,
		crystalGlyphStargate,
		
		dialerFast,
		
		naquadahOrePure,
		naquadahOreShard,
		naquadahRefined,
		
		stargateRingFragment
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
