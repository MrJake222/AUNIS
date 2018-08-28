package mrjake.aunis.stargate.merge;

import net.minecraft.util.math.BlockPos;

public class BlockPosition {
	private final int x;
	private final int y;
	private final int z;
	
	public BlockPosition(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockPos rotateAndGlobal(int angle, BlockPos pos) {
		int x = 0;
		int z = 0;
		
		switch (angle) {
			case 0:
				x = this.x;
				z = this.z;
				break;
				
			case 90:
				x = -this.z;
				z = this.x;
				break;
				
			case 180:
				x = -this.x;
				z = -this.z;
				break;
				
			case 270:
				x = this.z;
				z = -this.x;
				break;				
		}
		
		return pos.add(x, this.y, z);
	}
}
