package mrjake.aunis;

import net.minecraft.util.math.BlockPos;

public class AunisConfig {
	
	public static final BlockPos dhdRange = new BlockPos(10,3,10);
	
	public static final int stargateEnergyStorage = 14000000; // 14 000 000
	public static final int stargateMaxEnergyTransfer = 140000; // 140 000
	
	public static final int dhdCrystalEnergyStorage = 14000000; // 14 000 000
	public static final int dhdCrystalMaxEnergyTransfer = 140000; // 140 000
	
	public static final double openingBlockToEnergyRatio = 1500 / 10.0; // 1,500 RF / 10m
	public static final double keepAliveBlockToEnergyRatioPerTick = 8 / 10.0; // 8 RF / 10m PER ONE TICK

	public static double netherMultiplier = 2.0;

	public static double theEndMultiplier = 2.5;
}
