package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;

public class StargateSpinState extends State {
	public StargateSpinState() {}
	
	public SymbolInterface targetSymbol;
	public EnumSpinDirection direction;
	
	public StargateSpinState(SymbolInterface targetSymbol, EnumSpinDirection direction) {
		this.targetSymbol = targetSymbol;
		this.direction = direction;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(targetSymbol.getSymbolType().id);
		buf.writeInt(targetSymbol.getId());
		buf.writeInt(direction.id);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(buf.readInt());
		targetSymbol = symbolType.valueOfSymbol(buf.readInt());
		direction = EnumSpinDirection.valueOf(buf.readInt());
	}
}
