package mrjake.aunis.stargate.merge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MergeHelper {
	
	public static StargateBaseTile findBaseTile(World world, BlockPos currentPos) {
		// 6x10x6
		
		int x = currentPos.getX();
		int y = currentPos.getY();
		int z = currentPos.getZ();
				
		for (BlockPos gatePos : BlockPos.getAllInBox(x-6, y-10, z-6, x+6, y, z+6) ) {
			if (world.getBlockState(gatePos).getBlock() instanceof StargateBaseBlock) {
				return (StargateBaseTile) world.getTileEntity(gatePos);
			}
		}
		
		return null;
	}
	
	public static void updateBaseMergeState(World world, BlockPos currentPos) {
		StargateBaseTile gateTile = findBaseTile(world, currentPos);
		
		if (gateTile != null)		
			gateTile.updateMergeState(checkBlocks(gateTile));
	}
	
	public static void unmergeBase(World world, BlockPos currentPos) {
		StargateBaseTile gateTile = findBaseTile(world, currentPos);
		
		if (gateTile != null)		
			gateTile.updateMergeState(false);
	}
	
	private static List<BlockPosition> ringBlocks = Arrays.asList(
			new BlockPosition(-1, 0, 0), 
			new BlockPosition(-3, 1, 0), 
			new BlockPosition(-4, 3, 0), 
			new BlockPosition(-5, 4, 0), 
			new BlockPosition(-4, 6, 0), 
			new BlockPosition(-4, 7, 0), 
			new BlockPosition(-2, 9, 0), 
			new BlockPosition(-1, 9, 0), 
			new BlockPosition(1, 9, 0), 
			new BlockPosition(2, 9, 0), 
			new BlockPosition(4, 7, 0), 
			new BlockPosition(4, 6, 0), 
			new BlockPosition(5, 4, 0), 
			new BlockPosition(4, 3, 0), 
			new BlockPosition(3, 1, 0), 
			new BlockPosition(1, 0, 0) );
	
	public static List<BlockPosition> chevronBlocks = Arrays.asList(
			new BlockPosition(-2, 0, 0), 
			new BlockPosition(-4, 2, 0), 
			new BlockPosition(-5, 5, 0), 
			new BlockPosition(-3, 8, 0), 
			new BlockPosition(0, 9, 0), 
			new BlockPosition(3, 8, 0), 
			new BlockPosition(5, 5, 0), 
			new BlockPosition(4, 2, 0), 
			new BlockPosition(2, 0, 0) );
	
	private static Map<Block, List<BlockPosition>> blockListMap;
	
	private static Map<Block, List<BlockPosition>> getBlockListMap() {
		if (blockListMap == null) {
			blockListMap = new HashMap<Block, List<BlockPosition>>();
			blockListMap.put(AunisBlocks.ringBlock, ringBlocks);
			blockListMap.put(AunisBlocks.chevronBlock, chevronBlocks);
		}
		
		return blockListMap;
	}
	
	public static boolean checkBlocks(StargateBaseTile gateTile) {
//		World world = gateTile.getWorld();
//		BlockPos pos = gateTile.getPos();
//		
//		EnumFacing facing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
//		
//		for ( Map.Entry<Block, List<BlockPosition>> entry : getBlockListMap().entrySet() ) {			
//			for (BlockPosition blockPosition : entry.getValue()) {				
//				IBlockState state = world.getBlockState(blockPosition.rotateAndGlobal((int) facing.getHorizontalAngle(), pos));
//				
//				if ( state.getBlock() != entry.getKey() ) {
//					return false;
//				}
//				
//				if ( facing != state.getValue(AunisProps.FACING_HORIZONTAL) ) {
//					return false;
//				}
//			}
//		}
		
		return true;
	}

	public static void updateChevRingMergeState(StargateBaseTile gateTile, IBlockState state, boolean isMerged) {
		World world = gateTile.getWorld();
		BlockPos pos = gateTile.getPos();
		
		EnumFacing facing = state.getValue(AunisProps.FACING_HORIZONTAL);
		
		for ( Map.Entry<Block, List<BlockPosition>> entry : getBlockListMap().entrySet() ) {
			for (BlockPosition blockPosition : entry.getValue()) {
				
				BlockPos blockPos = blockPosition.rotateAndGlobal((int) facing.getHorizontalAngle(), pos);
				IBlockState blockState = world.getBlockState(blockPos);
				
				if ( blockState.getBlock() == entry.getKey() )
					world.setBlockState(blockPos, blockState.withProperty(AunisProps.RENDER_BLOCK, !isMerged), 2);
			}
		}
	}
}
