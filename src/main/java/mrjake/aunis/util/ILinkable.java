package mrjake.aunis.util;

import li.cil.oc.api.event.RackMountableRenderEvent.TileEntity;

/**
 * To be implemented on {@link TileEntity} which is going to be used with {@link LinkingHelper#findClosestUnlinked(World, BlockPos, BlockPos, Block)};
 * 
 * @author MrJake222
 */
public interface ILinkable {

	/**
	 * If this block is already linked to a controller of some kind.
	 * 
	 * @return isLinked. Usually <code>linkedPos != null</code>.
	 */
	boolean isLinked();
}
