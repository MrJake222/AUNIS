package mrjake.aunis.util;

import javax.annotation.Nullable;

import li.cil.oc.api.event.RackMountableRenderEvent.TileEntity;
import mrjake.aunis.config.AunisConfig;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LinkingHelper {
	
	/**
	 * Finds closest block of the given type within given radius.
	 * 
	 * @param world World instance.
	 * @param startPos Starting position.
	 * @param radius Radius. Subtracted and added to the startPos.
	 * @param targetBlock. Searched block instance. Must provide {@link TileEntity} and {@link TileEntity} should implement {@link ILinkable}.
	 * @return Found block's {@link BlockPos}. Null if not found.
	 */
	
	@Nullable
	public static BlockPos findClosestUnlinked(World world, BlockPos startPos, BlockPos radius, Block targetBlock) {		
		double closestDistance = Double.MAX_VALUE;
		BlockPos closest = null;
		
		for (BlockPos target : BlockPos.getAllInBoxMutable(startPos.subtract(radius), startPos.add(radius))) {
			if (world.getBlockState(target).getBlock() == targetBlock) {
				
				ILinkable linkedTile = (ILinkable) world.getTileEntity(target);
									
				if (linkedTile.canLinkTo()) {
					double distanceSq = startPos.distanceSq(target);
					
					if (distanceSq < closestDistance) {
						closestDistance = distanceSq;
						closest = target.toImmutable();
					}
				}
			}
		}
		
		return closest;
	}
	
	/**
	 * Returns proper DHD range. 
	 * 
	 * @return DHD range.
	 */
	public static BlockPos getDhdRange() {
		int xz = AunisConfig.dhdConfig.rangeFlat;
		int y = AunisConfig.dhdConfig.rangeVertical;
		
		return new BlockPos(xz, y ,xz);
	}
}
