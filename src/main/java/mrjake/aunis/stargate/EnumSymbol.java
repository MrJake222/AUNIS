package mrjake.aunis.stargate;

import java.util.HashMap;
import java.util.Map;

public enum EnumSymbol {
	SCULPTOR(0, "Sculptor"),
	SCORPIUS(1, "Scorpius"),
	CENTAURUS(2, "Centaurus"),
	MONOCEROS(3, "Monoceros"),
	ORIGIN(4, "Point of Origin"),
	PEGASUS(5, "Pegasus"),
	ANDROMEDA(6, "Andromeda"),
	SERPENSCAPUT(7, "Serpens Caput"),
	ARIES(8, "Aries"),
	LIBRA(9, "Libra"),
	ERIDANUS(10, "Eridanus"),
	LEOMINOR(11, "Leo Minor"),
	HYDRA(12, "Hydra"),
	SAGITTARIUS(13, "Sagittarius"),
	SEXTANS(14, "Sextans"),
	SCUTUM(15, "Scutum"),
	PISCES(16, "Pisces"),
	VIRGO(17, "Virgo"),
	BOOTES(18, "Boötes"),
	AURIGA(19, "Auriga"),
	CORONAAUSTRALIS(20, "Corona Australis"),
	GEMINI(21, "Gemini"),
	LEO(22, "Leo"),
	CETUS(23, "Cetus"),
	TRIANGULUM(24, "Triangulum"),
	AQUARIUS(25, "Aquarius"),
	MICROSCOPIUM(26, "Microscopium"),
	EQUULEUS(27, "Equuleus"),
	CRATER(28, "Crater"),
	PERSEUS(29, "Perseus"),
	CANCER(30, "Cancer"),
	NORMA(31, "Norma"),
	TAURUS(32, "Taurus"),
	CANISMINOR(33, "Canis Minor"),
	CAPRICORNUS(34, "Capricornus"),
	LYNX(35, "Lynx"),
	ORION(36, "Orion"),
	PISCISAUSTRINUS(37, "Piscis Austrinus"),
	BRB(38, "Bright Red Button");
	
	public int id;
	public String name;
	private static Map<Integer, EnumSymbol> map = new HashMap<Integer, EnumSymbol>();
	
	EnumSymbol(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	static {
		for (EnumSymbol symbol : EnumSymbol.values()) {
			map.put(symbol.id, symbol);
		}
	}
	
	public static EnumSymbol valueOf(int id) {
		return map.get(id);
	}
	
	public boolean equals(EnumSymbol symbol) {
		return symbol.id == this.id;
	}
	
	public String toString() {
		return name;
	}
}

