package mrjake.aunis.block;

import mrjake.aunis.tileentity.CrystalInfuserTile;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

public class CrystalInfuserBlock extends BlockFaced {

	public CrystalInfuserBlock() {
		super(Material.ROCK, SoundType.STONE, "crystal_infuser_block");
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public CrystalInfuserTile createTileEntity(World world, IBlockState state) {
		return new CrystalInfuserTile();
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
