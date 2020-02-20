package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumSymbol;

public class StargateSpinState extends State {
	public StargateSpinState() {}
	
	public EnumSymbol targetSymbol;
	public EnumSpinDirection direction;
	
	public StargateSpinState(EnumSymbol targetSymbol, EnumSpinDirection direction) {
		this.targetSymbol = targetSymbol;
		this.direction = direction;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(targetSymbol.id);
		buf.writeInt(direction.id);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {		
		targetSymbol = EnumSymbol.valueOf(buf.readInt());
		direction = EnumSpinDirection.valueOf(buf.readInt());
	}
}
