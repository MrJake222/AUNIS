package mrjake.aunis.stargate;

import java.util.HashMap;
import java.util.Map;
import mrjake.aunis.tileentity.TransportRingsTile;

public enum EnumScheduledTask {
	STARGATE_OPEN_SOUND(0, 25, false),
	STARGATE_ENGAGE(1, 86),
	STARGATE_CLOSE(2, 56),
	STARGATE_FAIL(3, 50),
	STARGATE_CHEVRON_SHUT_SOUND(4, 38, false),
	STARGATE_CHEVRON_OPEN_SOUND(5, 19, false),
	STARGATE_CHEVRON_LOCK_DHD_SOUND(6, 15, false),
	HORIZON_FLASH(7, -1, false), 
	STARGATE_ORLIN_OPEN(8, 144), // 8.93s(duration of dial sound) * 20(tps) − 25(STARGATE_OPEN_SOUND wait time)
	STARGATE_ORLIN_SPARK(9, 27, false),
	STARGATE_HORIZON_LIGHT_BLOCK(10, -1),
	STARGATE_HORIZON_WIDEN(11, -1, false),
	STARGATE_HORIZON_SHRINK(12, -1, false),
	RINGS_START_ANIMATION(13, 20),
	RINGS_FADE_OUT(14, TransportRingsTile.TIMEOUT_FADE_OUT),
	RINGS_TELEPORT(15, TransportRingsTile.TIMEOUT_TELEPORT),
	RINGS_CLEAR_OUT(15, TransportRingsTile.RINGS_CLEAR_OUT),
	RINGS_SOLID_BLOCKS(16, 20);
	
	public int id;
	public int waitTicks;
	
	/**
	 * Should the task be called on neatest occasion
	 * when the scheduled wait time exceeded?
	 */
	public boolean overtime;
	
	private EnumScheduledTask(int id, int waitTicks) {
		this(id, waitTicks, true);
	}
	
	private EnumScheduledTask(int id, int waitTicks, boolean overtime) {
		this.id = id;
		this.waitTicks = waitTicks;
		this.overtime = overtime;
	}
	
	@Override
	public String toString() {
		return this.name() + "[time=" + this.waitTicks+"]";
	}
	
	private static Map<Integer, EnumScheduledTask> idMap = new HashMap<>();
	static {
		for (EnumScheduledTask task : EnumScheduledTask.values())
			idMap.put(task.id, task);
	}
	
	public static EnumScheduledTask valueOf(int id) {
		return idMap.get(id);
	}
}
