package mrjake.aunis.renderer;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.DHDActivation;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.World;

public class DHDRenderer {
	private World world;
	
	private float horizontalRotation;
	
	public DHDRenderer(DHDTile te) {
		this.horizontalRotation = te.getWorld().getBlockState(te.getPos()).getValue(AunisProps.ROTATION_HORIZONTAL) * -22.5f;
		this.world = te.getWorld();
		
		// Load DHD textures
		for (int i=0; i<6; i++) {
			ModelLoader.getTexture(SYMBOL_TEXTURE_BASE + i + ".png");
			ModelLoader.getTexture(BRB_TEXTURE_BASE + i + ".png");
		}
		
		for (int i=0; i<38; i++)
			buttonTextureList.add(SYMBOL_TEXTURE_BASE + "0.png");
		
		buttonTextureList.add(BRB_TEXTURE_BASE + "0.png");
	}
	
	private List<Integer> getActiveSymbols() {
		List<Integer> out = new ArrayList<>();
		
		for (int i=0; i<buttonTextureList.size(); i++) {			
			if (!buttonTextureList.get(i).contains("0.png")) {
				out.add(i);
			}
		}
				
		return out;
	}
	
	private static final String SYMBOL_TEXTURE_BASE = "dhd/symbol/symbol";
	private static final String BRB_TEXTURE_BASE = "dhd/brb/brb";
	
	private List<String> buttonTextureList = new ArrayList<>(38);
	private List<Activation> activationList = new ArrayList<>();
	
	public void changeSymbols(List<Integer> idList, boolean dim) {
		for (int id : idList) {
			activationList.add(new DHDActivation(id, world.getTotalWorldTime(), dim));
		}
	}
	
	public void activateSymbols(List<Integer> idList) {
		changeSymbols(idList, false);
	}
	
	public void clearSymbols() {
		changeSymbols(getActiveSymbols(), true);
	}
	
	public void render(double x, double y, double z, double partialTicks) {
		Model dhdModel = ModelLoader.getModel(EnumModel.DHD_MODEL);
		Model brbModel = ModelLoader.getModel(EnumModel.BRB);
		
		if (dhdModel != null && brbModel != null) {	
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x+0.5, y, z+0.5);
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
						
			EnumModel.DHD_MODEL.bindTexture();
			dhdModel.render();
			
			ModelLoader.bindTexture(buttonTextureList.get(EnumSymbol.BRB.id));
			brbModel.render();
			
			for (EnumSymbol symbol : EnumSymbol.values()) {
				Model buttonModel = ModelLoader.getModel(EnumModel.getModelForSymbol(symbol));
				
				if (buttonModel != null) {
					ModelLoader.bindTexture(buttonTextureList.get(symbol.id));
					buttonModel.render();
				}
			}
			
			GlStateManager.popMatrix();
			
			// -----------------------------------------------------------------
			Activation.iterate(activationList, world.getTotalWorldTime(), partialTicks, (index, stage) -> {
				if (index == EnumSymbol.BRB.id)
					buttonTextureList.set(index, BRB_TEXTURE_BASE + stage + ".png");
				else
					buttonTextureList.set(index, SYMBOL_TEXTURE_BASE + stage + ".png");
			});
		}
	}
}
