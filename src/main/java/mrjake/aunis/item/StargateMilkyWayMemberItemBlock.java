package mrjake.aunis.item;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import mrjake.aunis.stargate.EnumMemberVariant;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class StargateMilkyWayMemberItemBlock extends ItemBlock {

	public StargateMilkyWayMemberItemBlock(Block block) {
		super(block);
		
		setRegistryName(StargateMilkyWayMemberBlock.BLOCK_NAME);
		setHasSubtypes(true);
	}
	
	@Override
	public String getTranslationKey(ItemStack stack) {		
		EnumMemberVariant variant = AunisBlocks.stargateMilkyWayMemberBlock.getStateFromMeta(stack.getMetadata()).getValue(AunisProps.MEMBER_VARIANT);
		
		switch (variant) {
			case CHEVRON:
				return "tile.aunis.stargate_milkyway_chevron_block";
				
			case RING:
				return "tile.aunis.stargate_milkyway_ring_block";
				
			default:
				return stack.getTranslationKey();
		}
	}	
}
