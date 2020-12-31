package mrjake.aunis.block.stargate;

import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class StargateUniverseBaseBlock extends StargateClassicBaseBlock {

	public StargateUniverseBaseBlock() {
		super("stargate_universe_base_block");
		setResistance(20.0f);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateUniverseBaseTile();
	}
}
