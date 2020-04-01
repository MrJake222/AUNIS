package mrjake.aunis.stargate.network;

import net.minecraft.util.ResourceLocation;

public interface SymbolInterface {
	
	public abstract boolean origin();
	public abstract float getAngle();
	public abstract int getAngleIndex();
	public abstract int getId();
	public abstract String getEnglishName();
	public abstract ResourceLocation getIconResource();
	public abstract String localize();
	public abstract SymbolTypeEnum getSymbolType();
}
