package mrjake.aunis.renderer.stargate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateSpinHelper;

public class StargateMilkyWayRendererState extends StargateClassicRendererState {
	public StargateMilkyWayRendererState() {}
	
	public StargateMilkyWayRendererState(StargateMilkyWayRendererStateBuilder builder) {
		super(builder);
		
		this.stargateSize = builder.stargateSize;
		this.spinHelper = new StargateSpinHelper(builder.currentRingSymbol, builder.spinDirection, builder.isSpinning, builder.targetRingSymbol, builder.spinStartTime);
	}
		
	// Gate
	// Saved
	public StargateSizeEnum stargateSize = AunisConfig.stargateSize;
	
	// Chevrons
	// Not saved
	public boolean chevronOpen;
	public long chevronActionStart;
	public boolean chevronOpening;
	public boolean chevronClosing;
	
	public void openChevron(long totalWorldTime) {
		chevronActionStart = totalWorldTime;
		chevronOpening = true;
	}
	
	public void closeChevron(long totalWorldTime) {
		chevronActionStart = totalWorldTime;
		chevronClosing = true;
	}
	
	// Ring		
	// Saved
	public StargateSpinHelper spinHelper;
		
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(stargateSize.id);
		spinHelper.toBytes(buf);
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {	
		stargateSize = StargateSizeEnum.fromId(buf.readInt());
		
		spinHelper = new StargateSpinHelper();
		spinHelper.fromBytes(buf);
				
		super.fromBytes(buf);
	}
	
	
	// ------------------------------------------------------------------------
	// Builder
	
	public static StargateMilkyWayRendererStateBuilder builder() {
		return new StargateMilkyWayRendererStateBuilder();
	}
	
	public static class StargateMilkyWayRendererStateBuilder extends StargateClassicRendererStateBuilder {
		
		private StargateSizeEnum stargateSize;
		private EnumSymbol currentRingSymbol;
		private EnumSpinDirection spinDirection; 
		private boolean isSpinning;
		private EnumSymbol targetRingSymbol;
		private long spinStartTime;
		
		public StargateMilkyWayRendererStateBuilder setStargateSize(StargateSizeEnum stargateSize) {
			this.stargateSize = stargateSize;
			return this;
		}
		
		public StargateMilkyWayRendererStateBuilder setCurrentRingSymbol(EnumSymbol currentRingSymbol) {
			this.currentRingSymbol = currentRingSymbol;
			return this;
		}
		
		public StargateMilkyWayRendererStateBuilder setSpinDirection(EnumSpinDirection spinDirection) {
			this.spinDirection = spinDirection;
			return this;
		}
		
		public StargateMilkyWayRendererStateBuilder setSpinning(boolean isSpinning) {
			this.isSpinning = isSpinning;
			return this;
		}
		
		public StargateMilkyWayRendererStateBuilder setTargetRingSymbol(EnumSymbol targetRingSymbol) {
			this.targetRingSymbol = targetRingSymbol;
			return this;
		}
		
		public StargateMilkyWayRendererStateBuilder setSpinStartTime(long spinStartTime) {
			this.spinStartTime = spinStartTime;
			return this;
		}

		@Override
		public StargateMilkyWayRendererState build() {
			return new StargateMilkyWayRendererState(this);
		}
	}
}