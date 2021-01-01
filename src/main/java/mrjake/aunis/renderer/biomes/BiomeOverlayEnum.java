package mrjake.aunis.renderer.biomes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public enum BiomeOverlayEnum {
	NORMAL(""),
	FROST("_frost");
	
	public String suffix;

	BiomeOverlayEnum(String suffix) {
		this.suffix = suffix;
	}
	
	public static BiomeOverlayEnum getOverlayFromBiome(TileEntity te) {
		BlockPos pos = te.getPos();
		float temp = te.getWorld().getBiome(pos).getTemperature(pos);
		
		if (temp < 0.1)
			return FROST;
		
		return NORMAL;
	}
}
