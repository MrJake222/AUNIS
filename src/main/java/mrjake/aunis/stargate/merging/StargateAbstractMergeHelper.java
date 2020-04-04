package mrjake.aunis.stargate.merging;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mrjake.aunis.block.stargate.StargateMilkyWayBaseBlock;
import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class StargateAbstractMergeHelper {
	
	/**
	 * @return {@link List} of {@link BlockPos} pointing to ring blocks.
	 */
	@Nonnull
	public abstract List<BlockPos> getRingBlocks();
	
	/**
	 * @return {@link List} of {@link BlockPos} pointing to chevron blocks.
	 */
	@Nonnull
	public abstract List<BlockPos> getChevronBlocks();
	
	/**
	 * @return Max box where to search for the base.
	 */
	public abstract AunisAxisAlignedBB getBaseSearchBox();
	
	/**
	 * @param state State of the block for the check.
	 * @return True if the {@link IBlockState} represents the Base block, false otherwise.
	 */
	public abstract boolean matchBase(IBlockState state);
	
	/**
	 * @param state State of the block for the check.
	 * @return True if the {@link IBlockState} represents the Member block, false otherwise.
	 */
	public abstract boolean matchMember(IBlockState state);
	
	/**
	 * @return Member block.
	 */
	public abstract Block getMemberBlock();
	
	/**
	 * Method searches for a {@link StargateMilkyWayBaseBlock} within {@link this#BASE_SEARCH_BOX}
	 * and returns it's {@link TileEntity}.
	 * 
	 * @param blockAccess Usually {@link World}.
	 * @param memberPos Starting position.
	 * @param facing Facing of the member blocks.
	 * @return {@link StargateMilkyWayBaseTile} if found, {@code null} otherwise.
	 */
	@Nullable
	public StargateAbstractBaseTile findBaseTile(IBlockAccess blockAccess, BlockPos memberPos, EnumFacing facing) {
		AunisAxisAlignedBB globalBox = getBaseSearchBox().rotate(facing).offset(memberPos);
		
		for (MutableBlockPos pos : BlockPos.getAllInBoxMutable(globalBox.getMinBlockPos(), globalBox.getMaxBlockPos())) {
			if (matchBase(blockAccess.getBlockState(pos))) {
				return (StargateAbstractBaseTile) blockAccess.getTileEntity(pos.toImmutable());
			}
		}
		
		return null;
	}
	
	/**
	 * Check the given {@link BlockPos} for the {@link StargateMilkyWayMemberBlock},
	 * it's correct variant and facing.
	 * 
	 * @param blockAccess Usually {@link World}.
	 * @param pos {@link BlockPos} to be checked.
	 * @param facing Expected {@link EnumFacing}.
	 * @param variant Expected {@link EnumMemberVariant}.
	 * @return {@code true} if the block matches given parameters, {@code false} otherwise.
	 */
	protected abstract boolean checkMemberBlock(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing, EnumMemberVariant variant);
	
	/**
	 * Called on block placement. Checks the found base block
	 * for other Stargate blocks and returns the result.
	 * 
	 * @param blockAccess Usually {@link World}.
	 * @param basePos Found {@link StargateMilkyWayBaseBlock}.
	 * @param baseFacing Current base facing.
	 * @return {@code true} if the structure matches, {@code false} otherwise.
	 */
	public boolean checkBlocks(IBlockAccess blockAccess, BlockPos basePos, EnumFacing baseFacing) {		
		if (AunisConfig.debugConfig.checkGateMerge) {	
			for (BlockPos pos : getRingBlocks()) {
				if (!checkMemberBlock(blockAccess, pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), baseFacing, EnumMemberVariant.RING))
					return false;
			}
			
			for (BlockPos pos : getChevronBlocks()) {
				if (!checkMemberBlock(blockAccess, pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), baseFacing, EnumMemberVariant.CHEVRON))
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Updates merge status of the given block. Block is internally
	 * checked by {@link this#MEMBER_MATCHER}.
	 * 
	 * @param world {@link World} instance.
	 * @param checkPos Position of the currently checked {@link StargateMilkyWayMemberBlock}.
	 * @param basePos Position of {@link StargateMilkyWayBaseBlock} the tiles should be linked to.
	 * @param baseFacing Facing of the base block
	 * @param shouldBeMerged {@code true} if the structure is merging, false otherwise.
	 */
	protected abstract void updateMemberMergeStatus(World world, BlockPos checkPos, BlockPos basePos, EnumFacing baseFacing, boolean shouldBeMerged);
	
	/**
	 * Updates merge status of the Stargate.
	 * 
	 * @param world {@link World} instance.
	 * @param basePos Position of {@link StargateMilkyWayBaseBlock} the tiles should be linked to.
	 * @param baseFacing Facing of {@link StargateMilkyWayBaseBlock}.
	 * @param shouldBeMerged {@code true} if the structure is merging, false otherwise.
	 */
	public void updateMembersMergeStatus(World world, BlockPos basePos, EnumFacing baseFacing, boolean shouldBeMerged) {
		for (BlockPos pos : getRingBlocks())
			updateMemberMergeStatus(world, pos, basePos, baseFacing, shouldBeMerged);
		
		for (BlockPos pos : getChevronBlocks())
			updateMemberMergeStatus(world, pos, basePos, baseFacing, shouldBeMerged);
	}}
