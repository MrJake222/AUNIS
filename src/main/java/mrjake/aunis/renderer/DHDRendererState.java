package mrjake.aunis.renderer;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.DHDActivation;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.state.State;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDRendererState extends State {
	public DHDRendererState() {}
	
	private static final String SYMBOL_TEXTURE_BASE = "dhd/symbol/symbol";
	private static final String BRB_TEXTURE_BASE = "dhd/brb/brb";
	
	public DHDRendererState(List<Integer> activeSymbols) {
		this.activeSymbols = activeSymbols;
	}
	
	public DHDRendererState initClient(BlockPos pos, float horizontalRotation) {
		this.pos = pos;
		this.horizontalRotation = horizontalRotation;
		
		for (int i=0; i<38; i++)
			buttonTextureList.add(SYMBOL_TEXTURE_BASE + (activeSymbols.contains(i) ? "5.png" : "0.png"));
		
		buttonTextureList.add(BRB_TEXTURE_BASE + (activeSymbols.contains(EnumSymbol.BRB.id) ? "5.png" : "0.png"));
		
		return this;
	}
	
	
	// Global
	// Not saved
	public BlockPos pos;
	public float horizontalRotation;
	
	// Symbols
	// Not saved
	public List<String> buttonTextureList = new ArrayList<>(38);
	public List<Activation> activationList = new ArrayList<>();
	// Saved
	public List<Integer> activeSymbols = new ArrayList<Integer>();	
	
	private boolean isSymbolActiveClientSide(int id) {
		return !buttonTextureList.get(id).contains("0.png");
	}
	
	public void clearSymbols(long totalWorldTime) {
		for (int id=0; id<=38; id++) {			
			if (isSymbolActiveClientSide(id)) {
				activationList.add(new DHDActivation(id, totalWorldTime, true));
			}
		}
	}
	
	public void activateSymbol(long totalWorldTime, EnumSymbol symbol) {
		activationList.add(new DHDActivation(symbol.id, totalWorldTime));
	}
	
	public void iterate(World world, double partialTicks) {
		Activation.iterate(activationList, world.getTotalWorldTime(), partialTicks, (index, stage) -> {
			if (index == EnumSymbol.BRB.id)
				buttonTextureList.set(index, BRB_TEXTURE_BASE + stage + ".png");
			else
				buttonTextureList.set(index, SYMBOL_TEXTURE_BASE + stage + ".png");
		});
	}
	
	
	public void toBytes(ByteBuf buf) {		
		int size = activeSymbols.size();
		buf.writeInt(size);
		
		for (int i=0; i<size; i++) {
			buf.writeInt(activeSymbols.get(i));
		}	
	}

	public void fromBytes(ByteBuf buf) {		
		int size = buf.readInt();
		activeSymbols.clear();
		
		for (int i=0; i<size; i++) {
			activeSymbols.add(buf.readInt());
		}
	}
	
//	public DHDRendererState() {}
//	
//	public List<Integer> activeButtons = new ArrayList<Integer>();
//	
//	public DHDRendererState(List<Integer> activeButtons) {
//		this.activeButtons = activeButtons;
//	}
//	

}