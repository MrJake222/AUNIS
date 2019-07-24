package mrjake.aunis.renderer.state;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.state.State;

public class RendererGateActionState extends State {

	public static enum EnumGateAction {
		ACTIVATE_NEXT(1),
		ACTIVATE_FINAL(2),
		OPEN_GATE(3),
		CLOSE_GATE(4),
		GATE_DIAL_FAILED(5),
		LIGHT_UP_CHEVRONS(6);
		
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
		
		public static EnumGateAction valueOf(int actionID) {
			return map.get(actionID);
		}
	}
	
	public RendererGateActionState() {}
	
	public EnumGateAction action;
	public boolean computer;
	public int chevronCount;
	
	public RendererGateActionState(EnumGateAction action, boolean computer, int chevronCount) {
		this.action = action;
		this.computer = computer;
		this.chevronCount = chevronCount;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(action.actionID);
		buf.writeBoolean(computer);
		buf.writeInt(chevronCount);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		action = EnumGateAction.valueOf(buf.readInt());
		computer = buf.readBoolean();
		chevronCount = buf.readInt();
	}

}
