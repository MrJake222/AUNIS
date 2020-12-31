package mrjake.aunis.block.stargate;

import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateMilkyWayMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class StargateMilkyWayMemberBlock extends StargateClassicMemberBlock {

	public static final String BLOCK_NAME = "stargate_milkyway_member_block";
	
	public StargateMilkyWayMemberBlock() {
		super(BLOCK_NAME);
		setResistance(2000.0f);
	}

	@Override
	protected StargateAbstractMergeHelper getMergeHelper() {
		return StargateMilkyWayMergeHelper.INSTANCE;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateMilkyWayMemberTile();
	}
}
