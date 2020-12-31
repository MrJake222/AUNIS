package mrjake.aunis.item;

import mrjake.aunis.block.stargate.StargateUniverseMemberBlock;

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
