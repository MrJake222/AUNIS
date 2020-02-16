package mrjake.aunis.tileentity.util;

import mrjake.aunis.command.CommandPrepare;
import net.minecraft.tileentity.TileEntity;

/**
 * Implemented by {@link TileEntity} that will be saved to a structure file.
 * Used to clean up NBT data before the save.
 * Called by {@link CommandPrepare}
 * 
 * @author MrJake222
 * 
 */
public interface PreparableInterface {
	
	/**
	 * Clears unnecessary data from {@link TileEntity}.
	 */
	public abstract void prepare();
}
