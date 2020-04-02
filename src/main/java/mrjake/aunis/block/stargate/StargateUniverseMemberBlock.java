package mrjake.aunis.block.stargate;

import mrjake.aunis.stargate.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.StargateUniverseMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseMemberTile;

public class StargateUniverseMemberBlock extends StargateClassicMemberBlock {

	public static final String BLOCK_NAME = "stargate_universe_member_block";

	@Override
	protected String getBlockName() {
		return BLOCK_NAME;
	}

	@Override
	protected StargateClassicMemberTile getTileEntity() {
		return new StargateUniverseMemberTile();
	}

	@Override
	protected StargateAbstractMergeHelper getMergeHelper() {
		return StargateUniverseMergeHelper.INSTANCE;
	}

	
}
