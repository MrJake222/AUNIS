package mrjake.aunis.state;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

public class StargateRendererActionState extends State {
	public static final StargateRendererActionState STARGATE_HORIZON_WIDEN_ACTION = new StargateRendererActionState(EnumGateAction.STARGATE_HORIZON_WIDEN, false, -1);
	public static final StargateRendererActionState STARGATE_HORIZON_SHRINK_ACTION = new StargateRendererActionState(EnumGateAction.STARGATE_HORIZON_SHRINK, false, -1);
	
	public static enum EnumGateAction {
		ACTIVATE_NEXT(1),
		ACTIVATE_FINAL(2),
		OPEN_GATE(3),
		CLOSE_GATE(4),
		GATE_DIAL_FAILED(5),
		LIGHT_UP_CHEVRONS(6),
		STARGATE_HORIZON_WIDEN(7),	// Used for rendering
		STARGATE_HORIZON_SHRINK(8);	// Event horizon killing box
		
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
	
	public StargateRendererActionState() {}
	
	public EnumGateAction action;
	public boolean computer;
	public int chevronCount;
	
	public StargateRendererActionState(EnumGateAction action, boolean computer, int chevronCount) {
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
