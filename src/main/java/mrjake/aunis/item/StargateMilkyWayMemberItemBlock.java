package mrjake.aunis.item;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import mrjake.aunis.stargate.EnumMemberVariant;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public final class StargateMilkyWayMemberItemBlock extends StargateMemberItemBlock {

	public StargateMilkyWayMemberItemBlock(StargateMilkyWayMemberBlock block) {
		super(block);
	}

	@Override
	protected String getRingUnlocalizedName() {
		return "tile.aunis.stargate_milkyway_ring_block";
	}

	@Override
	protected String getChevronUnlocalizedName() {
		return "tile.aunis.stargate_milkyway_chevron_block";
	}
}
