package mrjake.aunis.item;

import mrjake.aunis.Aunis;
import net.minecraft.item.Item;

public class AunisItems {
	public static ItemBase naquadahOreShard = new ItemBase("naquadah_ore_shard", Aunis.aunisCreativeTab);
	public static ItemBase pureNaquadahOre = new ItemBase("pure_naquadah_ore", Aunis.aunisCreativeTab);
	public static ItemBase refinedNaquadah = new ItemBase("refined_naquadah", Aunis.aunisCreativeTab);
	
	public static Item[] items = {
		naquadahOreShard,
		pureNaquadahOre,
		refinedNaquadah };
}
