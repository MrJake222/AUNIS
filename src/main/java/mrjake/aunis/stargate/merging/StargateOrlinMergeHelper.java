package mrjake.aunis.stargate.merging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.stargate.StargateOrlinMemberBlock;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.tileentity.stargate.StargateOrlinMemberTile;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class StargateOrlinMergeHelper extends StargateAbstractMergeHelper {

	public static final StargateOrlinMergeHelper INSTANCE = new StargateOrlinMergeHelper();

	private static final AunisAxisAlignedBB BASE_SEARCH_BOX = new AunisAxisAlignedBB(-1, -2, -1, 1, 0, 1);
	
	public static final BlockMatcher BASE_MATCHER = BlockMatcher.forBlock(AunisBlocks.STARGATE_ORLIN_BASE_BLOCK);
	public static final BlockMatcher MEMBER_MATCHER = BlockMatcher.forBlock(AunisBlocks.STARGATE_ORLIN_MEMBER_BLOCK);
	
	private static final Map<BlockPos, EnumFacing> BLOCK_MAP = new HashMap<>(); 
	
	static {
		BLOCK_MAP.put(new BlockPos(-1, 0, 0), EnumFacing.EAST);
		BLOCK_MAP.put(new BlockPos(-1, 1, 0), EnumFacing.DOWN);
		BLOCK_MAP.put(new BlockPos(-1, 2, 0), EnumFacing.UP);
		BLOCK_MAP.put(new BlockPos(0, 2, 0), EnumFacing.UP);
		BLOCK_MAP.put(new BlockPos(1, 2, 0), EnumFacing.UP);
		BLOCK_MAP.put(new BlockPos(1, 1, 0), EnumFacing.DOWN);
		BLOCK_MAP.put(new BlockPos(1, 0, 0), EnumFacing.WEST);
	}
	
	@Override
	public List<BlockPos> getRingBlocks() {
		return new ArrayList<BlockPos>(BLOCK_MAP.keySet());
	}

	@Override
	public List<BlockPos> getChevronBlocks() {
		return new ArrayList<BlockPos>(0);
	}

	@Override
	@Nullable
	public EnumMemberVariant getMemberVariantFromItemStack(ItemStack stack) {
		if (!(stack.getItem() instanceof ItemBlock))
			return null;
		
		// No need to use .equals() because blocks are singletons
		if (((ItemBlock) stack.getItem()).getBlock() != AunisBlocks.STARGATE_ORLIN_MEMBER_BLOCK)
			return null;
		
		return EnumMemberVariant.RING;
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
	public StargateOrlinMemberBlock getMemberBlock() {
		return AunisBlocks.STARGATE_ORLIN_MEMBER_BLOCK;
	}
	
	@Override
	protected boolean checkMemberBlock(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing, EnumMemberVariant variant) {
		IBlockState state = blockAccess.getBlockState(pos);
				
		return MEMBER_MATCHER.apply(state) &&
				state.getValue(AunisProps.ORLIN_VARIANT) == facing &&
				!((StargateOrlinMemberTile) blockAccess.getTileEntity(pos)).isBroken();
	}

	@Override
	protected void updateMemberMergeStatus(World world, BlockPos checkPos, BlockPos basePos, EnumFacing baseFacing, boolean shouldBeMerged) {		
		EnumFacing variant = BLOCK_MAP.get(checkPos);
		
		checkPos = checkPos.rotate(FacingToRotation.get(baseFacing)).add(basePos);
		IBlockState memberState = world.getBlockState(checkPos);
		
		if (MEMBER_MATCHER.apply(memberState)) {
			StargateOrlinMemberTile memberTile = (StargateOrlinMemberTile) world.getTileEntity(checkPos);
			
			if (baseFacing == EnumFacing.NORTH) {
				if (variant == EnumFacing.WEST) variant = EnumFacing.EAST;
				else if (variant == EnumFacing.EAST) variant = EnumFacing.WEST;
			}
			
			if (baseFacing == EnumFacing.WEST) {
				if (variant == EnumFacing.WEST) variant = EnumFacing.NORTH;
				else if (variant == EnumFacing.EAST) variant = EnumFacing.SOUTH;
			}
			
			if (baseFacing == EnumFacing.EAST) {
				if (variant == EnumFacing.WEST) variant = EnumFacing.SOUTH;
				else if (variant == EnumFacing.EAST) variant = EnumFacing.NORTH;
			}
						
			if ((shouldBeMerged && !memberTile.isMerged()) || (memberTile.isMerged() && memberTile.getBasePos().equals(basePos))) {
				memberState = memberState.withProperty(AunisProps.ORLIN_VARIANT, shouldBeMerged ? variant : baseFacing);			
				world.setBlockState(checkPos, memberState.withProperty(AunisProps.RENDER_BLOCK, !shouldBeMerged));
				
				memberTile.setBasePos(shouldBeMerged ? basePos : null);
			}
		}
	}
	
	public void incrementMembersOpenCount(World world, BlockPos basePos, EnumFacing baseFacing) {
		for (BlockPos pos : getRingBlocks()) {
			pos = pos.rotate(FacingToRotation.get(baseFacing)).add(basePos);
			
			if (MEMBER_MATCHER.apply(world.getBlockState(pos))) {
				StargateOrlinMemberTile memberTile = (StargateOrlinMemberTile) world.getTileEntity(pos);
				memberTile.incrementOpenCount();
			}
		}
	}	

	public int getMaxOpenCount(World world, BlockPos basePos, EnumFacing baseFacing) {
		int max = 0;
		
		for (BlockPos pos : getRingBlocks()) {
			pos = pos.rotate(FacingToRotation.get(baseFacing)).add(basePos);

			if (MEMBER_MATCHER.apply(world.getBlockState(pos))) {
				StargateOrlinMemberTile memberTile = (StargateOrlinMemberTile) world.getTileEntity(pos);
				int open = memberTile.getOpenCount();
				
				if (open > max)
					max = open;
			}
		}
		
		return max;
	}	
}
