package mrjake.aunis.worldgen;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.stargate.merging.StargateMilkyWayMergeHelper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template.BlockInfo;

public class StargateNetherTemplateProcessor implements ITemplateProcessor {

	@Override
	public BlockInfo processBlock(World world, BlockPos pos, BlockInfo blockInfoIn) {
		if (StargateMilkyWayMergeHelper.BASE_MATCHER.apply(blockInfoIn.blockState) ||
			StargateMilkyWayMergeHelper.MEMBER_MATCHER.apply(blockInfoIn.blockState) ||
			blockInfoIn.blockState.getBlock() == AunisBlocks.DHD_BLOCK)
			return blockInfoIn;
		
		if ((blockInfoIn.blockState.getBlock() == Blocks.NETHERRACK || blockInfoIn.blockState.getBlock() == Blocks.QUARTZ_ORE) && world.isAirBlock(pos.down()))
			return null;
		
		if (world.isAirBlock(pos) || (world.getBlockState(pos).getBlock() == Blocks.LAVA && world.getBlockState(pos).getValue(BlockLiquid.LEVEL) > 0))
			return blockInfoIn;
				
		return null;
	}
}
