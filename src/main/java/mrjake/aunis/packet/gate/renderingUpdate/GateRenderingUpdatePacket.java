package mrjake.aunis.packet.gate.renderingUpdate;

import java.util.HashMap;
import java.util.Map;

public class GateRenderingUpdatePacket {
	
	public enum EnumPacket {
		DHD_RENDERER_UPDATE(0),
		GATE_RENDERER_UPDATE(1);
		//ENGAGE_GATE(2),
		
		//CLEAR_DHD_BUTTONS(3);
				
		public int packetID;
		private static Map<Integer, EnumPacket> map = new HashMap<Integer, EnumPacket>();
		
		EnumPacket(int packetID) {
			this.packetID = packetID;
		}
		
		static {
			for (EnumPacket packet : EnumPacket.values()) {
				map.put(packet.packetID, packet);
			}
		}
		
		public static EnumPacket valueOf(int packetID) {
			return map.get(packetID);
		}
	}
	
	public enum EnumGateAction {
		CLEAR(0),
		ACTIVATE_NEXT(1),
		ACTIVATE_FINAL(2),
		OPEN_GATE(3),
		CLOSE_GATE(4),
		GATE_DIAL_FAILED(5),
		LIGHT_UP_ALL_CHEVRONS(6);
		//OPEN_GATE_RECEIVING(7);
		
		public int actionID;
		private static Map<Integer, EnumGateAction> map = new HashMap<Integer, EnumGateAction>();
		
		private EnumGateAction(int actionID) {
			this.actionID = actionID;
		}
		
		static {
			for (EnumGateAction action : EnumGateAction.values()) {
				map.put(action.actionID, action);
			}
		}
		
		public static EnumGateAction valueOf(int packetID) {
			return map.get(packetID);
		}
	}
}
