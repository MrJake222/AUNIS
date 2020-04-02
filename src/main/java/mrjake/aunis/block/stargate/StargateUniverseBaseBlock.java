package mrjake.aunis.block.stargate;

import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseBaseTile;

public class StargateUniverseBaseBlock extends StargateClassicBaseBlock {

	private static final String BLOCK_NAME = "stargate_universe_base_block";

	@Override
	protected String getBlockName() {
		return BLOCK_NAME;
	}

	@Override
	protected StargateClassicBaseTile getTileEntity() {
		return new StargateUniverseBaseTile();
	}
}
