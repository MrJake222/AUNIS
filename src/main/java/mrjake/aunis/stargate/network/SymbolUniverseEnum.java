package mrjake.aunis.stargate.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mrjake.aunis.Aunis;
import net.minecraft.util.ResourceLocation;

public enum SymbolUniverseEnum implements SymbolInterface {
	G1(1),
	G2(2),
	G3(3),
	G4(4),
	G5(5),
	G6(6),
	G7(7),
	G8(8),
	G9(9),
	G10(10),
	G11(11),
	G12(12),
	G13(13),
	G14(14),
	G15(15),
	G16(16),
	G17(17),
	G18(18),
	G19(19),
	G20(20),
	G21(21),
	G22(22),
	G23(23),
	G24(24),
	G25(25),
	G26(26),
	G27(27),
	G28(28),
	G29(29),
	G30(30),
	G31(31),
	G32(32),
	G33(33),
	G34(34),
	G35(35),
	G36(36);
	
	public static final int ANGLE_PER_SECTION = 8;
	
	public int id;
	public int angle;
	public String englishName;
	public String translationKey;
	public ResourceLocation iconResource;

	private SymbolUniverseEnum(int id) {
		this.id = id;
		
		int id0 = id - 1;
		this.angle = id0 + id0/4; // skip one each 4
		this.englishName = "Glyph " + id;
		this.translationKey = "glyph.aunis.universe.g" + id;
		this.iconResource = new ResourceLocation(Aunis.ModID, "textures/gui/symbol/universe/g"+id+".png");
	}

	@Override
	public boolean origin() {
		return false;
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
		return 0;
	}
	
	// ------------------------------------------------------------
	// Static
	
	public static SymbolUniverseEnum getRandomSymbol(Random random) {
		return valueOf(random.nextInt(36) + 1);
	}
	
	public static boolean validateDialedAddress(StargateAddressDynamic stargateAddress) {		
		return stargateAddress.size() == 6 || stargateAddress.size() == 9;
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
				return areDimensionsEqual ? 6 : 9;
		}
		
		return 0;
	}
	
	public static SymbolInterface getOrigin() {
		return null;
	}
	
	public static int getMaxSymbolsDisplay(boolean hasUpgrade) {
		return hasUpgrade ? 9 : 6;
	}
	
	private static final Map<Integer, SymbolUniverseEnum> ID_MAP = new HashMap<>();
	static {
		for (SymbolUniverseEnum symbol : values()) {
			ID_MAP.put(symbol.id, symbol);
		}
	}
	
	public static final SymbolUniverseEnum valueOf(int id) {
		return ID_MAP.get(id);
	}
}
