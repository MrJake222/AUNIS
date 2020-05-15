package mrjake.aunis.renderer.stargate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;

public class StargateMilkyWayRendererState extends StargateClassicRendererState {
	public StargateMilkyWayRendererState() {}
	
	private StargateMilkyWayRendererState(StargateMilkyWayRendererStateBuilder builder) {
		super(builder);
		
		this.stargateSize = builder.stargateSize;
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
		return "milkyway/chevron";
	}
		
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(stargateSize.id);
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {	
		stargateSize = StargateSizeEnum.fromId(buf.readInt());
				
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
		
		public StargateMilkyWayRendererStateBuilder(StargateClassicRendererStateBuilder superBuilder) {
			super(superBuilder);
			setSymbolType(superBuilder.symbolType);
			setActiveChevrons(superBuilder.activeChevrons);
			setFinalActive(superBuilder.isFinalActive);
			setCurrentRingSymbol(superBuilder.currentRingSymbol);
			setSpinDirection(superBuilder.spinDirection);
			setSpinning(superBuilder.isSpinning);
			setTargetRingSymbol(superBuilder.targetRingSymbol);
			setSpinStartTime(superBuilder.spinStartTime);
		}
		
		public StargateMilkyWayRendererStateBuilder setStargateSize(StargateSizeEnum stargateSize) {
			this.stargateSize = stargateSize;
			return this;
		}

		@Override
		public StargateMilkyWayRendererState build() {
			return new StargateMilkyWayRendererState(this);
		}
	}
}