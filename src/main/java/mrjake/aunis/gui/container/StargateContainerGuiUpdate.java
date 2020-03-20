package mrjake.aunis.gui.container;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.state.State;

public class StargateContainerGuiUpdate extends State {
	public StargateContainerGuiUpdate() {}
	
	public int energyStored;

	public StargateContainerGuiUpdate(int energyStored) {
		this.energyStored = energyStored;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energyStored);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		energyStored = buf.readInt();
	}

}
