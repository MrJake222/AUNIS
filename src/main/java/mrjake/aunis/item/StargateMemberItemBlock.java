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
	public String getTranslationKey(ItemStack stack) {		
		return super.getTranslationKey(stack) + ((stack.getMetadata() & 0x08) != 0 ? "_chevron" : "_ring");
	}	
}
