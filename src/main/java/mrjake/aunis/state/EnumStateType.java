package mrjake.aunis.state;

import mrjake.aunis.renderer.state.RendererState;

/**
 * Defines {@link RendererState} of which type we want to request from the server.
 * This will be sent to server. Then, appropriate state will be serialized and
 * returned to the client(Based on {@link ITileEntityStateProvider#getState(EnumStateType)}). Deserialization will occur based on this Enum.
 * 
 * @author MrJake
 *
 */
public enum EnumStateType {
	// This IDs MUST BE in order
	// Also NBT keys must be unique
	RENDERER_STATE(0),
	UPGRADE_RENDERER_STATE(1),
	GUI_STATE(2),
	CAMO_STATE(3),
	LIGHT_STATE(4),
	ENERGY_STATE(5),
	SPIN_STATE(6),
	FLASH_STATE(7),
	DHD_ACTIVATE_BUTTON(8),
	RENDERER_UPDATE(9);
	
	public int id;
	
	private EnumStateType(int id) {
		this.id = id;
	} 

	public static EnumStateType byId(int id) {
		return EnumStateType.values()[id];
	}
}
