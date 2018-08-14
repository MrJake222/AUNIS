package mrjake.aunis.init;

import mrjake.aunis.stargate.dhd.DHDBlock;
import mrjake.aunis.stargate.sgbase.StargateBaseBlock;

public class AunisBlocks {
	public static StargateBaseBlock stargateBaseBlock = new StargateBaseBlock();
	public static DHDBlock DHDBlock = new DHDBlock();
	
	public static void initBlockModels() {
		stargateBaseBlock.registerModel();
		DHDBlock.registerModel();
	}
}

