package mrjake.aunis.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.DHDActivation;
import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import mrjake.aunis.stargate.network.StargateAddressDynamic;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.state.State;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDRendererState extends State {
	public DHDRendererState() {}
	
	private static final String SYMBOL_TEXTURE_BASE = "textures/tesr/milkyway/symbol";
	private static final String BRB_TEXTURE_BASE = "textures/tesr/milkyway/brb";
	
	private static final Map<BiomeOverlayEnum, TextureContainer> BIOME_TEXTURE_MAP = new HashMap<>();
	
	private static class TextureContainer {
		public final Map<Integer, ResourceLocation> SYMBOL_RESOURCE_MAP = new HashMap<>();
		public final Map<Integer, ResourceLocation> BRB_RESOURCE_MAP = new HashMap<>();
	}
	
	static {
		for (BiomeOverlayEnum biomeOverlay : BiomeOverlayEnum.values()) {
			TextureContainer container = new TextureContainer();
			
			for (int i=0; i<=5; i++) {
				container.SYMBOL_RESOURCE_MAP.put(i, new ResourceLocation(Aunis.ModID, SYMBOL_TEXTURE_BASE + i + biomeOverlay.suffix + ".jpg"));
				container.BRB_RESOURCE_MAP.put(i, new ResourceLocation(Aunis.ModID, BRB_TEXTURE_BASE + i + biomeOverlay.suffix + ".jpg"));
			}
			
			BIOME_TEXTURE_MAP.put(biomeOverlay, container);
		}
	}
	
	public DHDRendererState(StargateAddressDynamic addressDialed, boolean brbActive, BiomeOverlayEnum biomeOverride) {
		this.addressDialed = addressDialed;
		this.brbActive = brbActive;
		this.biomeOverride = biomeOverride;
	}
	
	public DHDRendererState initClient(BlockPos pos, float horizontalRotation, BiomeOverlayEnum biomeOverlay) {
		this.pos = pos;
		this.horizontalRotation = horizontalRotation;
		this.biomeOverlay = biomeOverlay;
		
		for (SymbolMilkyWayEnum symbol : SymbolMilkyWayEnum.values()) {			
			if (symbol.brb())
				BUTTON_STATE_MAP.put(symbol, brbActive ? 5 : 0);
			else
				BUTTON_STATE_MAP.put(symbol, addressDialed.contains(symbol) ? 5 : 0);	
		}
		
		return this;
	}
	
	
	// Global
	// Not saved
	public BlockPos pos;
	public float horizontalRotation;
	private BiomeOverlayEnum biomeOverlay;
	
	// Symbols
	// Not saved
	private final Map<SymbolMilkyWayEnum, Integer> BUTTON_STATE_MAP = new HashMap<>(38);
	public List<Activation<SymbolMilkyWayEnum>> activationList = new ArrayList<>();
	// Saved
	public StargateAddressDynamic addressDialed;
	public boolean brbActive;
	
	// Biome Override
	// Saved
	public BiomeOverlayEnum biomeOverride;
	
	public BiomeOverlayEnum getBiomeOverlay() {
		if (biomeOverride != null)
			return biomeOverride;
		
		return biomeOverlay;
	}
	
	public void setBiomeOverlay(BiomeOverlayEnum biomeOverlay) {
		this.biomeOverlay = biomeOverlay;
	}
	
	private boolean isSymbolActiveClientSide(SymbolMilkyWayEnum symbol) {
		return BUTTON_STATE_MAP.get(symbol) != 0;
	}
	
	public void clearSymbols(long totalWorldTime) {
		for (SymbolMilkyWayEnum symbol : SymbolMilkyWayEnum.values()) {
			if (isSymbolActiveClientSide(symbol)) {
				activationList.add(new DHDActivation(symbol, totalWorldTime, true));
			}
		}
	}
	
	public void activateSymbol(long totalWorldTime, SymbolMilkyWayEnum symbol) {
		activationList.add(new DHDActivation(symbol, totalWorldTime, false));
	}
	
	public void iterate(World world, double partialTicks) {
		Activation.iterate(activationList, world.getTotalWorldTime(), partialTicks, (index, stage) -> {
			BUTTON_STATE_MAP.put(index, Math.round(stage));
		});
	}
	
	public ResourceLocation getButtonTexture(SymbolMilkyWayEnum symbol, BiomeOverlayEnum biomeOverlay) {
		TextureContainer container = BIOME_TEXTURE_MAP.get(biomeOverlay);
		
		if (symbol.brb())
			return container.BRB_RESOURCE_MAP.get(BUTTON_STATE_MAP.get(symbol));

		return container.SYMBOL_RESOURCE_MAP.get(BUTTON_STATE_MAP.get(symbol));
	}
	
	
	public void toBytes(ByteBuf buf) {
		addressDialed.toBytes(buf);
		buf.writeBoolean(brbActive);
		
		if (biomeOverride != null) {
			buf.writeBoolean(true);
			buf.writeInt(biomeOverride.ordinal());
		}
		
		else {
			buf.writeBoolean(false);
		}
	}

	public void fromBytes(ByteBuf buf) {
		addressDialed = new StargateAddressDynamic(SymbolTypeEnum.MILKYWAY);		
		addressDialed.fromBytes(buf);
		brbActive = buf.readBoolean();
		
		if (buf.readBoolean()) {
			biomeOverride = BiomeOverlayEnum.values()[buf.readInt()];
		}
	}
}