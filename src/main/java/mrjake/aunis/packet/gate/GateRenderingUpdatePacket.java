package mrjake.aunis.packet.gate;

public class GateRenderingUpdatePacket {
	
	public enum EnumPacket {
		DHDButton(0),
		Chevron(1),
		ENGAGE_GATE(2),
		CLOSE_GATE(3);
		
		public int packetID;
		
		EnumPacket(int packetID) {
			this.packetID = packetID;
		}
	}
	
	public enum EnumGateAction {
		CLEAR(0),
		ACTIVATE_NEXT(1),
		ACTIVATE_FINAL(2),
		OPEN_GATE(3),
		CLOSE_GATE(4);
		
		public int actionID;
		
		private EnumGateAction(int actionID) {
			this.actionID = actionID;
		}
	}
}
