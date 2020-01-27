package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumSymbol;

public class StargateSpinStateRequest extends State {
	public StargateSpinStateRequest() {}
	
	public EnumSpinDirection direction = EnumSpinDirection.COUNTER_CLOCKWISE;
	public EnumSymbol targetSymbol = null;
	public boolean lock = false;
	public boolean moveOnly;
	
	public StargateSpinStateRequest(EnumSpinDirection direction, EnumSymbol targetSymbol, boolean lock, boolean moveOnly) {
		this.direction = direction;
		this.targetSymbol = targetSymbol;
		this.lock = lock;
		this.moveOnly = moveOnly;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(direction.id);
		
		buf.writeInt(targetSymbol != null ? targetSymbol.id : -1);
		
		buf.writeBoolean(lock);
		buf.writeBoolean(moveOnly);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		direction = EnumSpinDirection.valueOf(buf.readInt());
		
		int targetSymbolId = buf.readInt();
		if (targetSymbolId != -1)
			targetSymbol = EnumSymbol.valueOf(targetSymbolId);
		
		lock = buf.readBoolean();
		moveOnly = buf.readBoolean();
	}

}
