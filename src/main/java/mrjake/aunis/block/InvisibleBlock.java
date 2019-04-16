package mrjake.aunis.block;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class InvisibleBlock extends Block {

	private static final String blockName = "invisible_block";
	
	public InvisibleBlock() {
		super(Material.ROCK);
		
		setRegistryName(Aunis.ModID + ":" + blockName);
		setUnlocalizedName(Aunis.ModID + "." + blockName);
		
		setLightLevel(1.0f);
		
//		setSoundType(SoundType.GLASS); 
//		setCreativeTab(Aunis.aunisCreativeTab);
	}
	
//	@Override
//	protected BlockStateContainer createBlockState() {
//		return new BlockStateContainer(this, AunisProps.LIGHT_LEVEL);
//	}
//	
//	@Override
//	public int getMetaFromState(IBlockState state) {		
//		return state.getValue(AunisProps.LIGHT_LEVEL);
//	}
//	
//	@Override
//	public IBlockState getStateFromMeta(int meta) {		
//		return getDefaultState()
//				.withProperty(AunisProps.LIGHT_LEVEL, meta);
//	}
		
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return false;
	}
	
//	@Override
//	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
//		return state.getValue(AunisProps.LIGHT_LEVEL);
//	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
}
