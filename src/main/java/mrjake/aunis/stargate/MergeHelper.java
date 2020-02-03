package mrjake.aunis.stargate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class MergeHelper {
	
	private static BlockPos findBasePos(IBlockAccess blockAccess, BlockPos currentPos, IBlockState state) {
		// 5x9x5
		
		int x = currentPos.getX();
		int y = currentPos.getY();
		int z = currentPos.getZ();
		
		EnumFacing facing = state.getValue(AunisProps.FACING_HORIZONTAL);
		
		Iterable<MutableBlockPos> blocks = null;
		
		switch (facing.getAxis()) {
			case X:
				blocks = BlockPos.getAllInBoxMutable(x, y-10, z-5, x, y, z+5);
				break;
				
			case Z:
				blocks = BlockPos.getAllInBoxMutable(x-5, y-10, z, x+5, y, z);
				break;
				
			default:
				break;
		}
		
		for (BlockPos gatePos : blocks) {
			if (blockAccess.getBlockState(gatePos).getBlock() == AunisBlocks.stargateMilkyWayBaseBlock) {
				return gatePos;
			}
		}
		
		return null;
	}
	
	public static StargateMilkyWayBaseTile findBaseTile(IBlockAccess blockAccess, BlockPos currentPos, IBlockState state) {
		BlockPos gatePos = findBasePos(blockAccess, currentPos, state);
		
		if (gatePos != null)
			return (StargateMilkyWayBaseTile) blockAccess.getTileEntity(gatePos);
		
		else
			return null;
	}
	
	private static List<BlockPos> ringBlocks = Arrays.asList(
			new BlockPos(-1, 0, 0), 
			new BlockPos(-3, 1, 0),
			new BlockPos(-4, 3, 0), 
			new BlockPos(-5, 4, 0), 
			new BlockPos(-4, 6, 0), 
			new BlockPos(-4, 7, 0), 
			new BlockPos(-2, 9, 0), 
			new BlockPos(-1, 9, 0), 
			new BlockPos(1, 9, 0), 
			new BlockPos(2, 9, 0), 
			new BlockPos(4, 7, 0), 
			new BlockPos(4, 6, 0), 
			new BlockPos(5, 4, 0), 
			new BlockPos(4, 3, 0), 
			new BlockPos(3, 1, 0), 
			new BlockPos(1, 0, 0));
	
	// Light up order
	public static List<BlockPos> chevronBlocks = Arrays.asList(
			new BlockPos(3, 8, 0), 
			new BlockPos(5, 5, 0), 
			new BlockPos(4, 2, 0), 
			new BlockPos(-4, 2, 0), 
			new BlockPos(-5, 5, 0), 
			new BlockPos(-3, 8, 0), 
			new BlockPos(2, 0, 0),
			new BlockPos(-2, 0, 0), 
			new BlockPos(0, 9, 0));
	
	public static List<BlockPos> getWithoutLastChevronBlock() {
		return chevronBlocks.subList(0, chevronBlocks.size() - 2);
	}
	
	public static BlockPos getLastChevronBlock() {
		return chevronBlocks.get(chevronBlocks.size() - 1);
	}
	
	private static Map<EnumMemberVariant, List<BlockPos>> blockMap = new HashMap<EnumMemberVariant, List<BlockPos>>();
	
	static {
		blockMap.put(EnumMemberVariant.RING, ringBlocks);
		blockMap.put(EnumMemberVariant.CHEVRON, chevronBlocks);
	}
	
	public static BlockPos rotateAndGlobal(BlockPos checkPos, EnumFacing facing, BlockPos basePos) {
		int x = 0;
		int z = 0;
		
		switch ((int) facing.getHorizontalAngle()) {
			case 0:
				x = checkPos.getX();
				z = checkPos.getZ();
				break;
				
			case 90:
				x = -checkPos.getZ();
				z = checkPos.getX();
				break;
				
			case 180:
				x = -checkPos.getX();
				z = -checkPos.getZ();
				break;
				
			case 270:
				x = checkPos.getZ();
				z = -checkPos.getX();
				break;				
		}
		
		return basePos.add(x, checkPos.getY(), z);
	}
	
	public static boolean checkBlocks(World world, BlockPos basePos) {
		
		if (AunisConfig.debugConfig.checkGateMerge) {	
			EnumFacing facing = world.getBlockState(basePos).getValue(AunisProps.FACING_HORIZONTAL);
			
			for ( EnumMemberVariant variant : blockMap.keySet() ) {			
				for (BlockPos checkPos : blockMap.get(variant)) {	
					IBlockState state = world.getBlockState(rotateAndGlobal(checkPos, facing, basePos));
					
					if (state.getBlock() != AunisBlocks.stargateMilkyWayMemberBlock || state.getValue(AunisProps.MEMBER_VARIANT) != variant) {					
						return false;
					}
				}
			}
		}
				
		return true;
	}
	
	public static void updateChevRingMergeState(World world, BlockPos basePos, IBlockState state, boolean isMerged) {
		EnumFacing facing = state.getValue(AunisProps.FACING_HORIZONTAL);
		
		for ( EnumMemberVariant variant : blockMap.keySet() ) {			
			for (BlockPos checkPos : blockMap.get(variant)) {	
				
				checkPos = rotateAndGlobal(checkPos, facing, basePos);
				
				IBlockState blockState = world.getBlockState(checkPos);
				
				if (blockState.getBlock() == AunisBlocks.stargateMilkyWayMemberBlock) {		
					StargateMilkyWayMemberTile memberTile = (StargateMilkyWayMemberTile) world.getTileEntity(checkPos);
					
					if ((isMerged && !memberTile.isMerged()) || (memberTile.isMerged() && memberTile.getBasePos().equals(basePos))) {
						
						ItemStack camoStack = memberTile.getCamoItemStack();
						if (camoStack != null) {
							InventoryHelper.spawnItemStack(world, checkPos.getX(), checkPos.getY(), checkPos.getZ(), camoStack);
							memberTile.setCamoState(null);
							
							TargetPoint point = new TargetPoint(world.provider.getDimension(), checkPos.getX(), checkPos.getY(), checkPos.getZ(), 512);
							AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(checkPos, StateTypeEnum.CAMO_STATE, memberTile.getState(StateTypeEnum.CAMO_STATE)), point);
						}
						
						memberTile.setBasePos(isMerged ? basePos : null);
						
						
						world.setBlockState(checkPos, blockState
							.withProperty(AunisProps.RENDER_BLOCK, !isMerged), 3);
					}
				}
			}
		}
	}
	
	public static void updateChevRingRotation(World world, BlockPos basePos, EnumFacing facing) {	
		
		for ( EnumMemberVariant variant : blockMap.keySet() ) {			
			for (BlockPos checkPos : blockMap.get(variant)) {
				checkPos = rotateAndGlobal(checkPos, facing, basePos);
				
				world.setBlockState(checkPos, world.getBlockState(checkPos).withProperty(AunisProps.FACING_HORIZONTAL, facing));
			}
		}
	}
	
	public static void updateChevRingBasePos(World world, BlockPos basePos, EnumFacing facing) {	
		
		for ( EnumMemberVariant variant : blockMap.keySet() ) {			
			for (BlockPos checkPos : blockMap.get(variant)) {
				checkPos = rotateAndGlobal(checkPos, facing, basePos);
				
				TileEntity tileEntity = world.getTileEntity(checkPos);
				
				if (tileEntity instanceof StargateMilkyWayMemberTile) {
					((StargateMilkyWayMemberTile) tileEntity).setBasePos(basePos);
				}
			}
		}
	}
}
