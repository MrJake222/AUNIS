package mrjake.aunis.loader;

import mrjake.aunis.loader.model.ModelLoader;
import mrjake.aunis.loader.texture.TextureLoader;
import net.minecraft.util.ResourceLocation;

public enum ElementEnum {
	
	// --------------------------------------------------------------------------------------------
	// Milky Way
	
	MILKYWAY_DHD("milkyway/DHD.obj", "milkyway/dhd.jpg"),
	MILKYWAY_GATE("milkyway/gate.obj", "milkyway/gatering7.jpg"),
	MILKYWAY_RING("milkyway/ring.obj", "milkyway/gatering7.jpg"),
	
	MILKYWAY_CHEVRON_LIGHT("milkyway/chevronLight.obj", "milkyway/chevron0.jpg"),
	MILKYWAY_CHEVRON_FRAME("milkyway/chevronFrame.obj", "milkyway/gatering7.jpg"),
	MILKYWAY_CHEVRON_MOVING("milkyway/chevronMoving.obj", "milkyway/chevron0.jpg"),
	MILKYWAY_CHEVRON_BACK("milkyway/chevronBack.obj", "milkyway/gatering7.jpg"),

	ORLIN_GATE("orlin/gate_orlin.obj", "orlin/gate_orlin.jpg"),

	
	// --------------------------------------------------------------------------------------------
	// Universe
	
	UNIVERSE_GATE("universe/universe_gate.obj", "universe/universe_gate.jpg"),
	UNIVERSE_CHEVRON("universe/universe_chevron.obj", "universe/universe_chevron10.jpg"),
	UNIVERSE_DIALER("universe/universe_dialer.obj", "universe/universe_dialer.jpg"),
	
	
	// --------------------------------------------------------------------------------------------
	// Transport rings
	
	RINGS_BLACK("transportrings/rings_black.obj", "transportrings/rings_black.jpg"),
	RINGSCONTROLLER_GOAULD("transportrings/plate_goauld.obj", "transportrings/goauld_panel.jpg"),
	RINGSCONTROLLER_GOAULD_BUTTONS("transportrings/buttons_goauld.obj", "transportrings/goauld_buttons.jpg");

	
	// --------------------------------------------------------------------------------------------

	public ResourceLocation modelResource;
	public ResourceLocation textureResource;

	private ElementEnum(String model, String texture) {
		this.modelResource = ModelLoader.getModelResource(model);
		this.textureResource = TextureLoader.getTextureResource(texture);
	}
	
	public void render() {
		ModelLoader.getModel(modelResource).render();
	}
	
	public void bindTexture() {
		TextureLoader.getTexture(textureResource).bindTexture();
	}
	
	public void bindTextureAndRender() {
		bindTexture();
		render();
	}
}
