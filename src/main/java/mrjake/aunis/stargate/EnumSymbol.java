package mrjake.aunis.stargate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;

public enum EnumSymbol {
	SCULPTOR(0, "Sculptor", "glyph.aunis.sculptor", "sculptor.png", 19),
	SCORPIUS(1, "Scorpius", "glyph.aunis.scorpius", "scorpius.png", 8),
	CENTAURUS(2, "Centaurus", "glyph.aunis.centaurus", "centaurus.png", 4),
	MONOCEROS(3, "Monoceros", "glyph.aunis.monoceros", "monoceros.png", 31),
	ORIGIN(4, "Point of Origin", "glyph.aunis.point_of_origin", "point of origin.png", 0),
	PEGASUS(5, "Pegasus", "glyph.aunis.pegasus", "pegasus.png", 18),
	ANDROMEDA(6, "Andromeda", "glyph.aunis.andromeda", "andromeda.png", 21),
	SERPENSCAPUT(7, "Serpens Caput", "glyph.aunis.serpens_caput", "serpens caput.png", 6),
	ARIES(8, "Aries", "glyph.aunis.aries", "aries.png", 23),
	LIBRA(9, "Libra", "glyph.aunis.libra", "libra.png", 5),
	ERIDANUS(10, "Eridanus", "glyph.aunis.eridanus", "eridanus.png", 28),
	LEOMINOR(11, "Leo Minor", "glyph.aunis.leo_minor", "leo minor.png", 37),
	HYDRA(12, "Hydra", "glyph.aunis.hydra", "hydra.png", 33),
	SAGITTARIUS(13, "Sagittarius", "glyph.aunis.sagittarius", "sagittarius.png", 11),
	SEXTANS(14, "Sextans", "glyph.aunis.sextans", "sextans.png", 36),
	SCUTUM(15, "Scutum", "glyph.aunis.scutum", "scutum.png", 10),
	PISCES(16, "Pisces", "glyph.aunis.pisces", "pisces.png", 20),
	VIRGO(17, "Virgo", "glyph.aunis.virgo", "virgo.png", 2),
	BOOTES(18, "Bootes", "glyph.aunis.boötes", "boötes.png", 3),
	AURIGA(19, "Auriga", "glyph.aunis.auriga", "auriga.png", 27),
	CORONAAUSTRALIS(20, "Corona Australis", "glyph.aunis.corona_australis", "corona australis.png", 9),
	GEMINI(21, "Gemini", "glyph.aunis.gemini", "gemini.png", 32),
	LEO(22, "Leo", "glyph.aunis.leo", "leo.png", 38),
	CETUS(23, "Cetus", "glyph.aunis.cetus", "cetus.png", 25),
	TRIANGULUM(24, "Triangulum", "glyph.aunis.triangulum", "triangulum.png", 22),
	AQUARIUS(25, "Aquarius", "glyph.aunis.aquarius", "aquarius.png", 17),
	MICROSCOPIUM(26, "Microscopium", "glyph.aunis.microscopium", "microscopium.png", 13),
	EQUULEUS(27, "Equuleus", "glyph.aunis.equuleus", "equuleus.png", 16),
	CRATER(28, "Crater", "glyph.aunis.crater", "crater.png", 1),
	PERSEUS(29, "Perseus", "glyph.aunis.perseus", "perseus.png", 24),
	CANCER(30, "Cancer", "glyph.aunis.cancer", "cancer.png", 35),
	NORMA(31, "Norma", "glyph.aunis.norma", "norma.png", 7),
	TAURUS(32, "Taurus", "glyph.aunis.taurus", "taurus.png", 26),
	CANISMINOR(33, "Canis Minor", "glyph.aunis.canis_minor", "canis minor.png", 30),
	CAPRICORNUS(34, "Capricornus", "glyph.aunis.capricornus", "capricornus.png", 14),
	LYNX(35, "Lynx", "glyph.aunis.lynx", "lynx.png", 34),
	ORION(36, "Orion", "glyph.aunis.orion", "orion.png", 29),
	PISCISAUSTRINUS(37, "Piscis Austrinus", "glyph.aunis.piscis_austrinus", "piscis austrinus.png", 15),
	BRB(38, "Bright Red Button", "glyph.aunis.bright_red_button", "bright red button.png", -1);
	
	public int id;
	public String translationKey;
	public String englishName;
	public String iconFile;
	public int angleIndex;
	public float angle;
	private static Map<Integer, EnumSymbol> idSymbolMap = new HashMap<Integer, EnumSymbol>();
	private static Map<String, EnumSymbol> nameSymbolMap = new HashMap<String, EnumSymbol>();
	private static Map<Integer,EnumSymbol> angleIndexSymbolMap = new HashMap<Integer, EnumSymbol>();
	
	public static final float ANGLE_PER_GLYPH = 9.2307692f;
	
	EnumSymbol(int id, String englishName, String translationKey, String iconFile, int angleIndex) {
		this.id = id;
		this.englishName = englishName;
		this.translationKey = translationKey;
		this.iconFile = iconFile;
		this.angleIndex = angleIndex;
		
		// BRB angle = -1
		// 360 / 39 = 9.2307692
		if (angleIndex != -1)
			this.angle = 360 - (angleIndex * ANGLE_PER_GLYPH);
		
		else 
			this.angle = -1;
	}
	
	static {
		for (EnumSymbol symbol : EnumSymbol.values()) {
			idSymbolMap.put(symbol.id, symbol);
			nameSymbolMap.put(symbol.englishName.toLowerCase(), symbol);
			angleIndexSymbolMap.put(symbol.angleIndex, symbol);
		}
	}
	
	public static EnumSymbol valueOf(int id) {
		return idSymbolMap.get(id);
	}
	
	public static EnumSymbol forEnglishName(String translationKey) {
		return nameSymbolMap.get(translationKey.toLowerCase());
	}
	
	public static EnumSymbol fromAngleIndex(int index) {
		return angleIndexSymbolMap.get(index);
	}
	
	public static EnumSymbol fromAngle(double angle) {
		double angleIndex = (360 - angle) / ANGLE_PER_GLYPH;
		int index = (int) Math.round(angleIndex);
		
		if (index == 12)
			index = 11;
		
		else if (index == 39)
			index = 0;
		
		return fromAngleIndex(index);
	}
	
	
	// Convert List of EnumSymbols to list of IDs
	public static List<Integer> toIntegerList(List<EnumSymbol> list, EnumSymbol... toBeAddded) {
		List<Integer> out = new ArrayList<Integer>();
		
		for (EnumSymbol symbol : list)
			out.add(symbol.id);
		
		for (EnumSymbol add : toBeAddded)
			out.add(add.id);
		
		return out;
	}
	
	// Convert ID list to list of EnumSymbols
	public static List<EnumSymbol> toSymbolList(List<Integer> list, EnumSymbol... toBeAddded) {
		List<EnumSymbol> out = new ArrayList<EnumSymbol>();
		
		for (int id : list)
			out.add(EnumSymbol.valueOf(id));
		
		for (EnumSymbol add : toBeAddded)
			out.add(add);
		
		return out;
	}
//	
//	/**
//	 * Convert {@link List} of {@link EnumSymbol} to {@link List<String>} of symbol's names
//	 * 
//	 * @param list
//	 * @return
//	 */
//	public static List<String> toStringList(List<EnumSymbol> list) {
//		List<String> out = new ArrayList<String>();
//		
//		for (EnumSymbol symbol : list)
//			out.add(symbol.name);
//		
//		return out;
//	}
	
	// Serialize address to long
	public static long toLong(List<EnumSymbol> address) {
		long out = 0;
		
		// This encodes only "base" address - 6 symbols
		int size = 6;
		
		/*
		int size = address.size(); 
		if (address.get(size-1) == EnumSymbol.ORIGIN) {
			size--;
		}
		
		if (size > 6)
			size--;*/
		
		
		
		
		for (int i=0; i<size; i++) {
			long id = (long)(address.get(i).id) << i*6;
			
			out |= id;
		}
		
		return out;
	}
	
	// Deserialize address
	public static List<Integer> fromLong(long address) {
		List<Integer> out = new ArrayList<Integer>();
				
		// This decodes only base address - 6 symbol one
		for (int i=0; i<6; i++) {
			int id = (int) ((address >>> i*6) & 0x3F); // 0b00111111
			
			out.add( id );
		}
		
		return out;
	}
	
	public String localize() {
		return Aunis.proxy.localize(translationKey);
	}
	
	@Override
	public String toString() {
		return englishName;
	}
}

