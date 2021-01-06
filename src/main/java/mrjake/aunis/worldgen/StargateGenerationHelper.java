package mrjake.aunis.worldgen;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class StargateGenerationHelper {
	
	public static class FreeSpace {
		private Map<EnumFacing, DirectionResult> freeSpaceMap = new HashMap<EnumFacing, DirectionResult>();
		private Map.Entry<EnumFacing, DirectionResult> maxFreeSpace;
		
		public void put(EnumFacing direction, DirectionResult distance, boolean ignoreInMaximum) {
			if (!ignoreInMaximum && (maxFreeSpace == null || distance.distance > maxFreeSpace.getValue().distance))
				maxFreeSpace = new AbstractMap.SimpleEntry<EnumFacing, DirectionResult>(direction, distance);
			
			freeSpaceMap.put(direction, distance);
		}
		
		public DirectionResult getDistance(EnumFacing direction) {
			return freeSpaceMap.get(direction);
		}
		
		public Map.Entry<EnumFacing, DirectionResult> getMaxDistance() {
			return maxFreeSpace;
		}
		
		@Override
		public String toString() {
			return freeSpaceMap.toString();
		}
	}
	
	public static class Direction {
		private EnumFacing direction;
		private boolean checkDown;
		
		private boolean hasMinDistance = false;
		private int minDistance;
		
		private boolean ignoreInMaximum = false;
		private int maxVertDiff;
		
		public Direction(EnumFacing direction, boolean checkDown, int maxVertDiff) {
			this.checkDown = checkDown;
			this.direction = direction;
			this.maxVertDiff = maxVertDiff;
		}
		
		public EnumFacing getDirection() {
			return direction;
		}
		
		public boolean checkDown(IBlockState blockState, List<BlockMatcher> matchers) {
			if (checkDown) {				
				for (BlockMatcher matcher : matchers) {
					if (matcher.apply(blockState))
						return true;
				}
				
				return false;
			}
			
			return true;
		}

		public Direction setRequiredMinimum(int distance) {
			this.hasMinDistance = true;
			this.minDistance = distance;
			
			return this;
		}
		
		public boolean checkRequiredMinimum(int distance) {
			if (hasMinDistance)
				return distance >= minDistance;
			
			return true;
		}
		
		public Direction setIgnoreInMaximum() {
			this.ignoreInMaximum = true;
			
			return this;
		}
		
		public boolean getIgnoreInMaximum() {
			return ignoreInMaximum;
		}
		
		public int getMaxVertDiff() {
			return maxVertDiff;
		}
	}
	
	public static class DirectionResult {
		public int distance;
		public int ydiff;

		public DirectionResult(int distance, int ydiff) {
			this.distance = distance;
			this.ydiff = ydiff;
		}
		
		@Override
		public String toString() {
			return "[dist="+distance+", ydiff="+ydiff+"]";
		}
	}
	
	public static DirectionResult getFreeSpaceInDirection(IBlockAccess blockAccess, BlockPos start, Direction direction, int maxDistance, List<BlockMatcher> allowedBlocksBelow) {
		int distance = 0;
		int ydiff = 0;
		
		while (distance < maxDistance) {
			BlockPos offset = start.offset(direction.getDirection(), distance+1);
			if (!blockAccess.isAirBlock(offset)) {
				if (ydiff == direction.getMaxVertDiff())
					break;
				
				start = start.up();
				ydiff++;
			}
			
			else if (blockAccess.isAirBlock(offset.down()) && direction.checkDown) {
				if (-ydiff == direction.getMaxVertDiff())
					break;
				
				start = start.down();
				ydiff--;
			}
			
			else if (!direction.checkDown(blockAccess.getBlockState(offset.down()), allowedBlocksBelow)) {
				break;
			}
			
			else {
				distance++;
			}
		}
		
		return new DirectionResult(distance, ydiff);
	}
	
	@Nullable
	public static FreeSpace getFreeSpaceInDirections(IBlockAccess blockAccess, BlockPos start, List<Direction> directions, int maxDistance, List<BlockMatcher> allowedBlocksBelow) {
		FreeSpace freeSpace = new FreeSpace();
		
		for (Direction direction : directions) {
			DirectionResult free = getFreeSpaceInDirection(blockAccess, start, direction, maxDistance, allowedBlocksBelow);
			
			if (!direction.checkRequiredMinimum(free.distance))
				return null;
			
			freeSpace.put(direction.getDirection(), free, direction.getIgnoreInMaximum());
		}
				
		return freeSpace;
	}
	
	public static void updateLinkedGate(World world, BlockPos gatePos, BlockPos dhdPos) {
		StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(gatePos);
		DHDTile dhdTile = (DHDTile) world.getTileEntity(dhdPos);
		
		if (dhdTile != null) {
			dhdTile.setLinkedGate(gatePos);
			gateTile.setLinkedDHD(dhdPos);
		}
	}
}
