package mrjake.aunis.tileentity.util;

import javax.annotation.Nullable;

import mrjake.aunis.stargate.EnumScheduledTask;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Used with {@link EnumScheduledTask} to execute scheduled tasks.
 * 
 * @author MrJake
 */
public interface ScheduledTaskExecutorInterface {
	
	/**
	 * Adds given {@link ScheduledTask} to the list.
	 * 
	 * @param scheduledTask The task to be added.
	 */
	public void addTask(ScheduledTask scheduledTask);
	
	/**
	 * Executes given task.
	 * 
	 * @param scheduledTask The task.
	 * @param customData Custom data passed by the user.
	 */
	public void executeTask(EnumScheduledTask scheduledTask, @Nullable NBTTagCompound customData);
}
