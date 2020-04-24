package mrjake.aunis.stargate.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mrjake.aunis.Aunis;
import mrjake.aunis.OBJLoader.ModelEnum;
import net.minecraft.util.ResourceLocation;

public enum SymbolUniverseEnum implements SymbolInterface {
	TOP_CHEVRON(0, null),
	G1(1, ModelEnum.UNIVERSE_G1),
	G2(2, ModelEnum.UNIVERSE_G2),
	G3(3, ModelEnum.UNIVERSE_G3),
	G4(4, ModelEnum.UNIVERSE_G4),
	G5(5, ModelEnum.UNIVERSE_G5),
	G6(6, ModelEnum.UNIVERSE_G6),
	G7(7, ModelEnum.UNIVERSE_G7),
	G8(8, ModelEnum.UNIVERSE_G8),
	G9(9, ModelEnum.UNIVERSE_G9),
	G10(10, ModelEnum.UNIVERSE_G10),
	G11(11, ModelEnum.UNIVERSE_G11),
	G12(12, ModelEnum.UNIVERSE_G12),
	G13(13, ModelEnum.UNIVERSE_G13),
	G14(14, ModelEnum.UNIVERSE_G14),
	G15(15, ModelEnum.UNIVERSE_G15),
	G16(16, ModelEnum.UNIVERSE_G16),
	G17(17, ModelEnum.UNIVERSE_G17),
	G18(18, ModelEnum.UNIVERSE_G18),
	G19(19, ModelEnum.UNIVERSE_G19),
	G20(20, ModelEnum.UNIVERSE_G20),
	G21(21, ModelEnum.UNIVERSE_G21),
	G22(22, ModelEnum.UNIVERSE_G22),
	G23(23, ModelEnum.UNIVERSE_G23),
	G24(24, ModelEnum.UNIVERSE_G24),
	G25(25, ModelEnum.UNIVERSE_G25),
	G26(26, ModelEnum.UNIVERSE_G26),
	G27(27, ModelEnum.UNIVERSE_G27),
	G28(28, ModelEnum.UNIVERSE_G28),
	G29(29, ModelEnum.UNIVERSE_G29),
	G30(30, ModelEnum.UNIVERSE_G30),
	G31(31, ModelEnum.UNIVERSE_G31),
	G32(32, ModelEnum.UNIVERSE_G32),
	G33(33, ModelEnum.UNIVERSE_G33),
	G34(34, ModelEnum.UNIVERSE_G34),
	G35(35, ModelEnum.UNIVERSE_G35),
	G36(36, ModelEnum.UNIVERSE_G36);
	
	
	public static final int ANGLE_PER_SECTION = 8;
	
	public int id;
	public ModelEnum model;
	public int angle;
	public int angleIndex;
	public String englishName;
	public String translationKey;
	public ResourceLocation iconResource;

	private SymbolUniverseEnum(int id, ModelEnum model) {
		this.id = id;
		this.model = model;
		
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
	
	public static int getMinimalSymbolCountTo(SymbolTypeEnum symbolType, boolean areDimensionsEqual) {
		switch (symbolType) {
			case MILKYWAY:
			case PEGASUS:
				return 9;
				
			case UNIVERSE:
				return areDimensionsEqual ? 7 : 9;
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
