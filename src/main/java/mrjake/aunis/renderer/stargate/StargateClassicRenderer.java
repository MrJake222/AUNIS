package mrjake.aunis.renderer.stargate;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.stargate.StargateAbstractMergeHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class StargateClassicRenderer<S extends StargateClassicRendererState> extends StargateAbstractRenderer<S> {
	
	@Override
	protected Map<BlockPos, IBlockState> getMemberBlockStates(StargateAbstractMergeHelper mergeHelper, EnumFacing facing) {
		Map<BlockPos, IBlockState> map = new HashMap<BlockPos, IBlockState>();
		
		for (BlockPos pos : mergeHelper.getRingBlocks())
			map.put(pos, mergeHelper.getMemberBlock().getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING).withProperty(AunisProps.FACING_HORIZONTAL, facing));
		
		for (BlockPos pos : mergeHelper.getChevronBlocks())
			map.put(pos, mergeHelper.getMemberBlock().getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.CHEVRON).withProperty(AunisProps.FACING_HORIZONTAL, facing));
		
		return map;
	}
	
	
	// ----------------------------------------------------------------------------------------
	// Chevrons
	
	protected abstract void renderChevron(S rendererState, double partialTicks, ChevronEnum chevron);
	
	protected void renderChevrons(S rendererState, double partialTicks) {
		for (ChevronEnum chevron : ChevronEnum.values())
			renderChevron(rendererState, partialTicks, chevron);
		
		rendererState.chevronTextureList.iterate(getWorld(), partialTicks);
	}
}
