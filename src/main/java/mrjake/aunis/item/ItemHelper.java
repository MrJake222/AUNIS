package mrjake.aunis.item;

import mrjake.aunis.Aunis;
import net.minecraft.item.Item;

public class ItemHelper {

	public static Item createGenericItem(String name) {
		Item item = new Item();
		
		item.setRegistryName(Aunis.ModID + ":" + name);
		item.setUnlocalizedName(Aunis.ModID + "." + name);
		
		item.setCreativeTab(Aunis.aunisCreativeTab);
		
		return item;
	}

}
