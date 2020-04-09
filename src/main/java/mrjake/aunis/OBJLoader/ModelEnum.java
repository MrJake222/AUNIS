package mrjake.aunis.OBJLoader;

import mrjake.aunis.Aunis;
import net.minecraft.util.ResourceLocation;

public enum ModelEnum {
	
	// --------------------------------------------------------------------------------------------
	// Milky Way
	
	MILKYWAY_B0("milkyway/0.obj", null),
	MILKYWAY_B1("milkyway/1.obj", null),
	MILKYWAY_B2("milkyway/2.obj", null),
	MILKYWAY_B3("milkyway/3.obj", null),
	MILKYWAY_B4("milkyway/4.obj", null),
	MILKYWAY_B5("milkyway/5.obj", null),
	MILKYWAY_B6("milkyway/6.obj", null),
	MILKYWAY_B7("milkyway/7.obj", null),
	MILKYWAY_B8("milkyway/8.obj", null),
	MILKYWAY_B9("milkyway/9.obj", null),
	MILKYWAY_B10("milkyway/10.obj", null),
	MILKYWAY_B11("milkyway/11.obj", null),
	MILKYWAY_B12("milkyway/12.obj", null),
	MILKYWAY_B13("milkyway/13.obj", null),
	MILKYWAY_B14("milkyway/14.obj", null),
	MILKYWAY_B15("milkyway/15.obj", null),
	MILKYWAY_B16("milkyway/16.obj", null),
	MILKYWAY_B17("milkyway/17.obj", null),
	MILKYWAY_B18("milkyway/18.obj", null),
	MILKYWAY_B19("milkyway/19.obj", null),
	MILKYWAY_B20("milkyway/20.obj", null),
	MILKYWAY_B21("milkyway/21.obj", null),
	MILKYWAY_B22("milkyway/22.obj", null),
	MILKYWAY_B23("milkyway/23.obj", null),
	MILKYWAY_B24("milkyway/24.obj", null),
	MILKYWAY_B25("milkyway/25.obj", null),
	MILKYWAY_B26("milkyway/26.obj", null),
	MILKYWAY_B27("milkyway/27.obj", null),
	MILKYWAY_B28("milkyway/28.obj", null),
	MILKYWAY_B29("milkyway/29.obj", null),
	MILKYWAY_B30("milkyway/30.obj", null),
	MILKYWAY_B31("milkyway/31.obj", null),
	MILKYWAY_B32("milkyway/32.obj", null),
	MILKYWAY_B33("milkyway/33.obj", null),
	MILKYWAY_B34("milkyway/34.obj", null),
	MILKYWAY_B35("milkyway/35.obj", null),
	MILKYWAY_B36("milkyway/36.obj", null),
	MILKYWAY_B37("milkyway/37.obj", null),
	MILKYWAY_BRB("milkyway/BRB.obj", null),

	MILKYWAY_DHD_MODEL("milkyway/DHD.obj", "milkyway/dhd.png"),
	
	MILKYWAY_GATE_MODEL("milkyway/gate.obj", "milkyway/gatering7.png"),
	MILKYWAY_RING_MODEL("milkyway/ring.obj", "milkyway/gatering7.png"),
	
	MILKYWAY_CHEVRON_LIGHT("milkyway/chevronLight.obj", "milkyway/chevron0.png"),
	MILKYWAY_CHEVRON_FRAME("milkyway/chevronFrame.obj", "milkyway/gatering7.png"),
	MILKYWAY_CHEVRON_MOVING("milkyway/chevronMoving.obj", "milkyway/chevron0.png"),
	MILKYWAY_CHEVRON_BACK("milkyway/chevronBack.obj", "milkyway/gatering7.png"),

	ORLIN_GATE("orlin/gate_orlin.obj", "orlin/gate_orlin.png"),
	
	// --------------------------------------------------------------------------------------------
	// Universe
	
	UNIVERSE_DIALER_MODEL("universe/universe_dialer.obj", "universe/universe_dialer.png"),
	UNIVERSE_GATE_MODEL("universe/universe_gate.obj", "universe/universe_gate.png"),
	UNIVERSE_CHEVRON_MODEL("universe/universe_chevron.obj", "universe/universe_chevron10.png"),
	UNIVERSE_G1("universe/01.obj", null),
	UNIVERSE_G2("universe/02.obj", null),
	UNIVERSE_G3("universe/03.obj", null),
	UNIVERSE_G4("universe/04.obj", null),
	UNIVERSE_G5("universe/05.obj", null),
	UNIVERSE_G6("universe/06.obj", null),
	UNIVERSE_G7("universe/07.obj", null),
	UNIVERSE_G8("universe/08.obj", null),
	UNIVERSE_G9("universe/09.obj", null),
	UNIVERSE_G10("universe/10.obj", null),
	UNIVERSE_G11("universe/11.obj", null),
	UNIVERSE_G12("universe/12.obj", null),
	UNIVERSE_G13("universe/13.obj", null),
	UNIVERSE_G14("universe/14.obj", null),
	UNIVERSE_G15("universe/15.obj", null),
	UNIVERSE_G16("universe/16.obj", null),
	UNIVERSE_G17("universe/17.obj", null),
	UNIVERSE_G18("universe/18.obj", null),
	UNIVERSE_G19("universe/19.obj", null),
	UNIVERSE_G20("universe/20.obj", null),
	UNIVERSE_G21("universe/21.obj", null),
	UNIVERSE_G22("universe/22.obj", null),
	UNIVERSE_G23("universe/23.obj", null),
	UNIVERSE_G24("universe/24.obj", null),
	UNIVERSE_G25("universe/25.obj", null),
	UNIVERSE_G26("universe/26.obj", null),
	UNIVERSE_G27("universe/27.obj", null),
	UNIVERSE_G28("universe/28.obj", null),
	UNIVERSE_G29("universe/29.obj", null),
	UNIVERSE_G30("universe/30.obj", null),
	UNIVERSE_G31("universe/31.obj", null),
	UNIVERSE_G32("universe/32.obj", null),
	UNIVERSE_G33("universe/33.obj", null),
	UNIVERSE_G34("universe/34.obj", null),
	UNIVERSE_G35("universe/35.obj", null),
	UNIVERSE_G36("universe/36.obj", null),

	
	// --------------------------------------------------------------------------------------------
	// Transport rings
	
	RINGS_BLACK("transportrings/rings_black.obj", "transportrings/rings_black.png"),
	RINGSCONTROLLER_GOAULD("transportrings/plate_goauld.obj", "transportrings/goauld_panel.png"),
	RINGSCONTROLLER_GOAULD_BUTTONS("transportrings/buttons_goauld.obj", "transportrings/goauld_buttons.png");

	
	public String modelPath;	
	public ResourceLocation textureResource;
	
	private ModelEnum(String path, String texturePath) {
		this.modelPath = "assets/aunis/models/" + path;
		
		if (texturePath != null) {
			this.textureResource = new ResourceLocation(Aunis.ModID, "textures/tesr/" + texturePath);
		}
	}
}
