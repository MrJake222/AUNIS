package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.beamer.BeamerRendererAction;

public class BeamerRendererActionState extends State {
	public BeamerRendererActionState() {}
	
	public BeamerRendererAction action;
	
	public BeamerRendererActionState(BeamerRendererAction action) {
		this.action = action;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(action.id);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		action = BeamerRendererAction.valueOf(buf.readInt());
	}
}
