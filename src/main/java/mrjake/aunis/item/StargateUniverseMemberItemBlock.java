package mrjake.aunis.item;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import mrjake.aunis.block.stargate.StargateUniverseMemberBlock;
import mrjake.aunis.stargate.EnumMemberVariant;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public final class StargateUniverseMemberItemBlock extends StargateMemberItemBlock {

	public StargateUniverseMemberItemBlock(StargateUniverseMemberBlock block) {
		super(block);
	}

	@Override
	protected String getRingUnlocalizedName() {
		return "tile.aunis.stargate_universe_ring_block";
	}

	@Override
	protected String getChevronUnlocalizedName() {
		return "tile.aunis.stargate_universe_chevron_block";
	}
}
