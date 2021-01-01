package mrjake.aunis.block.stargate;

import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class StargateMilkyWayBaseBlock extends StargateClassicBaseBlock {

	public static final String BLOCK_NAME = "stargate_milkyway_base_block";
	
	public StargateMilkyWayBaseBlock() {
		super(BLOCK_NAME);
		setResistance(2000.0f);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateMilkyWayBaseTile();
	}
}
