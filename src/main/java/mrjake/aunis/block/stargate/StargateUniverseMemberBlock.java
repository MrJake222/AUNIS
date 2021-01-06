package mrjake.aunis.block.stargate;

import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateUniverseMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateUniverseMemberTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class StargateUniverseMemberBlock extends StargateClassicMemberBlock {

	public static final String BLOCK_NAME = "stargate_universe_member_block";
	
	public final int RING_META = getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING));
	public final int CHEVRON_META = getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.CHEVRON));
	
	public StargateUniverseMemberBlock() {
		super(BLOCK_NAME);
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
