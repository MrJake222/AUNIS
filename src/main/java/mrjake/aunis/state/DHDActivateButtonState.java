package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;

public class DHDActivateButtonState extends State {
	public DHDActivateButtonState() {}
	
	public SymbolMilkyWayEnum symbol;
	public boolean clearAll = false;

	public DHDActivateButtonState(boolean clearAll) {
		this.clearAll = clearAll;
	}
	
	public DHDActivateButtonState(SymbolMilkyWayEnum symbol) {
		this.symbol = symbol;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(clearAll);
		
		if (!clearAll) {
			buf.writeInt(symbol.getId());
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		clearAll = buf.readBoolean();
		
		if (!clearAll) {
			symbol = SymbolMilkyWayEnum.valueOf(buf.readInt());
		}
	}
}
