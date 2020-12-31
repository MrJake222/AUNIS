package mrjake.aunis.block.stargate;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.gui.GuiIdEnum;
import mrjake.aunis.stargate.BoundingHelper;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public abstract class StargateClassicBaseBlock extends StargateBaseBlock {
	
	public StargateClassicBaseBlock(String blockName) {
		super(blockName);
	}
	
	// --------------------------------------------------------------------------------------
	// Interactions
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		StargateClassicBaseTile gateTile = (StargateClassicBaseTile) world.getTileEntity(pos);
		EnumFacing facing = placer.getHorizontalFacing().getOpposite();
		
		if (!world.isRemote) {			
			state = state.withProperty(AunisProps.FACING_HORIZONTAL, facing)
					.withProperty(AunisProps.RENDER_BLOCK, true);
		
			world.setBlockState(pos, state);
					
			gateTile.updateFacing(facing, true);
			gateTile.updateMergeState(gateTile.getMergeHelper().checkBlocks(world, pos, facing), facing);
		}
	}

	@Override
	protected void showGateInfo(EntityPlayer player, World world, BlockPos pos) {
		player.openGui(Aunis.instance, GuiIdEnum.GUI_STARGATE.id, world, pos.getX(), pos.getY(), pos.getZ());
	}

	// --------------------------------------------------------------------------------------
	// Rendering
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return BoundingHelper.getStargateBlockBoundingBox(state, access, pos);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return BoundingHelper.getStargateBlockBoundingBox(state, access, pos);
	}
}
