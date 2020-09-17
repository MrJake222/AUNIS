package mrjake.aunis.tileentity.util;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
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
	 * Clears unnecessary data from {@link TileEntity} preparing it to be saved to NBT.
	 * {@link TileEntity#writeToNBT(net.minecraft.nbt.NBTTagCompound)} must perform successfully on such prepared TE.
	 * 
	 * @param sender Sender of the command - used for sending replies.
	 * @param command Command issued  - used for sending replies.
	 * @return True if operation successful.
	 */
	public abstract boolean prepare(ICommandSender sender, ICommand command);
}
