package mrjake.aunis.stargate;

import mrjake.aunis.AunisProps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;

public class BoundingHelper {
	
	/**
	 * This method gets appropriate bounding box for given gate rotation(in IBlockState)
	 * The gate should appear as thinner block. Depending on the axis, the bounding box changes
	 * 
	 * @param state - {@link IBlockState} containing the FACING field
	 * @return {@link AxisAlignedBB} appropriate for the rotation
	 */
	@SuppressWarnings("incomplete-switch")
	public static AxisAlignedBB getStargateBlockBoundingBox(IBlockState state) {
				
		if (!state.getValue(AunisProps.RENDER_BLOCK)) {
			switch(state.getValue(AunisProps.FACING_HORIZONTAL).getAxis()) {
				case X:
					return new AxisAlignedBB(0.2, 0, 0, 0.8, 1, 1);
					
				case Z:
					return new AxisAlignedBB(0, 0, 0.2, 1, 1, 0.8);
			}
		}
		
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}
}
