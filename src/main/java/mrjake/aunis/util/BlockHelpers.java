package mrjake.aunis.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockHelpers {
	
	public static boolean isBlockDirectlyUnderSky(IBlockAccess world, BlockPos pos) {
		while (pos.getY() < 255) {
			pos = pos.up();
			
			if (!world.isAirBlock(pos))
				return false;
		}
		
		return true;
	}
}
