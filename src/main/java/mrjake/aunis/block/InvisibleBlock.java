package mrjake.aunis.block;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class InvisibleBlock extends Block {

	private static final String blockName = "invisible_block";
	
	public InvisibleBlock() {
		super(Material.AIR);
		
		setRegistryName(Aunis.ModID + ":" + blockName);
		setUnlocalizedName(Aunis.ModID + "." + blockName);
		
		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.HAS_COLLISIONS, true));
		
		setLightLevel(1.0f);
		setLightOpacity(0);
	}
	
	// ------------------------------------------------------------------------
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AunisProps.HAS_COLLISIONS);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state
				.getValue(AunisProps.HAS_COLLISIONS) ? 0x01 : 0x00;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(AunisProps.HAS_COLLISIONS, (meta & 0x01) == 1);
	}
	
	// ------------------------------------------------------------------------
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
	
	// ------------------------------------------------------------------------
	@Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(AunisProps.HAS_COLLISIONS))		
        	return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        else
        	return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    	if (state.getValue(AunisProps.HAS_COLLISIONS))		
        	return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        else
        	return null;
    }
}
