package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSymbol;

public class StargateSpinState extends SpinState {
	
	/**
	 * Symbol being locked or engaged.
	 */
	public EnumSymbol targetSymbol = null;
	
	/**
	 * Indicates if final chevron lock sound has been played
	 */
	public boolean lockSoundPlayed = false;
	
	/**
	 * If the ring is spinned by a computer
	 */
	public boolean computerInitializedStop = false;
	
	/**
	 * If it's the final chevron or not
	 */
	public boolean finalChevron = false;
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(targetSymbol != null ? targetSymbol.id : -1);
		buf.writeBoolean(lockSoundPlayed);
		buf.writeBoolean(computerInitializedStop);
		buf.writeBoolean(finalChevron);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		int targetSymbolId = buf.readInt();
		if (targetSymbolId != -1)
			targetSymbol = EnumSymbol.valueOf(targetSymbolId);
		
		lockSoundPlayed = buf.readBoolean();
		computerInitializedStop = buf.readBoolean();
		finalChevron = buf.readBoolean();
	}
}
