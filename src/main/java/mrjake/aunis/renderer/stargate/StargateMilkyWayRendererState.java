package mrjake.aunis.renderer.stargate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.StargateClassicSpinHelper;
import mrjake.aunis.stargate.StargateMilkyWaySpinHelper;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;

public class StargateMilkyWayRendererState extends StargateClassicRendererState {
	public StargateMilkyWayRendererState() {}
	
	private StargateMilkyWayRendererState(StargateMilkyWayRendererStateBuilder builder) {
		super(builder);
		
		this.stargateSize = builder.stargateSize;
		this.spinHelper = new StargateMilkyWaySpinHelper(builder.currentRingSymbol, builder.spinDirection, builder.isSpinning, builder.targetRingSymbol, builder.spinStartTime);
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
	
	@Override
	protected String getChevronTextureBase() {
		return "textures/tesr/milkyway/chevron";
	}
	
	// Ring		
	// Saved
	public StargateClassicSpinHelper<SymbolMilkyWayEnum> spinHelper;
		
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(stargateSize.id);
		spinHelper.toBytes(buf);
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {	
		stargateSize = StargateSizeEnum.fromId(buf.readInt());
		
		spinHelper = new StargateMilkyWaySpinHelper();
		spinHelper.fromBytes(buf);
				
		super.fromBytes(buf);
	}
	
	
	// ------------------------------------------------------------------------
	// Builder
	
	public static StargateMilkyWayRendererStateBuilder builder() {
		return new StargateMilkyWayRendererStateBuilder();
	}
	
	public static class StargateMilkyWayRendererStateBuilder extends StargateClassicRendererStateBuilder {
		public StargateMilkyWayRendererStateBuilder() {}
		
		private StargateSizeEnum stargateSize;
		private SymbolMilkyWayEnum currentRingSymbol;
		private EnumSpinDirection spinDirection; 
		private boolean isSpinning;
		private SymbolMilkyWayEnum targetRingSymbol;
		private long spinStartTime;
		
		public StargateMilkyWayRendererStateBuilder(StargateClassicRendererStateBuilder superBuilder) {
			super(superBuilder);
			setActiveChevrons(superBuilder.activeChevrons);
			setFinalActive(superBuilder.isFinalActive);
		}
		
		public StargateMilkyWayRendererStateBuilder setStargateSize(StargateSizeEnum stargateSize) {
			this.stargateSize = stargateSize;
			return this;
		}
		
		public StargateMilkyWayRendererStateBuilder setCurrentRingSymbol(SymbolMilkyWayEnum currentRingSymbol) {
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
		
		public StargateMilkyWayRendererStateBuilder setTargetRingSymbol(SymbolMilkyWayEnum targetRingSymbol) {
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