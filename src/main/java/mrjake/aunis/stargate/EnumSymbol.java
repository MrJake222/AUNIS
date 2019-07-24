package mrjake.aunis.stargate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EnumSymbol {
	SCULPTOR(0, "Sculptor", 19),
	SCORPIUS(1, "Scorpius", 8),
	CENTAURUS(2, "Centaurus", 4),
	MONOCEROS(3, "Monoceros", 31),
	ORIGIN(4, "Point of Origin", 0),
	PEGASUS(5, "Pegasus", 18),
	ANDROMEDA(6, "Andromeda", 21),
	SERPENSCAPUT(7, "Serpens Caput", 6),
	ARIES(8, "Aries", 23),
	LIBRA(9, "Libra", 5),
	ERIDANUS(10, "Eridanus", 28),
	LEOMINOR(11, "Leo Minor", 37),
	HYDRA(12, "Hydra", 33),
	SAGITTARIUS(13, "Sagittarius", 11),
	SEXTANS(14, "Sextans", 36),
	SCUTUM(15, "Scutum", 10),
	PISCES(16, "Pisces", 20),
	VIRGO(17, "Virgo", 2),
	BOOTES(18, "Boötes", 3),
	AURIGA(19, "Auriga", 27),
	CORONAAUSTRALIS(20, "Corona Australis", 9),
	GEMINI(21, "Gemini", 32),
	LEO(22, "Leo", 38),
	CETUS(23, "Cetus", 25),
	TRIANGULUM(24, "Triangulum", 22),
	AQUARIUS(25, "Aquarius", 17),
	MICROSCOPIUM(26, "Microscopium", 13),
	EQUULEUS(27, "Equuleus", 16),
	CRATER(28, "Crater", 1),
	PERSEUS(29, "Perseus", 24),
	CANCER(30, "Cancer", 35),
	NORMA(31, "Norma", 7),
	TAURUS(32, "Taurus", 26),
	CANISMINOR(33, "Canis Minor", 30),
	CAPRICORNUS(34, "Capricornus", 14),
	LYNX(35, "Lynx", 34),
	ORION(36, "Orion", 29),
	PISCISAUSTRINUS(37, "Piscis Austrinus", 15),
	BRB(38, "Bright Red Button", -1);
	
	public int id;
	public String name;
	public int angleIndex;
	public float angle;
	private static Map<Integer, EnumSymbol> idSymbolMap = new HashMap<Integer, EnumSymbol>();
	private static Map<String, EnumSymbol> nameSymbolMap = new HashMap<String, EnumSymbol>();
	private static Map<Integer,EnumSymbol> angleIndexSymbolMap = new HashMap<Integer, EnumSymbol>();
	
	public static final float ANGLE_PER_GLYPH = 9.2307692f;
	
	EnumSymbol(int id, String name, int angleIndex) {
		this.id = id;
		this.name = name;
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
			nameSymbolMap.put(symbol.name, symbol);
			angleIndexSymbolMap.put(symbol.angleIndex, symbol);
		}
	}
	
	public static EnumSymbol valueOf(int id) {
		return idSymbolMap.get(id);
	}
	
	public static EnumSymbol forName(String name) {
		if (name.equals("Bootes"))
			return EnumSymbol.BOOTES;
		
		return nameSymbolMap.get(name);
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
	
	/**
	 * Convert {@link List} of {@link EnumSymbol} to {@link List<String>} of symbol's names
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> toStringList(List<EnumSymbol> list) {
		List<String> out = new ArrayList<String>();
		
		for (EnumSymbol symbol : list)
			out.add(symbol.name);
		
		return out;
	}
	
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
	
	public boolean equals(EnumSymbol symbol) {
		return symbol.id == this.id;
	}
	
	public String toString() {
		return name;
	}
}

