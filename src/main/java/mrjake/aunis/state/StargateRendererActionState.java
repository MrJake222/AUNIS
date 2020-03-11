package mrjake.aunis.state;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

public class StargateRendererActionState extends State {
	public static final StargateRendererActionState STARGATE_HORIZON_WIDEN_ACTION = new StargateRendererActionState(EnumGateAction.STARGATE_HORIZON_WIDEN);
	public static final StargateRendererActionState STARGATE_HORIZON_SHRINK_ACTION = new StargateRendererActionState(EnumGateAction.STARGATE_HORIZON_SHRINK);
	
	public static enum EnumGateAction {
		CHEVRON_ACTIVATE(1),
		OPEN_GATE(3),
		CLOSE_GATE(4),
		CLEAR_CHEVRONS(5),
		LIGHT_UP_CHEVRONS(6),
		STARGATE_HORIZON_WIDEN(7),	// Used for rendering
		STARGATE_HORIZON_SHRINK(8),	// Event horizon killing box
		CHEVRON_OPEN(9),
		CHEVRON_CLOSE(10),
		CHEVRON_ACTIVATE_BOTH(11),
		CHEVRON_DIM(12);
		
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
//	public boolean computer;
	public int chevronCount;
	public boolean modifyFinal;
	
	public StargateRendererActionState(EnumGateAction action) {
		this.action = action;
	}
	
	public StargateRendererActionState(EnumGateAction action, int chevronCount, boolean modifyFinal) {
		this.action = action;
//		this.computer = computer;
		this.chevronCount = chevronCount;
		this.modifyFinal = modifyFinal;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(action.actionID);
//		buf.writeBoolean(computer);
		buf.writeInt(chevronCount);
		buf.writeBoolean(modifyFinal);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		action = EnumGateAction.valueOf(buf.readInt());
//		computer = buf.readBoolean();
		chevronCount = buf.readInt();
		modifyFinal = buf.readBoolean();
	}

}
