package mrjake.aunis.renderer.stargate;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateMilkyWayMergeHelper;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

public abstract class StargateClassicRenderer<S extends StargateClassicRendererState> extends StargateAbstractRenderer<S> {
	
	@Override
	protected void applyLightMap(StargateClassicRendererState rendererState, double partialTicks) {
		final int chevronCount = 6;
		int skyLight = 0;
		int blockLight = 0;
		
		for (int i=0; i<chevronCount; i++) {
			BlockPos blockPos = StargateMilkyWayMergeHelper.INSTANCE.getChevronBlocks().get(i).rotate(FacingToRotation.get(rendererState.facing)).add(rendererState.pos);
			
			skyLight += getWorld().getLightFor(EnumSkyBlock.SKY, blockPos);
			blockLight += getWorld().getLightFor(EnumSkyBlock.BLOCK, blockPos);
		}
		
		skyLight /= chevronCount;
		blockLight /= chevronCount;
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight * 16, skyLight * 16);
	}
	
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
