package mrjake.aunis;

import mrjake.aunis.block.AunisBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class AunisCreativeTab extends CreativeTabs {
	
	public AunisCreativeTab() {
		super(Aunis.ModID);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK);
	}
}
