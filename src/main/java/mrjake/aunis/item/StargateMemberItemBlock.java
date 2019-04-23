package mrjake.aunis.item;

import mrjake.aunis.block.StargateMemberBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class StargateMemberItemBlock extends ItemBlock {

	public StargateMemberItemBlock(Block block) {
		super(block);
		
		setRegistryName(StargateMemberBlock.blockName);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {		
		return super.getUnlocalizedName(stack) + ((stack.getMetadata() & 0x08) != 0 ? "_chevron" : "_ring");
	}	
}
