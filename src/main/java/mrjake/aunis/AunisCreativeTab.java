package mrjake.aunis;

import mrjake.aunis.init.AunisItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class AunisCreativeTab extends CreativeTabs {
	
	public AunisCreativeTab() {
		
		super(Aunis.ModID);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		
		return new ItemStack(AunisItems.naquadahItem);
	}
}
