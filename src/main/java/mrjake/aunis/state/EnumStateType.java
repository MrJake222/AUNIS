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
	RENDERER_STATE(0, "rendererState"),
	UPGRADE_RENDERER_STATE(1, "upgradeRendererState"),
	GUI_STATE(2, "guiState"),
	CAMO_STATE(3, "camoState"),
	LIGHT_STATE(4, "lightState");
	
	public int id;
	private String key;
	
	private EnumStateType(int id, String key) {
		this.id = id;
		this.key = key;
	} 

	public static EnumStateType byId(int id) {
		return EnumStateType.values()[id];
	}

	/**
	 * Gets NBT key
	 * 
	 * @return NBT key
	 */
	public String getKey() {
		return key;
	}
}
