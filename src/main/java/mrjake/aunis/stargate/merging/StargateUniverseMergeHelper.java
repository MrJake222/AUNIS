package mrjake.aunis.stargate.merging;

import java.util.Arrays;
import java.util.List;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.util.AunisAxisAlignedBB;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.util.math.BlockPos;

public class StargateUniverseMergeHelper extends StargateClassicMergeHelper {
	
	public static final StargateUniverseMergeHelper INSTANCE = new StargateUniverseMergeHelper();
	
	/**
	 * Bounding box used for {@link StargateMilkyWayBaseTile} search.
	 * Searches 3 blocks to the left/right and 7 blocks down.
	 */
	private static final AunisAxisAlignedBB BASE_SEARCH_BOX = new AunisAxisAlignedBB(-3, -7, 0, 3, 0, 0);
	
	public static final BlockMatcher BASE_MATCHER = BlockMatcher.forBlock(AunisBlocks.STARGATE_UNIVERSE_BASE_BLOCK);
	public static final BlockMatcher MEMBER_MATCHER = BlockMatcher.forBlock(AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK);
		
	private static final List<BlockPos> RING_BLOCKS = Arrays.asList(
			new BlockPos(1, 7, 0),
			new BlockPos(3, 5, 0),
			new BlockPos(3, 3, 0),
			new BlockPos(2, 1, 0),
			new BlockPos(-2, 1, 0),
			new BlockPos(-3, 3, 0),
			new BlockPos(-3, 5, 0),
			new BlockPos(-1, 7, 0));
	
	private static final List<BlockPos> CHEVRON_BLOCKS = Arrays.asList(
			new BlockPos(2, 6, 0),
			new BlockPos(3, 4, 0),
			new BlockPos(3, 2, 0),
			new BlockPos(-3, 2, 0),
			new BlockPos(-3, 4, 0),
			new BlockPos(-2, 6, 0),
			new BlockPos(1, 0, 0),
			new BlockPos(-1, 0, 0),
			new BlockPos(0, 7, 0));
	
	@Override
	public List<BlockPos> getRingBlocks() {
		return RING_BLOCKS;
	}
	
	@Override
	public List<BlockPos> getChevronBlocks() {
		return CHEVRON_BLOCKS;
	}
	
	@Override
	public AunisAxisAlignedBB getBaseSearchBox() {
		return BASE_SEARCH_BOX;
	}
	
	@Override
	public boolean matchBase(IBlockState state) {
		return BASE_MATCHER.apply(state);
	}
	
	@Override
	public boolean matchMember(IBlockState state) {
		return MEMBER_MATCHER.apply(state);
	}
	
	@Override
	public Block getMemberBlock() {
		return AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK;
	}
}
