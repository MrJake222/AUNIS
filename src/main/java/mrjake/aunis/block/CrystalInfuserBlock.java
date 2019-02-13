package mrjake.aunis.block;

import mrjake.aunis.tileentity.CrystalInfuserTile;
import mrjake.aunis.tileentity.TileEntityFaced;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

public class CrystalInfuserBlock extends TileEntityFaced<CrystalInfuserTile> {

	public CrystalInfuserBlock() {
		super(Material.ROCK, SoundType.STONE, "crystal_infuser_block");
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public Class<CrystalInfuserTile> getTileEntityClass() {
		return CrystalInfuserTile.class;
	}

	@Override
	public CrystalInfuserTile createTileEntity(World world, IBlockState state) {
		return new CrystalInfuserTile();
	}
	
	
}
