package mrjake.aunis.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockTESRMember extends BlockFaced {
	public static final PropertyBool RENDER = PropertyBool.create("render");	

	public BlockTESRMember(Material material, SoundType sound, String name) {
		super(material, sound, name);
		
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(RENDER, true));
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(RENDER, true), 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		// return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 0x07)).withProperty(RENDER, (meta & 0x08) != 0);
		return super.getStateFromMeta(meta).withProperty(RENDER, (meta & 0x08) != 0);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		// return state.getValue(FACING).getIndex() | (state.getValue(RENDER) ? 0x08 : 0);
		return super.getMetaFromState(state) | (state.getValue(RENDER) ? 0x08 : 0);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, RENDER);
	}
}
