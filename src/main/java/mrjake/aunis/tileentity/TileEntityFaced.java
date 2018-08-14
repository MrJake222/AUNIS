package mrjake.aunis.tileentity;

import javax.annotation.Nullable;

import mrjake.aunis.block.BlockFaced;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class TileEntityFaced<TE extends TileEntity> extends BlockFaced {

	public TileEntityFaced(Material material, SoundType sound, String name) {
		super(material, sound, name);
	}
	
	public abstract Class<TE> getTileEntityClass();
	
	@SuppressWarnings("unchecked")
	public TE getTileEntity(IBlockAccess world, BlockPos pos) {
		return (TE)world.getTileEntity(pos);
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Nullable
	@Override
	public abstract TE createTileEntity(World world, IBlockState state);
}
