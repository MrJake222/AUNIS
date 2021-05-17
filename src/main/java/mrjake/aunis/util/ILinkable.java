package mrjake.aunis.util;

import net.minecraft.tileentity.TileEntity;

/**
 * To be implemented on {@link TileEntity} which is going to be used with {@link LinkingHelper#findClosestUnlinked(World, BlockPos, BlockPos, Block)};
 * 
 * @author MrJake222
 */
public interface ILinkable {

	/**
	 * Checks if we can connect to this Linkable.
	 * @return True if the {@link TileEntity} can be linked to, false otherwise.
	 */
	boolean canLinkTo();

	/**
	* Returns the link id. Normally when e.g. a gate with DHD is being moved to a place where
	* a DHD is already present, it tries to connect to the DHD regardless of its previous connection.
	* linkId property is meant to solve that problem.
	* @return linkId.
	*/
	int getLinkId();
}
