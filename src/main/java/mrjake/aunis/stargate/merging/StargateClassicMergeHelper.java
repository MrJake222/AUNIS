package mrjake.aunis.stargate.merging;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.stargate.StargateMilkyWayBaseBlock;
import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public abstract class StargateClassicMergeHelper extends StargateAbstractMergeHelper {
	
	protected boolean checkMemberBlock(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing, EnumMemberVariant variant) {
		IBlockState state = blockAccess.getBlockState(pos);
		
		return matchMember(state) &&
				state.getValue(AunisProps.FACING_HORIZONTAL) == facing &&
				state.getValue(AunisProps.MEMBER_VARIANT) == variant;
	}
	
	protected void updateMemberMergeStatus(World world, BlockPos checkPos, BlockPos basePos, EnumFacing baseFacing, boolean shouldBeMerged) {
		checkPos = checkPos.rotate(FacingToRotation.get(baseFacing)).add(basePos);
		IBlockState state = world.getBlockState(checkPos);
		
		if (matchMember(state)) {		
			StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(checkPos);
			
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
	private void updateMemberBasePos(IBlockAccess blockAccess, BlockPos pos, BlockPos basePos, EnumFacing baseFacing) {
		IBlockState state = blockAccess.getBlockState(pos);

		if (matchMember(state)) {		
			StargateClassicMemberTile memberTile = (StargateClassicMemberTile) blockAccess.getTileEntity(pos);
			
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
}
