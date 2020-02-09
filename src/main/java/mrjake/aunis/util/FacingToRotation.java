package mrjake.aunis.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;

public class FacingToRotation {
	private static final Map<EnumFacing, Rotation> ROTATION_MAP = new HashMap<EnumFacing, Rotation>();

	static {
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			switch (facing) {
				case SOUTH: ROTATION_MAP.put(facing, Rotation.NONE); break;
				case WEST: ROTATION_MAP.put(facing, Rotation.CLOCKWISE_90); break;
				case NORTH: ROTATION_MAP.put(facing, Rotation.CLOCKWISE_180); break;
				case EAST: ROTATION_MAP.put(facing, Rotation.COUNTERCLOCKWISE_90); break;
				default: break;
			}
		}
	}
	
	public static Rotation get(EnumFacing facing) {
		return ROTATION_MAP.get(facing);
	}
}
