package mrjake.aunis.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines {@link State} of which type we want to request from the server.
 * This will be sent to server. Then, appropriate state will be serialized and
 * returned to the client(Based on {@link StateProviderInterface#getState(StateTypeEnum)}). Deserialization will occur based on this Enum.
 * Must be unique within one TileEntity, but can be reused by multiple TileEntities.
 * 
 * @author MrJake
 *
 */
public enum StateTypeEnum {
	RENDERER_STATE(0),
	GUI_STATE(1),
	GUI_UPDATE(2),
	CAMO_STATE(3),
	LIGHT_STATE(4),
	ENERGY_STATE(5),
	SPIN_STATE(6),
	FLASH_STATE(7),
	DHD_ACTIVATE_BUTTON(8),
	RENDERER_UPDATE(9),
	SPARK_STATE(10),
	RINGS_START_ANIMATION(11),
	STARGATE_VAPORIZE_BLOCK_PARTICLES(12),
	STARGATE_UNIVERSE_ACTIVATE_SYMBOL(13),
	RENDERER_ACTION(14);
	
	public int id;
	
	private StateTypeEnum(int id) {
		this.id = id;
	} 
	
	private static Map<Integer, StateTypeEnum> ID_MAP = new HashMap<Integer, StateTypeEnum>();
	static {
		for (StateTypeEnum stateType : values())
			ID_MAP.put(stateType.id, stateType);
	}

	public static StateTypeEnum byId(int id) {
		return ID_MAP.get(id);
	}
}
