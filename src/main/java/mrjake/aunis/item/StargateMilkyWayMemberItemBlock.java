package mrjake.aunis.item;

import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;

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
