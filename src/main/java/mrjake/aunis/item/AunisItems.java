package mrjake.aunis.item;

import mrjake.aunis.Aunis;

public class AunisItems {
	public static ItemBase naquadahItem = new ItemBase("naquadah_item", Aunis.aunisCreativeTab);
	
	public static void initItemModels() {
		naquadahItem.registerModel();
	}
}
