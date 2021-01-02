package mrjake.aunis.renderer.stargate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.UniverseActivation;
import mrjake.aunis.stargate.network.StargateAddressDynamic;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateUniverseRendererState extends StargateClassicRendererState {
	public StargateUniverseRendererState() {}
	
	public StargateUniverseRendererState(StargateUniverseRendererStateBuilder builder) {
		super(builder);
		
		this.dialedAddress = builder.dialedAddress;
	}

	@Override
	public StargateAbstractRendererState initClient(BlockPos pos, EnumFacing facing) {
		for (SymbolUniverseEnum symbol : SymbolUniverseEnum.values()) {
			symbolStateMap.put(symbol, getFloatValue(dialedAddress.contains(symbol)));
		}
		
		return super.initClient(pos, facing);
	}
	
	@Override
	protected String getChevronTextureBase() {
		return "universe/universe_chevron";
	}
	
	
	// ------------------------------------------------------------------------
	// Saving
	
	// Symbols
	// Saved
	private StargateAddressDynamic dialedAddress;
	// Not saved
	private final Map<SymbolUniverseEnum, Float> symbolStateMap = new HashMap<>(36);
	private final List<Activation<SymbolUniverseEnum>> activationList = new ArrayList<>();
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		dialedAddress.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		dialedAddress = new StargateAddressDynamic(SymbolTypeEnum.UNIVERSE);
		dialedAddress.fromBytes(buf);
	}
	
	private boolean isSymbolActiveClientSide(SymbolUniverseEnum symbol) {
		return symbolStateMap.get(symbol) > 0;
	}
	
	public void clearSymbols(long totalWorldTime) {
		for (SymbolUniverseEnum symbol : SymbolUniverseEnum.values()) {
			if (isSymbolActiveClientSide(symbol)) {
				activationList.add(new UniverseActivation(symbol, totalWorldTime, true));
			}
		}
	}
	
	public void activateSymbol(long totalWorldTime, SymbolUniverseEnum symbol) {
		activationList.add(new UniverseActivation(symbol, totalWorldTime, false));
	}
	
	public void iterate(World world, double partialTicks) {
		Activation.iterate(activationList, world.getTotalWorldTime(), partialTicks, (index, stage) -> {
			symbolStateMap.put(index, stage);
		});
	}
	
	public float getSymbolColor(SymbolUniverseEnum symbol) {
		return symbolStateMap.get(symbol);
	}
	
	
	// ------------------------------------------------------------------------
	// Static
	
	public static float getFloatValue(boolean isActive) {
		return isActive ? 0.6f : 0.0f;
	}
	
	
	// ------------------------------------------------------------------------
	// Builder
	
	public static StargateUniverseRendererStateBuilder builder() {
		return new StargateUniverseRendererStateBuilder();
	}
	
	public static class StargateUniverseRendererStateBuilder extends StargateClassicRendererStateBuilder {
		public StargateUniverseRendererStateBuilder() {}
		
		private StargateAddressDynamic dialedAddress;
		
		public StargateUniverseRendererStateBuilder(StargateClassicRendererStateBuilder superBuilder) {
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
		
		public StargateUniverseRendererStateBuilder setDialedAddress(StargateAddressDynamic dialedAddress) {
			this.dialedAddress = dialedAddress;
			return this;
		}
		
		@Override
		public StargateAbstractRendererState build() {
			return new StargateUniverseRendererState(this);
		}
	}
}
