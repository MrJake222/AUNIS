package mrjake.aunis.loader;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.loader.model.ModelLoader;
import mrjake.aunis.loader.texture.TextureLoader;
import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import net.minecraft.util.ResourceLocation;

public enum ElementEnum {
	
	// --------------------------------------------------------------------------------------------
	// Milky Way
	
	MILKYWAY_DHD("milkyway/DHD.obj", "milkyway/dhd", true),
	MILKYWAY_GATE("milkyway/gate.obj", "milkyway/gatering7", true),
	MILKYWAY_RING("milkyway/ring.obj", "milkyway/gatering7", true),
	
	MILKYWAY_CHEVRON_LIGHT("milkyway/chevronLight.obj", "milkyway/chevron0", true),
	MILKYWAY_CHEVRON_FRAME("milkyway/chevronFrame.obj", "milkyway/gatering7", true),
	MILKYWAY_CHEVRON_MOVING("milkyway/chevronMoving.obj", "milkyway/chevron0", true),
	MILKYWAY_CHEVRON_BACK("milkyway/chevronBack.obj", "milkyway/gatering7", true),

	ORLIN_GATE("orlin/gate_orlin.obj", "orlin/gate_orlin"),

	
	// --------------------------------------------------------------------------------------------
	// Universe
	
	UNIVERSE_GATE("universe/universe_gate.obj", "universe/universe_gate"),
	UNIVERSE_CHEVRON("universe/universe_chevron.obj", "universe/universe_chevron10"),
	UNIVERSE_DIALER("universe/universe_dialer.obj", "universe/universe_dialer"),
	
	
	// --------------------------------------------------------------------------------------------
	// Transport rings
	
	RINGS_BLACK("transportrings/rings_black.obj", "transportrings/rings_black"),
	RINGSCONTROLLER_GOAULD("transportrings/plate_goauld.obj", "transportrings/goauld_panel"),
	RINGSCONTROLLER_GOAULD_BUTTONS("transportrings/buttons_goauld.obj", "transportrings/goauld_buttons");

	
	// --------------------------------------------------------------------------------------------

	private boolean supportsFrost;
	public ResourceLocation modelResource;
	public Map<BiomeOverlayEnum, ResourceLocation> biomeTextureResourceMap = new HashMap<>();
	
	private ElementEnum(String model, String texture, boolean supportsFrost) {
		this.supportsFrost = supportsFrost;
		this.modelResource = ModelLoader.getModelResource(model);
		
		biomeTextureResourceMap.put(BiomeOverlayEnum.NORMAL, TextureLoader.getTextureResource(texture + ".jpg"));
		if (supportsFrost) biomeTextureResourceMap.put(BiomeOverlayEnum.FROST, TextureLoader.getTextureResource(texture + BiomeOverlayEnum.FROST.suffix + ".jpg"));
	}
	
	private ElementEnum(String model, String texture) {
		this(model, texture, false);
	}
	
	public void render() {
		ModelLoader.getModel(modelResource).render();
	}
	
	public void bindTexture(BiomeOverlayEnum biomeOverlay) {
		// TODO Remove this failsafe
		if (biomeOverlay == BiomeOverlayEnum.FROST && !supportsFrost)
			biomeOverlay = BiomeOverlayEnum.NORMAL;
		
		TextureLoader.getTexture(biomeTextureResourceMap.get(biomeOverlay)).bindTexture();
	}
	
	public void bindTextureAndRender(BiomeOverlayEnum biomeOverlay) {
		bindTexture(biomeOverlay);
		render();
	}
}
