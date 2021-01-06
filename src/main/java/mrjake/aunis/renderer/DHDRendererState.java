package mrjake.aunis.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.DHDActivation;
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
	
	private static final Map<Integer, ResourceLocation> SYMBOL_RESOURCE_MAP = new HashMap<>();
	private static final Map<Integer, ResourceLocation> BRB_RESOURCE_MAP = new HashMap<>();
	
	static {
		for (int i=0; i<=5; i++) {
			SYMBOL_RESOURCE_MAP.put(i, new ResourceLocation(Aunis.ModID, SYMBOL_TEXTURE_BASE + i + ".jpg"));
			BRB_RESOURCE_MAP.put(i, new ResourceLocation(Aunis.ModID, BRB_TEXTURE_BASE + i + ".jpg"));
		}
	}
	
	public DHDRendererState(StargateAddressDynamic addressDialed, boolean brbActive) {
		this.addressDialed = addressDialed;
		this.brbActive = brbActive;
	}
	
	public DHDRendererState initClient(BlockPos pos, float horizontalRotation) {
		this.pos = pos;
		this.horizontalRotation = horizontalRotation;
		
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
	
	// Symbols
	// Not saved
	private final Map<SymbolMilkyWayEnum, Integer> BUTTON_STATE_MAP = new HashMap<>(38);
	public List<Activation<SymbolMilkyWayEnum>> activationList = new ArrayList<>();
	// Saved
	public StargateAddressDynamic addressDialed;
	public boolean brbActive;
	
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
	
	public ResourceLocation getButtonTexture(SymbolMilkyWayEnum symbol) {
		if (symbol.brb())
			return BRB_RESOURCE_MAP.get(BUTTON_STATE_MAP.get(symbol));

		return SYMBOL_RESOURCE_MAP.get(BUTTON_STATE_MAP.get(symbol));
	}
	
	
	public void toBytes(ByteBuf buf) {
		addressDialed.toBytes(buf);
		buf.writeBoolean(brbActive);
	}

	public void fromBytes(ByteBuf buf) {
		addressDialed = new StargateAddressDynamic(SymbolTypeEnum.MILKYWAY);		
		addressDialed.fromBytes(buf);
		brbActive = buf.readBoolean();
	}
}