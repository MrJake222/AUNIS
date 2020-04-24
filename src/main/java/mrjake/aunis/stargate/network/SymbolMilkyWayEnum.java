package mrjake.aunis.stargate.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mrjake.aunis.Aunis;
import mrjake.aunis.OBJLoader.ModelEnum;
import net.minecraft.util.ResourceLocation;

public enum SymbolMilkyWayEnum implements SymbolInterface {
	SCULPTOR(0, 19, "Sculptor", ModelEnum.MILKYWAY_B0),
	SCORPIUS(1, 8, "Scorpius", ModelEnum.MILKYWAY_B1),
	CENTAURUS(2, 4, "Centaurus", ModelEnum.MILKYWAY_B2),
	MONOCEROS(3, 31, "Monoceros", ModelEnum.MILKYWAY_B3),
	ORIGIN(4, 0, "Point of Origin", ModelEnum.MILKYWAY_B4),
	PEGASUS(5, 18, "Pegasus", ModelEnum.MILKYWAY_B5),
	ANDROMEDA(6, 21, "Andromeda", ModelEnum.MILKYWAY_B6),
	SERPENSCAPUT(7, 6, "Serpens Caput", ModelEnum.MILKYWAY_B7),
	ARIES(8, 23, "Aries", ModelEnum.MILKYWAY_B8),
	LIBRA(9, 5, "Libra", ModelEnum.MILKYWAY_B9),
	ERIDANUS(10, 28, "Eridanus", ModelEnum.MILKYWAY_B10),
	LEOMINOR(11, 37, "Leo Minor", ModelEnum.MILKYWAY_B11),
	HYDRA(12, 33, "Hydra", ModelEnum.MILKYWAY_B12),
	SAGITTARIUS(13, 11, "Sagittarius", ModelEnum.MILKYWAY_B13),
	SEXTANS(14, 36, "Sextans", ModelEnum.MILKYWAY_B14),
	SCUTUM(15, 10, "Scutum", ModelEnum.MILKYWAY_B15),
	PISCES(16, 20, "Pisces", ModelEnum.MILKYWAY_B16),
	VIRGO(17, 2, "Virgo", ModelEnum.MILKYWAY_B17),
	BOOTES(18, 3, "Bootes", ModelEnum.MILKYWAY_B18),
	AURIGA(19, 27, "Auriga", ModelEnum.MILKYWAY_B19),
	CORONAAUSTRALIS(20, 9, "Corona Australis", ModelEnum.MILKYWAY_B20),
	GEMINI(21, 32, "Gemini", ModelEnum.MILKYWAY_B21),
	LEO(22, 38, "Leo", ModelEnum.MILKYWAY_B22),
	CETUS(23, 25, "Cetus", ModelEnum.MILKYWAY_B23),
	TRIANGULUM(24, 22, "Triangulum", ModelEnum.MILKYWAY_B24),
	AQUARIUS(25, 17, "Aquarius", ModelEnum.MILKYWAY_B25),
	MICROSCOPIUM(26, 13, "Microscopium", ModelEnum.MILKYWAY_B26),
	EQUULEUS(27, 16, "Equuleus", ModelEnum.MILKYWAY_B27),
	CRATER(28, 1, "Crater", ModelEnum.MILKYWAY_B28),
	PERSEUS(29, 24, "Perseus", ModelEnum.MILKYWAY_B29),
	CANCER(30, 35, "Cancer", ModelEnum.MILKYWAY_B30),
	NORMA(31, 7, "Norma", ModelEnum.MILKYWAY_B31),
	TAURUS(32, 26, "Taurus", ModelEnum.MILKYWAY_B32),
	CANISMINOR(33, 30, "Canis Minor", ModelEnum.MILKYWAY_B33),
	CAPRICORNUS(34, 14, "Capricornus", ModelEnum.MILKYWAY_B34),
	LYNX(35, 34, "Lynx", ModelEnum.MILKYWAY_B35),
	ORION(36, 29, "Orion", ModelEnum.MILKYWAY_B36),
	PISCISAUSTRINUS(37, 15, "Piscis Austrinus", ModelEnum.MILKYWAY_B37),
	BRB(38, -1, "Bright Red Button", ModelEnum.MILKYWAY_BRB);
	
	public static final float ANGLE_PER_GLYPH = 9.2307692f;
	
	public int id;
	public int angleIndex;
	public float angle;
	
	public String englishName;
	public String translationKey;
	public ResourceLocation iconResource;
	
	public ModelEnum model;
	
	SymbolMilkyWayEnum(int id, int angleIndex, String englishName, ModelEnum model) {
		this.id = id;
		
		this.angleIndex = angleIndex;
		this.angle = 360 - (angleIndex * ANGLE_PER_GLYPH);
		
		this.englishName = englishName;
		this.translationKey = "glyph.aunis.milkyway." + englishName.toLowerCase().replace(" ", "_");
		this.iconResource = new ResourceLocation(Aunis.ModID, "textures/gui/symbol/milkyway/" + englishName.toLowerCase() + ".png");
		
		this.model = model;
	}
	
	public boolean brb() {
		return this == BRB;
	}
	
	@Override
	public boolean origin() {
		return this == ORIGIN;
	}
	
	@Override
	public int getId() {
		return id;
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
		return SymbolTypeEnum.MILKYWAY;
	}
	
	// ------------------------------------------------------------
	// Static
	
	public static SymbolMilkyWayEnum getRandomSymbol(Random random) {
		int id = 0;
		do { 
			id = random.nextInt(38);
		} while (id == ORIGIN.id);
		
		return valueOf(id);
	}
	
	public static boolean validateDialedAddress(StargateAddressDynamic stargateAddress) {
		if (stargateAddress.size() < 7)
			return false;
		
		if (!stargateAddress.get(stargateAddress.size()-1).origin())
			return false;
		
		return true;
	}
	
	public static List<SymbolInterface> stripOrigin(List<SymbolInterface> dialedAddress) {
		return dialedAddress.subList(0, dialedAddress.size()-1);
	}

	public static int getMinimalSymbolCountTo(SymbolTypeEnum symbolType, boolean areDimensionsEqual) {
		switch (symbolType) {
			case MILKYWAY:
			case PEGASUS:
				return areDimensionsEqual ? 7 : 8;
				
			case UNIVERSE:
				return 9;
		}
		
		return 0;
	}

	public static SymbolInterface getOrigin() {
		return ORIGIN;
	}
	
	public static int getMaxSymbolsDisplay(boolean hasUpgrade) {
		return hasUpgrade ? 8 : 6;
	}

	public static float getAnglePerGlyph() {
		return ANGLE_PER_GLYPH;
	}
	
	public static SymbolInterface getTopSymbol() {
		return ORIGIN;
	}
	
	private static final Map<Integer, SymbolMilkyWayEnum> ID_MAP = new HashMap<>();
	private static final Map<String, SymbolMilkyWayEnum> ENGLISH_NAME_MAP = new HashMap<>();
	
	static {
		for (SymbolMilkyWayEnum symbol : values()) {
			ID_MAP.put(symbol.id, symbol);
			ENGLISH_NAME_MAP.put(symbol.englishName.toLowerCase(), symbol);
		}
	}
	
	public static final SymbolMilkyWayEnum valueOf(int id) {
		return ID_MAP.get(id);
	}
	
	public static final SymbolMilkyWayEnum fromEnglishName(String englishName) {
		return ENGLISH_NAME_MAP.get(englishName.toLowerCase().replace("ö", "o"));
	}
}
