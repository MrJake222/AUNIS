package mrjake.aunis.tileentity.util;

import java.util.List;

import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Holds scheduled task to be performed some time in future on {@link ITickable#update()}.
 * 
 * Previously done by a shitload of variables in {@link StargateMilkyWayBaseTile} and other like "waitForShit", "shitStateChange".
 * 
 * @author MrJake222
 */
public class ScheduledTask implements INBTSerializable<NBTTagCompound> {
	
	/**
	 * {@link TileEntity} to perform {@link ScheduledTaskExecutorInterface#executeTask(EnumScheduledTask)} on.
	 */
	private ScheduledTaskExecutorInterface executor;
	
	/**
	 * When the {@link ScheduledTask} was created.
	 */
	private long taskCreated;
	
	/**
	 * Task to be called when the time will come. Also holds the wait time.
	 */
	private EnumScheduledTask scheduledTask;
	
	/**
	 * Is this {@link ScheduledTask} actively called from the {@link ITickable#update()}?
	 */
	private boolean active;
	
	/**
	 * User can use {@link ScheduledTask#ScheduledTask(ScheduledTaskExecutorInterface, long, EnumScheduledTask, int)} to set custom waiting time.
	 */
	private boolean customWaitTime;
	private int waitTime;

	/**
	 * User can pass custom data to the {@link ScheduledTaskExecutorInterface} instance.
	 */
	private NBTTagCompound customData = null;
	
	/**
	 * Main constructor
	 * 
	 * @param taskCreated When the {@link ScheduledTask} was created.
	 * @param scheduledTask Task to perform.
	 */
	public ScheduledTask(EnumScheduledTask scheduledTask) {
		this.scheduledTask = scheduledTask;
		this.active = true;
		
		this.customWaitTime = false;
	}
	
	public ScheduledTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		this(scheduledTask);
		this.customData = customData;
	}
	
	public ScheduledTask(EnumScheduledTask scheduledTask, int waitTime) {
		this(scheduledTask);
		
		this.customWaitTime = true;
		this.waitTime = waitTime;
	}
	
	public ScheduledTask(EnumScheduledTask scheduledTask, int waitTime, NBTTagCompound customData) {
		this(scheduledTask, waitTime);
		this.customData = customData;
	}
	
	public ScheduledTask(NBTTagCompound compound) {		
		deserializeNBT(compound);
	}
	
	/**
	 * Sets the executor for this task. Done in {@link ScheduledTaskExecutorInterface#addTask(ScheduledTask)}.
	 * 
	 * @param executor The {@link TileEntity}.
	 * @return 
	 * @return This instance.
	 */
	public ScheduledTask setExecutor(ScheduledTaskExecutorInterface executor) {
		this.executor = executor;
		
		return this;
	}
	
	/**
	 * Sets the time of creation. Done in {@link ScheduledTaskExecutorInterface#addTask(ScheduledTask)}.
	 * 
	 * @param taskCreated Usually {@link World#getTotalWorldTime()}.
	 */
	public void setTaskCreated(long taskCreated) {
		this.taskCreated = taskCreated;
	}
	
	/**
	 * Mark this {@link ScheduledTask} inactive.
	 * Prevents {@link ScheduledTask#activate(long, double)} from being called from the {@link ITickable#update()}.
	 * 
	 * @return This instance.
	 */
	public ScheduledTask inactive() {
		this.active = false;
		
		return this;
	}
	
	/**
	 * Mark this {@link ScheduledTask} active.
	 * @see Activation#inactive().
	 * 
	 * @return This instance.
	 */
	public ScheduledTask active() {
		this.active = true;
		
		return this;
	}
	
	/**
	 * Getter for active
	 * 
	 * @return active state.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Main waiting function. Call this in {@link ITickable#update()}.
	 * 
	 * @param worldTicks Usually {@link World#getTotalWorldTime()}.
	 * @return {@code True} if this {@link ScheduledTask} should be removed.
	 */
	public boolean update(long worldTicks) {
		int waitTime = customWaitTime ? this.waitTime : scheduledTask.waitTicks;
		long effTick = worldTicks - taskCreated;
		boolean call = effTick == waitTime;
		
		if (scheduledTask.overtime)
			call = effTick >= waitTime;
		
		if (call) {
			try {
//				Aunis.info("execute " + scheduledTask + " time: " + (worldTicks-taskCreated));
				executor.executeTask(scheduledTask, customData);
			}
			
			catch (UnsupportedOperationException e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setLong("taskCreated", taskCreated);
		compound.setInteger("scheduledTask", scheduledTask.id);
		compound.setBoolean("active", active);
		
		compound.setBoolean("customWaitTime", customWaitTime);
		compound.setInteger("waitTime", waitTime);
		
		if (customData != null)
			compound.setTag("customData", customData);
		
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		taskCreated = compound.getLong("taskCreated");
		scheduledTask = EnumScheduledTask.valueOf(compound.getInteger("scheduledTask"));
		active = compound.getBoolean("active");
		
		customWaitTime = compound.getBoolean("customWaitTime");
		waitTime = compound.getInteger("waitTime");
		
		if (compound.hasKey("customData"))
			customData = compound.getCompoundTag("customData");
	}
	
	@Override
	public String toString() {
		return scheduledTask.toString() + (customWaitTime ? ", custom time="+waitTime : "");
	}

	// Eclipse generated methods
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scheduledTask == null) ? 0 : scheduledTask.hashCode());
		result = prime * result + (int) (taskCreated ^ (taskCreated >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScheduledTask other = (ScheduledTask) obj;
		if (scheduledTask != other.scheduledTask)
			return false;
		if (taskCreated != other.taskCreated)
			return false;
		return true;
	}
	
	/**
	 * Static method to consolidate iterating through
	 * a set of {@link ScheduledTask}.
	 * 
	 * @param scheduledTasks {@link List} of {@link ScheduledTask}.
	 * @param worldTicks Usually {@link World#getTotalWorldTime()}.
	 */
	public static void iterate(List<ScheduledTask> scheduledTasks, long worldTicks) {
		for (int i=0; i<scheduledTasks.size();) {			
			ScheduledTask scheduledTask = scheduledTasks.get(i);
			
			if (scheduledTask.isActive()) {								
				if (scheduledTask.update(worldTicks))
					scheduledTasks.remove(scheduledTask);
				
				else i++;
			}
			
			else i++;
		}
	}
	
	public static NBTTagCompound serializeList(List<ScheduledTask> scheduledTasks) {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setInteger("size", scheduledTasks.size());
		for (int i=0; i<scheduledTasks.size(); i++)
			compound.setTag("scheduledTask"+i, scheduledTasks.get(i).serializeNBT());
		
		return compound;
	}
	
	public static void deserializeList(NBTTagCompound compound, List<ScheduledTask> scheduledTasks, ScheduledTaskExecutorInterface executor) {
		int size = compound.getInteger("size");
		for (int i=0; i<size; i++)
			scheduledTasks.add(new ScheduledTask(compound.getCompoundTag("scheduledTask"+i)).setExecutor(executor));
	}
}
