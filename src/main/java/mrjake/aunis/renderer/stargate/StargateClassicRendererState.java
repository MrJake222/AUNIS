package mrjake.aunis.renderer.stargate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.StargateClassicSpinHelper;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class StargateClassicRendererState extends StargateAbstractRendererState {
	public StargateClassicRendererState() {}
	
	public StargateClassicRendererState(StargateClassicRendererStateBuilder builder) {
		super(builder);
		
		this.chevronTextureList = new ChevronTextureList(getChevronTextureBase(), builder.activeChevrons, builder.isFinalActive);
		this.spinHelper = new StargateClassicSpinHelper(builder.symbolType, builder.currentRingSymbol, builder.spinDirection, builder.isSpinning, builder.targetRingSymbol, builder.spinStartTime);
		this.biomeOverride = builder.biomeOverride;
	}
	
	@Override
	public StargateAbstractRendererState initClient(BlockPos pos, EnumFacing facing, BiomeOverlayEnum biomeOverlay) {
		chevronTextureList.initClient();
		
		return super.initClient(pos, facing, biomeOverlay);
	}
	
	protected abstract String getChevronTextureBase();
	
	// Chevrons
	// Saved
	public ChevronTextureList chevronTextureList;
	
	// Spin		
	// Saved
	public StargateClassicSpinHelper spinHelper;
	
	
	// Biome override
	// Saved
	public BiomeOverlayEnum biomeOverride;
	
	@Override
	public BiomeOverlayEnum getBiomeOverlay() {
		if (biomeOverride != null)
			return biomeOverride;
		
		return super.getBiomeOverlay();
	}
	
	// ------------------------------------------------------------------------
	// Saving
	
	@Override
	public void toBytes(ByteBuf buf) {
		chevronTextureList.toBytes(buf);
		spinHelper.toBytes(buf);
		
		if (biomeOverride != null) {
			buf.writeBoolean(true);
			buf.writeInt(biomeOverride.ordinal());
		}
		
		else {
			buf.writeBoolean(false);
		}
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {			
		chevronTextureList = new ChevronTextureList(getChevronTextureBase());
		chevronTextureList.fromBytes(buf);
		
		spinHelper = new StargateClassicSpinHelper();
		spinHelper.fromBytes(buf);
		
		if (buf.readBoolean()) {
			biomeOverride = BiomeOverlayEnum.values()[buf.readInt()];
		}
		
		super.fromBytes(buf);
	}
	
	
	// ------------------------------------------------------------------------
	// Builder
	
	public static StargateClassicRendererStateBuilder builder() {
		return new StargateClassicRendererStateBuilder();
	}
	
	public static class StargateClassicRendererStateBuilder extends StargateAbstractRendererStateBuilder {
		public StargateClassicRendererStateBuilder() {}
		
		protected SymbolTypeEnum symbolType;
		
		// Chevrons
		protected int activeChevrons;
		protected boolean isFinalActive;
		
		// Spinning
		protected SymbolInterface currentRingSymbol;
		protected EnumSpinDirection spinDirection; 
		protected boolean isSpinning;
		protected SymbolInterface targetRingSymbol;
		protected long spinStartTime;
		
		// Biome override
		public BiomeOverlayEnum biomeOverride;
		
		public StargateClassicRendererStateBuilder(StargateAbstractRendererStateBuilder superBuilder) {
			setStargateState(superBuilder.stargateState);
		}
		
		public StargateClassicRendererStateBuilder setSymbolType(SymbolTypeEnum symbolType) {
			this.symbolType = symbolType;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setActiveChevrons(int activeChevrons) {
			this.activeChevrons = activeChevrons;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setFinalActive(boolean isFinalActive) {
			this.isFinalActive = isFinalActive;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setCurrentRingSymbol(SymbolInterface currentRingSymbol) {
			this.currentRingSymbol = currentRingSymbol;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setSpinDirection(EnumSpinDirection spinDirection) {
			this.spinDirection = spinDirection;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setSpinning(boolean isSpinning) {
			this.isSpinning = isSpinning;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setTargetRingSymbol(SymbolInterface targetRingSymbol) {
			this.targetRingSymbol = targetRingSymbol;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setSpinStartTime(long spinStartTime) {
			this.spinStartTime = spinStartTime;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setBiomeOverride(BiomeOverlayEnum biomeOverride) {
			this.biomeOverride = biomeOverride;
			return this;
		}
	}
}
