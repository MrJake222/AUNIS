package mrjake.aunis.stargate;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BoundingHelper {
	
	/**
	 * This method gets appropriate bounding box for given gate rotation(in IBlockState)
	 * The gate should appear as thinner block. Depending on the axis, the bounding box changes
	 * 
	 * @param state - {@link IBlockState} containing the FACING field
	 * @return {@link AxisAlignedBB} appropriate for the rotation
	 */
	@SuppressWarnings("incomplete-switch")
	public static AxisAlignedBB getStargateBlockBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
				
		if (!state.getValue(AunisProps.RENDER_BLOCK)) {
			switch(state.getValue(AunisProps.FACING_HORIZONTAL).getAxis()) {
				case X:
					return new AxisAlignedBB(0.2, 0, 0, 0.8, 1, 1);
					
				case Z:
					return new AxisAlignedBB(0, 0, 0.2, 1, 1, 0.8);
			}
		}
		
		else {	
			if (state.getBlock() == AunisBlocks.stargateMilkyWayMemberBlock && access.getBlockState(pos).getBlock() == AunisBlocks.stargateMilkyWayMemberBlock) {
				state = state.getBlock().getExtendedState(state, access, pos);
				IBlockState camoState = ((IExtendedBlockState) state).getValue(AunisProps.CAMO_BLOCKSTATE);
				
				if (camoState != null) {
					Block block = camoState.getBlock();
				
					if (block instanceof BlockSlab && !((BlockSlab) block).isDouble()) {
						return new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
					}
				}
			}
		}
		
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}
}
