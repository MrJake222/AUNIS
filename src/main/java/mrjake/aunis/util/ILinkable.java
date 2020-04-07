package mrjake.aunis.util;

import net.minecraft.tileentity.TileEntity;

/**
 * To be implemented on {@link TileEntit} which is going to be used with {@link LinkingHelper#findClosestUnlinked(World, BlockPos, BlockPos, Block)};
 * 
 * @author MrJake222
 */
public interface ILinkable {

	/**
	 * Checks if we can connect to this Linkable.
	 * @return True if the {@link TileEntity} can be linked to, false otherwise.
	 */
	boolean canLinkTo();
}
