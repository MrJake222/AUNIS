package mrjake.aunis.block.stargate;

import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateUniverseMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseMemberTile;

public class StargateUniverseMemberBlock extends StargateClassicMemberBlock {

	public static final String BLOCK_NAME = "stargate_universe_member_block";

	public StargateUniverseMemberBlock() {
		super();
		
		setResistance(20.0f);
	}
	
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
