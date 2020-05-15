package mrjake.aunis.stargate.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mrjake.aunis.Aunis;
import mrjake.aunis.loader.model.ModelLoader;
import net.minecraft.util.ResourceLocation;

public enum SymbolUniverseEnum implements SymbolInterface {
	TOP_CHEVRON(0, null),
	G1(1, "01.obj"),
	G2(2, "02.obj"),
	G3(3, "03.obj"),
	G4(4, "04.obj"),
	G5(5, "05.obj"),
	G6(6, "06.obj"),
	G7(7, "07.obj"),
	G8(8, "08.obj"),
	G9(9, "09.obj"),
	G10(10, "10.obj"),
	G11(11, "11.obj"),
	G12(12, "12.obj"),
	G13(13, "13.obj"),
	G14(14, "14.obj"),
	G15(15, "15.obj"),
	G16(16, "16.obj"),
	G17(17, "17.obj"),
	G18(18, "18.obj"),
	G19(19, "19.obj"),
	G20(20, "20.obj"),
	G21(21, "21.obj"),
	G22(22, "22.obj"),
	G23(23, "23.obj"),
	G24(24, "24.obj"),
	G25(25, "25.obj"),
	G26(26, "26.obj"),
	G27(27, "27.obj"),
	G28(28, "28.obj"),
	G29(29, "29.obj"),
	G30(30, "30.obj"),
	G31(31, "31.obj"),
	G32(32, "32.obj"),
	G33(33, "33.obj"),
	G34(34, "34.obj"),
	G35(35, "35.obj"),
	G36(36, "36.obj");
	
	
	public static final int ANGLE_PER_SECTION = 8;
	
	public int id;
	public ResourceLocation modelResource;
	public int angle;
	public int angleIndex;
	public String englishName;
	public String translationKey;
	public ResourceLocation iconResource;

	private SymbolUniverseEnum(int id, String model) {
		this.id = id;
		
		if (model != null)
			this.modelResource = ModelLoader.getModelResource("universe/" + model);
		
		int id0 = id - 1;
		this.angleIndex = id0 + id0/4 + 1; // skip one each 4
		this.angle = 360 - (angleIndex * ANGLE_PER_SECTION);
		this.englishName = "Glyph " + id;
		this.translationKey = "glyph.aunis.universe.g" + id;
		this.iconResource = new ResourceLocation(Aunis.ModID, "textures/gui/symbol/universe/g"+id+".png");
	}

	@Override
	public boolean origin() {
		return this == G17;
	}

	@Override
	public float getAngle() {
		return angle;
	}
	
	@Override
	public int getAngleIndex() {
		return angleIndex;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getEnglishName() {
		return englishName;
	}
	
	@Override
	public String toString() {
		return getEnglishName();
	}
	
	@Override
	public ResourceLocation getIconResource() {
		return iconResource;
	}

	@Override
	public String localize() {
		return Aunis.proxy.localize(translationKey);
	}
	
	@Override
	public SymbolTypeEnum getSymbolType() {
		return SymbolTypeEnum.UNIVERSE;
	}
	
	public static float getAnglePerGlyph() {
		return ANGLE_PER_SECTION;
	}
	
	// ------------------------------------------------------------
	// Static
	
	public static SymbolUniverseEnum getRandomSymbol(Random random) {		
		int id = 0;
		do { 
			id = random.nextInt(36) + 1;
		} while (id == getOrigin().getId());
		
		return valueOf(id);
	}
	
	public static boolean validateDialedAddress(StargateAddressDynamic stargateAddress) {		
		if (stargateAddress.size() != 7 && stargateAddress.size() != 9)
			return false;
		
		if (!stargateAddress.get(stargateAddress.size()-1).origin())
			return false;
		
		return true;
	}
	
	public static List<SymbolInterface> stripOrigin(List<SymbolInterface> dialedAddress) {
		return dialedAddress;
	}
	
	public static int getMinimalSymbolCountTo(SymbolTypeEnum symbolType, boolean localDial) {
		switch (symbolType) {
			case MILKYWAY:
			case PEGASUS:
				return 9;
				
			case UNIVERSE:
				return localDial ? 7 : 9;
		}
		
		return 0;
	}
	
	public static SymbolInterface getOrigin() {
		return G17;
	}
	
	public static int getMaxSymbolsDisplay(boolean hasUpgrade) {
		return hasUpgrade ? 8 : 6;
	}
	
	public static SymbolInterface getTopSymbol() {
		return TOP_CHEVRON;
	}
	
	private static final Map<Integer, SymbolUniverseEnum> ID_MAP = new HashMap<>();
	private static final Map<String, SymbolUniverseEnum> ENGLISH_NAME_MAP = new HashMap<>();
	
	static {
		for (SymbolUniverseEnum symbol : values()) {
			ID_MAP.put(symbol.id, symbol);
			ENGLISH_NAME_MAP.put(symbol.englishName.toLowerCase(), symbol);
		}
	}
	
	public static final SymbolUniverseEnum valueOf(int id) {
		return ID_MAP.get(id);
	}
	
	public static final SymbolUniverseEnum fromEnglishName(String englishName) {
		return ENGLISH_NAME_MAP.get(englishName.toLowerCase());
	}
}
