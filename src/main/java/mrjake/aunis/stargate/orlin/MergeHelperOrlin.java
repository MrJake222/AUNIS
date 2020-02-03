package mrjake.aunis.stargate.orlin;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.stargate.MergeHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MergeHelperOrlin {
	public static BlockPos findBase(World world, BlockPos currentPos) {
		int x = currentPos.getX();
		int y = currentPos.getY();
		int z = currentPos.getZ();
		
		for (BlockPos base : BlockPos.getAllInBoxMutable(x-1, y-2, z-1, x+1, y, z+1)) {
			if (world.getBlockState(base).getBlock() == AunisBlocks.stargateOrlinBaseBlock) {
				return base.toImmutable();
			}
		}
		
		return null;
	}
	
	private static final Map<BlockPos, EnumFacing> BLOCK_MAP = new HashMap<>(); 
	
	static {
		BLOCK_MAP.put(new BlockPos(-1, 0, 0), EnumFacing.EAST);
		BLOCK_MAP.put(new BlockPos(-1, 1, 0), EnumFacing.DOWN);
		BLOCK_MAP.put(new BlockPos(-1, 2, 0), EnumFacing.UP);
		BLOCK_MAP.put(new BlockPos(0, 2, 0), EnumFacing.UP);
		BLOCK_MAP.put(new BlockPos(1, 2, 0), EnumFacing.UP);
		BLOCK_MAP.put(new BlockPos(1, 1, 0), EnumFacing.DOWN);
		BLOCK_MAP.put(new BlockPos(1, 0, 0), EnumFacing.WEST);
	}
	
	public static boolean checkBlocks(World world, BlockPos basePos) {
		
		if (AunisConfig.debugConfig.checkGateMerge) {	
			EnumFacing facing = world.getBlockState(basePos).getValue(AunisProps.FACING_HORIZONTAL);
			
			for (BlockPos checkPos : BLOCK_MAP.keySet()) {			
				IBlockState state = world.getBlockState(MergeHelper.rotateAndGlobal(checkPos, facing, basePos));
								
				if (state.getBlock() != AunisBlocks.stargateOrlinMemberBlock || state.getValue(AunisProps.ORLIN_VARIANT) != facing) {					
					return false;
				}
			}
		}
				
		return true;
	}
	
	public static void updateMergeState(World world, BlockPos basePos, IBlockState state, boolean isMerged) {
		EnumFacing facing = state.getValue(AunisProps.FACING_HORIZONTAL);
		
		for (BlockPos checkPos : BLOCK_MAP.keySet()) {
			EnumFacing variant = BLOCK_MAP.get(checkPos);
			
			if (facing == EnumFacing.NORTH) {
				if (variant == EnumFacing.WEST) variant = EnumFacing.EAST;
				else if (variant == EnumFacing.EAST) variant = EnumFacing.WEST;
			}
			
			if (facing == EnumFacing.WEST) {
				if (variant == EnumFacing.WEST) variant = EnumFacing.NORTH;
				else if (variant == EnumFacing.EAST) variant = EnumFacing.SOUTH;
			}
			
			if (facing == EnumFacing.EAST) {
				if (variant == EnumFacing.WEST) variant = EnumFacing.SOUTH;
				else if (variant == EnumFacing.EAST) variant = EnumFacing.NORTH;
			}
			
			checkPos = MergeHelper.rotateAndGlobal(checkPos, facing, basePos);
			
			if (world.getBlockState(checkPos).getBlock() == AunisBlocks.stargateOrlinMemberBlock) {
				IBlockState memberState = world.getBlockState(checkPos);
				boolean renderblock = memberState.getValue(AunisProps.RENDER_BLOCK);
				
				if ((!isMerged && !renderblock) || (isMerged && renderblock))
					memberState = memberState.withProperty(AunisProps.ORLIN_VARIANT, isMerged ? variant : facing);
				
				world.setBlockState(checkPos, memberState.withProperty(AunisProps.RENDER_BLOCK, !isMerged));
			}
		}
		
		if (world.getBlockState(basePos).getBlock() == AunisBlocks.stargateOrlinBaseBlock)
			world.setBlockState(basePos, world.getBlockState(basePos).withProperty(AunisProps.RENDER_BLOCK, !isMerged));
	}
}
