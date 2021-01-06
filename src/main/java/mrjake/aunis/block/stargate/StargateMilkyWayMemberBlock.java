package mrjake.aunis.block.stargate;

import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateMilkyWayMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;

public class StargateMilkyWayMemberBlock extends StargateClassicMemberBlock {

	public static final String BLOCK_NAME = "stargate_milkyway_member_block";

	public StargateMilkyWayMemberBlock() {
		super();
		
		setResistance(2000.0f);
	}
	
	@Override
	protected String getBlockName() {
		return BLOCK_NAME;
	}

	@Override
	protected StargateClassicMemberTile getTileEntity() {
		return new StargateMilkyWayMemberTile();
	}

	@Override
	protected StargateAbstractMergeHelper getMergeHelper() {
		return StargateMilkyWayMergeHelper.INSTANCE;
	}

	
}
