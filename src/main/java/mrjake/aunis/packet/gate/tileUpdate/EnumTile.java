package mrjake.aunis.packet.gate.tileUpdate;

import mrjake.aunis.renderer.RendererState;
import mrjake.aunis.renderer.StargateRendererState;

public enum EnumTile {
	GATE_TILE(0),
	DHD_TILE(1);
		
	public int id;
		
	EnumTile(int id) {
		this.id = id;
	}

	public static int fromObject(RendererState rendererState) {
		if (rendererState instanceof StargateRendererState)
			return GATE_TILE.id;
		else
			return DHD_TILE.id;
	}
		
	public static EnumTile fromInt(int readInt) {
		return values()[readInt];
	}
}

