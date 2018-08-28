package mrjake.aunis.block;

public class AunisBlocks {
	public static StargateBaseBlock stargateBaseBlock = new StargateBaseBlock();
	public static DHDBlock dhdBlock = new DHDBlock();
	public static RingBlock ringBlock = new RingBlock();
	public static ChevronBlock chevronBlock = new ChevronBlock();
	
	public static void initBlockModels() {
		stargateBaseBlock.registerModel();
		dhdBlock.registerModel();
		ringBlock.registerModel();
		chevronBlock.registerModel();
	}
}

