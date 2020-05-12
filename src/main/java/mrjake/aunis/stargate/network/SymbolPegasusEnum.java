package mrjake.aunis.stargate.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mrjake.aunis.Aunis;
import net.minecraft.util.ResourceLocation;

public enum SymbolPegasusEnum implements SymbolInterface {
	ACJESIS(0, "Acjesis"),
	LENCHAN(1, "Lenchan"),
	ALURA(2, "Alura"),
	CAPO(3, "Ca Po"),
	LAYLOX(4, "Laylox"),
	ECRUMIG(5, "Ecrumig"),
	AVONIV(6, "Avoniv"),
	BYDO(7, "Bydo"),
	AAXEL(8, "Aaxel"),
	ALDENI(9, "Aldeni"),
	SETAS(10, "Setas"),
	ARAMI(11, "Arami"),
	DANAMI(12, "Danami"),
	POCORE(13, "Poco Re"),
	ROBANDUS(14, "Robandus"),
	RECKTIC(15, "Recktic"),
	ZAMILLOZ(16, "Zamilloz"),
	SUBIDO(17, "Subido"),
	DAWNRE(18, "Dawnre"),
	SALMA(19, "Salma"),
	HAMLINTO(20, "Hamlinto"),
	ELENAMI(21, "Elenami"),
	TAHNAN(22, "Tahnan"),
	ZEO(23, "Zeo"),
	ROEHI(24, "Roehi"),
	ONCEEL(25, "Once El"),
	BASELAI(26, "Baselai"),
	SANDOVI(27, "Sandovi"),
	ILLUME(28, "Illume"),
	AMIWILL(29, "Amiwill"),
	SIBBRON(30, "Sibbron"),
	GILLTIN(31, "Gilltin"),
	ABRIN(32, "Abrin"),
	RAMNON(33, "Ramnon"),
	OLAVII(34, "Olavii"),
	HACEMILL(35, "Hacemill");
	
	public int id;
	public String englishName;
	public String translationKey;
	public ResourceLocation iconResource;

	private SymbolPegasusEnum(int id, String englishName) {
		this.id = id;
		
		this.englishName = englishName;
		this.translationKey = "glyph.aunis.pegasus." + englishName.toLowerCase().replace(" ", "_");
		this.iconResource = new ResourceLocation(Aunis.ModID, "textures/gui/symbol/pegasus/" + englishName.toLowerCase() + ".png");
	}

	@Override
	public boolean origin() {
		return this == SUBIDO;
	}

	@Override
	public float getAngle() {
		return id;
	}
	
	@Override
	public int getAngleIndex() {
		return id;
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
		return SymbolTypeEnum.PEGASUS;
	}
	
	// ------------------------------------------------------------
	// Static
	
	public static SymbolPegasusEnum getRandomSymbol(Random random) {
		int id = 0;
		do { 
			id = random.nextInt(36);
		} while (id == SUBIDO.id);
		
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
	
	public static int getMinimalSymbolCountTo(SymbolTypeEnum symbolType, boolean localDial) {
		switch (symbolType) {
			case MILKYWAY:
			case PEGASUS:
				return localDial ? 7 : 8;
				
			case UNIVERSE:
				return 9;
		}
		
		return 0;
	}
	
	public static SymbolInterface getOrigin() {
		return SUBIDO;
	}
	
	public static int getMaxSymbolsDisplay(boolean hasUpgrade) {
		return hasUpgrade ? 8 : 6;
	}

	public static float getAnglePerGlyph() {
		return 0;
	}
	
	public static SymbolInterface getTopSymbol() {
		return SUBIDO;
	}
	
	private static final Map<Integer, SymbolPegasusEnum> ID_MAP = new HashMap<>();
	private static final Map<String, SymbolPegasusEnum> ENGLISH_NAME_MAP = new HashMap<>();
	
	static {
		for (SymbolPegasusEnum symbol : values()) {
			ID_MAP.put(symbol.id, symbol);
			ENGLISH_NAME_MAP.put(symbol.englishName.toLowerCase(), symbol);
		}
	}
	
	public static final SymbolPegasusEnum valueOf(int id) {
		return ID_MAP.get(id);
	}
	
	public static final SymbolPegasusEnum fromEnglishName(String englishName) {
		return ENGLISH_NAME_MAP.get(englishName.toLowerCase());
	}
}
