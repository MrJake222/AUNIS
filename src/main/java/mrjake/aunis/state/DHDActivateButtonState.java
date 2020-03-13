package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSymbol;

public class DHDActivateButtonState extends State {
	public DHDActivateButtonState() {}
	
	public EnumSymbol symbol;
	public boolean clearAll = false;

	public DHDActivateButtonState(boolean clearAll) {
		this.clearAll = clearAll;
	}
	
	public DHDActivateButtonState(EnumSymbol symbol) {
		this.symbol  = symbol;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(clearAll);
		
		if (!clearAll) {
			buf.writeInt(symbol.id);
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		clearAll = buf.readBoolean();
		
		if (!clearAll) {
			symbol = EnumSymbol.valueOf(buf.readInt());
		}
	}
}
