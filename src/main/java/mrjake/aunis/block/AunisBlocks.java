package mrjake.aunis.block;

public class AunisBlocks {
	public static StargateBaseBlock stargateBaseBlock = new StargateBaseBlock();
	public static DHDBlock DHDBlock = new DHDBlock();
	
	public static void initBlockModels() {
		stargateBaseBlock.registerModel();
		DHDBlock.registerModel();
	}
}

