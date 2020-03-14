package mrjake.aunis.gui;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.state.State;
import mrjake.aunis.tileentity.util.ReactorStateEnum;

public class DHDContainerGuiState extends State {
	public DHDContainerGuiState() {}
	
	public int fluidAmount;
	public int tankCapacity;
	public ReactorStateEnum reactorState;
	
	public DHDContainerGuiState(int fluidAmount, int tankCapacity, ReactorStateEnum reactorState) {
		this.fluidAmount = fluidAmount;
		this.tankCapacity = tankCapacity;
		this.reactorState = reactorState;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(fluidAmount);
		buf.writeInt(tankCapacity);
		buf.writeShort(reactorState.getId());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		fluidAmount = buf.readInt();
		tankCapacity = buf.readInt();
		reactorState = ReactorStateEnum.valueOf(buf.readShort());
	}

}
