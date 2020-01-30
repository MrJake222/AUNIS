package mrjake.aunis.state;

/**
 * Defines {@link State} of which type we want to request from the server.
 * This will be sent to server. Then, appropriate state will be serialized and
 * returned to the client(Based on {@link StateProviderInterface#getState(StateTypeEnum)}). Deserialization will occur based on this Enum.
 * Must be unique withing one TileEntity, but can be reused by multiple TileEntities.
 * 
 * @author MrJake
 *
 */
public enum StateTypeEnum {
	// This IDs MUST BE in order
	RENDERER_STATE(0),
	UPGRADE_RENDERER_STATE(1),
	GUI_STATE(2),
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
	STARGATE_RING_STOP(13);
	
	public int id;
	
	private StateTypeEnum(int id) {
		this.id = id;
	} 

	public static StateTypeEnum byId(int id) {
		return StateTypeEnum.values()[id];
	}
}
