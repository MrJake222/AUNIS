package mrjake.aunis.init;

import mrjake.aunis.Aunis;
import mrjake.aunis.item.ItemBase;

public class AunisItems {
	public static ItemBase naquadahItem = new ItemBase("naquadah_item", Aunis.aunisCreativeTab);
	
	public static void initItemModels() {
		naquadahItem.registerModel();
	}
}
