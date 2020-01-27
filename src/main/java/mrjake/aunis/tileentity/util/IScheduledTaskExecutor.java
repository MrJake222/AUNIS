package mrjake.aunis.tileentity.util;

import mrjake.aunis.stargate.EnumScheduledTask;

/**
 * Used with {@link EnumScheduledTask} to execute scheduled tasks.
 * 
 * @author MrJake
 */
public interface IScheduledTaskExecutor {
	
	/**
	 * Executes given task.
	 * 
	 * @param scheduledTask The task.
	 */
	public void executeTask(EnumScheduledTask scheduledTask);
}
