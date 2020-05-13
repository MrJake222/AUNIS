package mrjake.aunis.stargate.network;

import java.util.List;
import java.util.Random;

import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;

public enum SymbolTypeEnum implements EnumKeyInterface<Integer> {
	MILKYWAY(0, 32, 32),
	PEGASUS (1, 27, 27),
	UNIVERSE(2, 20, 42);
	
	public int id;
	public int iconWidht;
	public int iconHeight;

	private SymbolTypeEnum(int id, int iconWidht, int iconHeight) {
		this.id = id;
		this.iconWidht = iconWidht;
		this.iconHeight = iconHeight;
	}
	
	public SymbolInterface getRandomSymbol(Random random) {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.getRandomSymbol(random);
				
			case PEGASUS:
				return SymbolPegasusEnum.getRandomSymbol(random);
				
			case UNIVERSE:
				return SymbolUniverseEnum.getRandomSymbol(random);
		}
		
		return null;
	}
	
	public SymbolInterface valueOfSymbol(int id) {
		switch (this) {
		case MILKYWAY:
			return SymbolMilkyWayEnum.valueOf(id);
			
		case PEGASUS:
			return SymbolPegasusEnum.valueOf(id);
			
		case UNIVERSE:
			return SymbolUniverseEnum.valueOf(id);
		}
		
		return null;
	}
	
	public boolean validateDialedAddress(StargateAddressDynamic stargateAddress) {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.validateDialedAddress(stargateAddress);
				
			case PEGASUS:
				return SymbolPegasusEnum.validateDialedAddress(stargateAddress);
				
			case UNIVERSE:
				return SymbolUniverseEnum.validateDialedAddress(stargateAddress);
		}
		
		return false;
	}
	
	public List<SymbolInterface> stripOrigin(List<SymbolInterface> dialedAddress) {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.stripOrigin(dialedAddress);
				
			case PEGASUS:
				return SymbolPegasusEnum.stripOrigin(dialedAddress);
				
			case UNIVERSE:
				return SymbolUniverseEnum.stripOrigin(dialedAddress);
		}
		
		return null;
	}
	
	public int getMinimalSymbolCountTo(SymbolTypeEnum symbolType, boolean localDial) {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.getMinimalSymbolCountTo(symbolType, localDial);
				
			case PEGASUS:
				return SymbolPegasusEnum.getMinimalSymbolCountTo(symbolType, localDial);
				
			case UNIVERSE:
				return SymbolUniverseEnum.getMinimalSymbolCountTo(symbolType, localDial);
		}
		
		return 0;
	}

	public SymbolInterface getOrigin() {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.getOrigin();
				
			case PEGASUS:
				return SymbolPegasusEnum.getOrigin();
				
			case UNIVERSE:
				return SymbolUniverseEnum.getOrigin();
		}
		
		return null;
	}
	

	public SymbolInterface fromEnglishName(String englishName) {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.fromEnglishName(englishName);
				
			case PEGASUS:
				return SymbolPegasusEnum.fromEnglishName(englishName);
				
			case UNIVERSE:
				SymbolUniverseEnum symbol = SymbolUniverseEnum.fromEnglishName(englishName);
				
				if (symbol != null)
					return symbol;
				
				try {
					return SymbolUniverseEnum.valueOf(englishName.toUpperCase());
				}
				
				catch (IllegalArgumentException ex) {
					return null;
				}
		}
		
		return null;
	}
	
	public int getMaxSymbolsDisplay(boolean hasUpgrade) {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.getMaxSymbolsDisplay(hasUpgrade);
				
			case PEGASUS:
				return SymbolPegasusEnum.getMaxSymbolsDisplay(hasUpgrade);
				
			case UNIVERSE:
				return SymbolUniverseEnum.getMaxSymbolsDisplay(hasUpgrade);
		}
		
		return 0;
	}
	
	public float getAnglePerGlyph() {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.getAnglePerGlyph();
				
			case PEGASUS:
				return SymbolPegasusEnum.getAnglePerGlyph();
				
			case UNIVERSE:
				return SymbolUniverseEnum.getAnglePerGlyph();
		}
		
		return 0;
	}
	

	public SymbolInterface getTopSymbol() {
		switch (this) {
			case MILKYWAY:
				return SymbolMilkyWayEnum.getTopSymbol();
				
			case PEGASUS:
				return SymbolPegasusEnum.getTopSymbol();
				
			case UNIVERSE:
				return SymbolUniverseEnum.getTopSymbol();
		}
		
		return null;
	}
	
	public boolean hasOrigin() {
		return getOrigin() != null;
	}
	
	
	// ------------------------------------------------------------
	// Static
	
	private static final EnumKeyMap<Integer, SymbolTypeEnum> ID_MAP = new EnumKeyMap<Integer, SymbolTypeEnum>(values());
	
	@Override
	public Integer getKey() {
		return id;
	}
	
	public static SymbolTypeEnum valueOf(int id) {
		return ID_MAP.valueOf(id);
	}
}
