package mrjake.aunis.packet.gate.tileUpdate;

import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.renderer.state.LimitedStargateRendererState;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.StargateRendererState;

public enum EnumTile {
	GATE_TILE(0),
	LIMITED_GATE_TILE(1),
	DHD_TILE(2);
		
	public int id;
		
	EnumTile(int id) {
		this.id = id;
	}

	public static int fromObject(RendererState rendererState) {
		if (rendererState instanceof StargateRendererState)
			return GATE_TILE.id;
		else if (rendererState instanceof LimitedStargateRendererState)
			return LIMITED_GATE_TILE.id;
		else if (rendererState instanceof DHDRendererState)
			return DHD_TILE.id;
		
		return -1;
	}
		
	public static EnumTile fromInt(int readInt) {
		return values()[readInt];
	}
}

