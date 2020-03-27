package mrjake.aunis.block.stargate;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.gui.GuiIdEnum;
import mrjake.aunis.stargate.BoundingHelper;
import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseBaseTile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class StargateUniverseBaseBlock extends Block {

	private static final String BLOCK_NAME = "stargate_universe_base_block";
	
	public StargateUniverseBaseBlock() {		
		super(Material.IRON);
		
		setRegistryName(Aunis.ModID + ":" + BLOCK_NAME);
		setTranslationKey(Aunis.ModID + "." + BLOCK_NAME);
		
		setSoundType(SoundType.METAL); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH)
				.withProperty(AunisProps.RENDER_BLOCK, true));
		
		setLightOpacity(0);
		
		setHardness(3.0f);
		setHarvestLevel("pickaxe", 3);
	}
	
	// ------------------------------------------------------------------------
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AunisProps.FACING_HORIZONTAL, AunisProps.RENDER_BLOCK);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {		
		return (state.getValue(AunisProps.RENDER_BLOCK) ? 0x04 : 0) |
				state.getValue(AunisProps.FACING_HORIZONTAL).getHorizontalIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {		
		return getDefaultState()
				.withProperty(AunisProps.RENDER_BLOCK, (meta & 0x04) != 0)
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.byHorizontalIndex(meta & 0x03));
	}
	
	// ------------------------------------------------------------------------	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		StargateClassicBaseTile gateTile = (StargateClassicBaseTile) world.getTileEntity(pos);
		EnumFacing facing = placer.getHorizontalFacing().getOpposite();
		
		if (!world.isRemote) {			
			state = state.withProperty(AunisProps.FACING_HORIZONTAL, facing)
					.withProperty(AunisProps.RENDER_BLOCK, false);
		
			world.setBlockState(pos, state);
					
			gateTile.updateFacing(facing, true);
			// TODO Update merging
//			gateTile.updateMergeState(StargateMilkyWayMergeHelper.INSTANCE.checkBlocks(world, pos, facing), facing);
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {		
		StargateClassicBaseTile gateTile = (StargateClassicBaseTile) world.getTileEntity(pos);
						
		if (!world.isRemote) {
			gateTile.onBlockBroken();
		}
		
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!player.isSneaking()) {
			player.openGui(Aunis.instance, GuiIdEnum.GUI_STARGATE.id, world, pos.getX(), pos.getY(), pos.getZ());
			
			return true;
		}
		
		return false;
	}
	
	// ------------------------------------------------------------------------
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public StargateUniverseBaseTile createTileEntity(World world, IBlockState state) {
		return new StargateUniverseBaseTile();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		// Client side
		
		if ( state.getValue(AunisProps.RENDER_BLOCK) )
			return EnumBlockRenderType.MODEL;
		else
			return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
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
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return BoundingHelper.getStargateBlockBoundingBox(state, access, pos);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return BoundingHelper.getStargateBlockBoundingBox(state, access, pos);
	}
}
