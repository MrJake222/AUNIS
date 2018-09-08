package mrjake.aunis.item;

import net.minecraft.item.Item;

public class AunisItems {
	public static ItemBase naquadahOreShard = new ItemBase("naquadah_ore_shard");
	public static ItemBase pureNaquadahOre = new ItemBase("pure_naquadah_ore");
	public static ItemBase refinedNaquadah = new ItemBase("refined_naquadah");
	public static ItemBase emptyCrystal = new ItemBase("empty_crystal");
	public static ItemBase dhdControlCrystal = new ItemBase("dhd_control_crystal");
	public static ItemBase stargateAddressCrystal = new ItemBase("stargate_address_crystal");
	public static ItemBase ancientAnalyzer = new ItemBase("ancient_analyzer");
	public static ItemBase fastDialer = new ItemBase("fast_dialer");
	
	public static Item[] items = {
		naquadahOreShard,
		pureNaquadahOre,
		refinedNaquadah,
		emptyCrystal,
		dhdControlCrystal,
		stargateAddressCrystal,
		ancientAnalyzer,
		fastDialer };
}
