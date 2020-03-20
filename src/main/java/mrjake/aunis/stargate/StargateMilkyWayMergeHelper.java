package mrjake.aunis.stargate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.stargate.StargateMilkyWayBaseBlock;
import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class StargateMilkyWayMergeHelper extends StargateAbstractMergeHelper {
	
	public static final StargateMilkyWayMergeHelper INSTANCE = new StargateMilkyWayMergeHelper();
	
	/**
	 * Bounding box used for {@link StargateMilkyWayBaseTile} search.
	 * Searches 3 blocks to the left/right and 7 blocks down.
	 */
	private static final AunisAxisAlignedBB BASE_SEARCH_BOX_SMALL = new AunisAxisAlignedBB(-3, -7, 0, 3, 0, 0);
	private static final AunisAxisAlignedBB BASE_SEARCH_BOX_LARGE = new AunisAxisAlignedBB(-5, -9, 0, 5, 0, 0);
	
	public static final BlockMatcher BASE_MATCHER = BlockMatcher.forBlock(AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK);
	public static final BlockMatcher MEMBER_MATCHER = BlockMatcher.forBlock(AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK);
		
	private static final List<BlockPos> RING_BLOCKS_SMALL = Arrays.asList(
			new BlockPos(1, 7, 0),
			new BlockPos(3, 5, 0),
			new BlockPos(3, 3, 0),
			new BlockPos(2, 1, 0),
			new BlockPos(-2, 1, 0),
			new BlockPos(-3, 3, 0),
			new BlockPos(-3, 5, 0),
			new BlockPos(-1, 7, 0));
	
	private static final List<BlockPos> CHEVRON_BLOCKS_SMALL = Arrays.asList(
			new BlockPos(2, 6, 0),
			new BlockPos(3, 4, 0),
			new BlockPos(3, 2, 0),
			new BlockPos(-3, 2, 0),
			new BlockPos(-3, 4, 0),
			new BlockPos(-2, 6, 0),
			new BlockPos(1, 0, 0),
			new BlockPos(-1, 0, 0),
			new BlockPos(0, 7, 0));
	
	private static final List<BlockPos> RING_BLOCKS_LARGE = Arrays.asList(
			new BlockPos(-1, 0, 0), 
			new BlockPos(-3, 1, 0),
			new BlockPos(-4, 3, 0), 
			new BlockPos(-5, 4, 0), 
			new BlockPos(-4, 6, 0), 
			new BlockPos(-4, 7, 0), 
			new BlockPos(-2, 9, 0), 
			new BlockPos(-1, 9, 0), 
			new BlockPos(1, 9, 0), 
			new BlockPos(2, 9, 0), 
			new BlockPos(4, 7, 0), 
			new BlockPos(4, 6, 0), 
			new BlockPos(5, 4, 0), 
			new BlockPos(4, 3, 0), 
			new BlockPos(3, 1, 0), 
			new BlockPos(1, 0, 0));
	
	private static final List<BlockPos> CHEVRON_BLOCKS_LARGE = Arrays.asList(
			new BlockPos(3, 8, 0), 
			new BlockPos(5, 5, 0), 
			new BlockPos(4, 2, 0), 
			new BlockPos(-4, 2, 0), 
			new BlockPos(-5, 5, 0), 
			new BlockPos(-3, 8, 0), 
			new BlockPos(2, 0, 0),
			new BlockPos(-2, 0, 0), 
			new BlockPos(0, 9, 0));
	
	@Override
	public List<BlockPos> getRingBlocks() {
		switch (AunisConfig.stargateSize) {
		case SMALL:
		case MEDIUM:
			return RING_BLOCKS_SMALL;
			
		case LARGE:
			return RING_BLOCKS_LARGE;
			
		default:
			return null;
		}
	}
	
	@Override
	public List<BlockPos> getChevronBlocks() {
		switch (AunisConfig.stargateSize) {
			case SMALL:
			case MEDIUM:
				return CHEVRON_BLOCKS_SMALL;
				
			case LARGE:
				return CHEVRON_BLOCKS_LARGE;
				
			default:
				return null;
		}
	}
	
	@Override
	public AunisAxisAlignedBB getBaseSearchBox() {
		switch (AunisConfig.stargateSize) {
			case SMALL:
			case MEDIUM:
				return BASE_SEARCH_BOX_SMALL;
				
			case LARGE:
				return BASE_SEARCH_BOX_LARGE;
				
			default:
				return null;
		}
	}
	
	@Override
	public boolean matchBase(IBlockState state) {
		return BASE_MATCHER.apply(state);
	}
	
	@Override
	public boolean matchMember(IBlockState state) {
		return MEMBER_MATCHER.apply(state);
	}
	
	protected boolean checkMemberBlock(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing, EnumMemberVariant variant) {
		IBlockState state = blockAccess.getBlockState(pos);
		
		return MEMBER_MATCHER.apply(state) &&
				state.getValue(AunisProps.FACING_HORIZONTAL) == facing &&
				state.getValue(AunisProps.MEMBER_VARIANT) == variant;
	}
	
	protected void updateMemberMergeStatus(World world, BlockPos checkPos, BlockPos basePos, EnumFacing baseFacing, boolean shouldBeMerged) {
		checkPos = checkPos.rotate(FacingToRotation.get(baseFacing)).add(basePos);
		IBlockState state = world.getBlockState(checkPos);
		
		if (MEMBER_MATCHER.apply(state)) {		
			StargateMilkyWayMemberTile memberTile = (StargateMilkyWayMemberTile) world.getTileEntity(checkPos);
			
			if ((shouldBeMerged && !memberTile.isMerged()) || (memberTile.isMerged() && memberTile.getBasePos().equals(basePos))) {
				
				ItemStack camoStack = memberTile.getCamoItemStack();
				if (camoStack != null) {
					InventoryHelper.spawnItemStack(world, checkPos.getX(), checkPos.getY(), checkPos.getZ(), camoStack);
					memberTile.setCamoState(null);
					
					TargetPoint point = new TargetPoint(world.provider.getDimension(), checkPos.getX(), checkPos.getY(), checkPos.getZ(), 512);
					AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(checkPos, StateTypeEnum.CAMO_STATE, memberTile.getState(StateTypeEnum.CAMO_STATE)), point);
				}
				
				memberTile.setBasePos(shouldBeMerged ? basePos : null);
				
				world.setBlockState(checkPos, state.withProperty(AunisProps.RENDER_BLOCK, !shouldBeMerged), 3);
			}
		}
	}
	
	/**
	 * Updates the {@link StargateMilkyWayBaseBlock} position of the
	 * {@link StargateMilkyWayMemberTile}.
	 * 
	 * @param blockAccess Usually {@link World}.
	 * @param pos Position of the currently updated {@link StargateMilkyWayMemberBlock}.
	 * @param basePos Position of {@link StargateMilkyWayBaseBlock} the tiles should be linked to.
	 * @param baseFacing Facing of {@link StargateMilkyWayBaseBlock}.
	 */
	private static void updateMemberBasePos(IBlockAccess blockAccess, BlockPos pos, BlockPos basePos, EnumFacing baseFacing) {
		IBlockState state = blockAccess.getBlockState(pos);

		if (MEMBER_MATCHER.apply(state)) {		
			StargateMilkyWayMemberTile memberTile = (StargateMilkyWayMemberTile) blockAccess.getTileEntity(pos);
			
			memberTile.setBasePos(basePos);
		}
	}
	
	/**
	 * Updates all {@link StargateMilkyWayMemberTile} to contain
	 * correct {@link StargateMilkyWayBaseBlock} position.
	 * 
	 * @param blockAccess Usually {@link World}.
	 * @param basePos Position of {@link StargateMilkyWayBaseBlock} the tiles should be linked to.
	 * @param baseFacing Facing of {@link StargateMilkyWayBaseBlock}.
	 */
	public void updateMembersBasePos(IBlockAccess blockAccess, BlockPos basePos, EnumFacing baseFacing) {
		for (BlockPos pos : getRingBlocks())
			updateMemberBasePos(blockAccess, pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), basePos, baseFacing);
		
		for (BlockPos pos : getChevronBlocks())
			updateMemberBasePos(blockAccess, pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), basePos, baseFacing);
	}
	
	/**
	 * Converts merged Stargate from old pattern (1.5)
	 * to new pattern (1.6).
	 * 
	 * @param world {@link World} instance.
	 * @param basePos Position of {@link StargateMilkyWayBaseBlock} the tiles should be linked to.
	 * @param baseFacing Facing of {@link StargateMilkyWayBaseBlock}.
	 * @param currentStargateSize Current Stargate size as read from NBT.
	 * @param targetStargateSize Target Stargate size as defined in config.
	 */
	public void convertToPattern(World world, BlockPos basePos, EnumFacing baseFacing, StargateSizeEnum currentStargateSize, StargateSizeEnum targetStargateSize) {
		Aunis.info(basePos + ": Converting Stargate from " + currentStargateSize + " to " + targetStargateSize);
		List<BlockPos> oldPatternBlocks = new ArrayList<BlockPos>();
		
		switch (currentStargateSize) {
			case SMALL:
			case MEDIUM:
				oldPatternBlocks.addAll(RING_BLOCKS_SMALL);
				oldPatternBlocks.addAll(CHEVRON_BLOCKS_SMALL);
				break;
				
			case LARGE:
				oldPatternBlocks.addAll(RING_BLOCKS_LARGE);
				oldPatternBlocks.addAll(CHEVRON_BLOCKS_LARGE);
				break;
		}
		
		for (BlockPos pos : oldPatternBlocks)
			world.setBlockToAir(pos.rotate(FacingToRotation.get(baseFacing)).add(basePos));
		
		IBlockState memberState = AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK.getDefaultState()
				.withProperty(AunisProps.FACING_HORIZONTAL, baseFacing)
				.withProperty(AunisProps.RENDER_BLOCK, false);
		
		for (BlockPos pos : getRingBlocks())
			world.setBlockState(pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), memberState.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING));
		
		for (BlockPos pos : getChevronBlocks())
			world.setBlockState(pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), memberState.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.CHEVRON));
	}
}
