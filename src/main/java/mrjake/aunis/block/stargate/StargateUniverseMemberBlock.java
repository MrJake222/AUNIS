package mrjake.aunis.block.stargate;

import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateUniverseMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseMemberTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class StargateUniverseMemberBlock extends StargateClassicMemberBlock {

	public StargateUniverseMemberBlock() {
		super("stargate_universe_member_block");
		setResistance(20.0f);
	}

	@Override
	protected StargateAbstractMergeHelper getMergeHelper() {
		return StargateUniverseMergeHelper.INSTANCE;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateUniverseMemberTile();
	}

}
