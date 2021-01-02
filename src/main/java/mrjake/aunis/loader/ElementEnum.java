package mrjake.aunis.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.loader.model.ModelLoader;
import mrjake.aunis.loader.texture.TextureLoader;
import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import net.minecraft.util.ResourceLocation;

public enum ElementEnum {
	
	// --------------------------------------------------------------------------------------------
	// Milky Way
	
	MILKYWAY_DHD("milkyway/DHD.obj", "milkyway/dhd"),
	MILKYWAY_GATE("milkyway/gate.obj", "milkyway/gatering7"),
	MILKYWAY_RING("milkyway/ring.obj", "milkyway/gatering7"),
	
	MILKYWAY_CHEVRON_LIGHT("milkyway/chevronLight.obj", "milkyway/chevron0"),
	MILKYWAY_CHEVRON_FRAME("milkyway/chevronFrame.obj", "milkyway/gatering7"),
	MILKYWAY_CHEVRON_MOVING("milkyway/chevronMoving.obj", "milkyway/chevron0"),
	MILKYWAY_CHEVRON_BACK("milkyway/chevronBack.obj", "milkyway/gatering7"),

	ORLIN_GATE("orlin/gate_orlin.obj", "orlin/gate_orlin", false), // TODO Mossy

	
	// --------------------------------------------------------------------------------------------
	// Universe
	
	UNIVERSE_GATE("universe/universe_gate.obj", "universe/universe_gate", false), // TODO Mossy
	UNIVERSE_CHEVRON("universe/universe_chevron.obj", "universe/universe_chevron10", false), // TODO Mossy
	UNIVERSE_DIALER("universe/universe_dialer.obj", "universe/universe_dialer"),
	
	
	// --------------------------------------------------------------------------------------------
	// Transport rings
	
	RINGS_BLACK("transportrings/rings_black.obj", "transportrings/rings_black"),
	RINGSCONTROLLER_GOAULD("transportrings/plate_goauld.obj", "transportrings/goauld_panel"),
	RINGSCONTROLLER_GOAULD_BUTTONS("transportrings/buttons_goauld.obj", "transportrings/goauld_buttons");

	
	// --------------------------------------------------------------------------------------------

	private boolean supportsMossy;
	public ResourceLocation modelResource;
	public Map<BiomeOverlayEnum, ResourceLocation> biomeTextureResourceMap = new HashMap<>();
	
	private ElementEnum(String model, String texture, boolean supportsMossy) {
		this.supportsMossy = supportsMossy;
		this.modelResource = ModelLoader.getModelResource(model);
				
		for (BiomeOverlayEnum biomeOverlay : BiomeOverlayEnum.values())
			biomeTextureResourceMap.put(biomeOverlay, TextureLoader.getTextureResource(texture + biomeOverlay.suffix + ".jpg"));
	}
	
	private ElementEnum(String model, String texture) {
		this(model, texture, true);
	}
	
	public void render() {
		ModelLoader.getModel(modelResource).render();
	}
	
	private List<BiomeOverlayEnum> nonExistingReported = new ArrayList<>();
	
	public void bindTexture(BiomeOverlayEnum biomeOverlay) {
		if (biomeOverlay == BiomeOverlayEnum.MOSSY && !supportsMossy)
			biomeOverlay = BiomeOverlayEnum.NORMAL;
		
		ResourceLocation resourceLocation = biomeTextureResourceMap.get(biomeOverlay);
		
		if (!TextureLoader.isTextureLoaded(resourceLocation)) {
			// Probably doesn't exist
			
			if (!nonExistingReported.contains(biomeOverlay)) {
				Aunis.logger.error(this + " tried to use BiomeOverlay " + biomeOverlay + " but it doesn't exist. ("+resourceLocation+")");
				nonExistingReported.add(biomeOverlay);
			}
			
			resourceLocation = biomeTextureResourceMap.get(BiomeOverlayEnum.NORMAL);
		}
		
		TextureLoader.getTexture(resourceLocation).bindTexture();
	}
	
	public void bindTextureAndRender(BiomeOverlayEnum biomeOverlay) {
		bindTexture(biomeOverlay);
		render();
	}
}
