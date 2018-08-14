package mrjake.aunis.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockRotated extends BlockBase {
	public static final PropertyInteger ROTATE = PropertyInteger.create("rotate", 0, 15);	

	public BlockRotated(Material material, SoundType sound, String name) {
		super(material, sound, name);
		
        setDefaultState(blockState.getBaseState().withProperty(ROTATE, 0));
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		int facing = MathHelper.floor( (double)((placer.rotationYaw) * 16.0F / 360.0F) + 0.5D ) & 0x0F;
		world.setBlockState(pos, state.withProperty(ROTATE, facing), 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ROTATE, meta & 0x0F);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ROTATE);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ROTATE);
	}
}
