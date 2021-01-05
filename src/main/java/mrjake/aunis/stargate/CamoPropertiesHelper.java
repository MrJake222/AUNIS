package mrjake.aunis.stargate;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class CamoPropertiesHelper {
	
	/**
	 * This method gets appropriate bounding box for given gate rotation(in IBlockState)
	 * The gate should appear as thinner block. Depending on the axis, the bounding box changes
	 * 
	 * @param state {@link IBlockState} containing the FACING field
	 * @param collision {@code true} if getting the collision box (called from {@link Block#getCollisionBoundingBox(IBlockState, IBlockAccess, BlockPos)}).
	 * @return {@link AxisAlignedBB} appropriate for the rotation
	 */
	@SuppressWarnings("incomplete-switch")
	public static AxisAlignedBB getStargateBlockBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos, boolean collision) {
				
		if (!state.getValue(AunisProps.RENDER_BLOCK)) {
			switch(state.getValue(AunisProps.FACING_HORIZONTAL).getAxis()) {
				case X:
					return new AxisAlignedBB(0.2, 0, 0, 0.8, 1, 1);
					
				case Z:
					return new AxisAlignedBB(0, 0, 0.2, 1, 1, 0.8);
			}
		}
		
		else {	
			if (state.getBlock() == (Block)AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK && access.getBlockState(pos).getBlock() == (Block)AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK) {
				if (!(state instanceof ExtendedBlockState))
					state = state.getBlock().getExtendedState(state, access, pos);

				IBlockState camoState = ((IExtendedBlockState) state).getValue(AunisProps.CAMO_BLOCKSTATE);
				
				if (camoState != null) {					
					if (collision)
						return camoState.getCollisionBoundingBox(access, pos);
					else
						return camoState.getBoundingBox(access, pos);
				}
			}
		}
		
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}

	
	/**
	 * Gets light opacity for the gate. Camo-block aware.
	 * 
	 */
	public static int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (!state.getValue(AunisProps.RENDER_BLOCK)) {
			// Rendering only .obj model
			return 0;
		}
				
		if (!(state instanceof ExtendedBlockState))
			state = state.getBlock().getExtendedState(state, world, pos);
		
		IBlockState camoState = ((IExtendedBlockState) state).getValue(AunisProps.CAMO_BLOCKSTATE);
		
		if (camoState != null) {
			return camoState.getLightOpacity(world, pos);
		}
		
		// Can't get state, return as if some full block is being rendered
		return 255;
	}
}
