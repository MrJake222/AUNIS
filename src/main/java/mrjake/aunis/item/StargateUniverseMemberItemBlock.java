package mrjake.aunis.item;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.stargate.StargateUniverseMemberBlock;
import mrjake.aunis.stargate.EnumMemberVariant;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class StargateUniverseMemberItemBlock extends ItemBlock {

	public StargateUniverseMemberItemBlock(Block block) {
		super(block);
		
		setRegistryName(StargateUniverseMemberBlock.BLOCK_NAME);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		EnumMemberVariant variant = AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK.getStateFromMeta(stack.getMetadata()).getValue(AunisProps.MEMBER_VARIANT);
		
		switch (variant) {
			case CHEVRON:
				return "tile.aunis.stargate_universe_chevron_block";
				
			case RING:
				return "tile.aunis.stargate_universe_ring_block";
				
			default:
				return stack.getUnlocalizedName();
		}
	}	
}
