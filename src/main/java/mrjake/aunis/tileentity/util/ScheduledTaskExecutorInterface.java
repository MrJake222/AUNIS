package mrjake.aunis.tileentity.util;

import mrjake.aunis.stargate.EnumScheduledTask;

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
	 */
	public void executeTask(EnumScheduledTask scheduledTask);
}
