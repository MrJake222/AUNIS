package mrjake.aunis.block;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.raycaster.RaycasterRingsController;
import mrjake.aunis.tileentity.TRControllerTile;
import mrjake.aunis.tileentity.TransportRingsTile;
import mrjake.aunis.util.LinkingHelper;
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

public class TRControllerBlock extends Block {
	
	private static final String blockName = "transportrings_controller_block";
	
	public TRControllerBlock() {	
		super(Material.ROCK);
		
		setRegistryName(Aunis.ModID + ":" + blockName);
		setTranslationKey(Aunis.ModID + "." + blockName);
		
		setSoundType(SoundType.STONE); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH));
		
		setLightOpacity(0);
		
		setHardness(3.0f);
		setHarvestLevel("pickaxe", 3);
	}
	
	// ------------------------------------------------------------------------
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AunisProps.FACING_HORIZONTAL);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {		
		return state.getValue(AunisProps.FACING_HORIZONTAL).getHorizontalIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {		
		return getDefaultState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.byHorizontalIndex(meta & 0x03));
	}
	
	
	// ------------------------------------------------------------------------	
	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		return super.canPlaceBlockOnSide(worldIn, pos, side) && AunisProps.FACING_HORIZONTAL.getAllowedValues().contains(side);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState().withProperty(AunisProps.FACING_HORIZONTAL, facing);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		EnumFacing backFacing = state.getValue(AunisProps.FACING_HORIZONTAL).getOpposite();
		
		if (world.isAirBlock(pos.offset(backFacing))) {
			this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
		}
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {		
		TRControllerTile controllerTile = (TRControllerTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {			
			BlockPos closestRings = LinkingHelper.findClosestUnlinked(world, pos, new BlockPos(10, 5, 10), AunisBlocks.TRANSPORT_RINGS_BLOCK);
			
			if (closestRings != null) {
				TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(closestRings);
				
				controllerTile.setLinkedRings(closestRings);
				ringsTile.setLinkedController(pos);
			}
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TRControllerTile controllerTile = (TRControllerTile) world.getTileEntity(pos);
		
		if (!world.isRemote && controllerTile.isLinked())
			controllerTile.getLinkedRingsTile(world).setLinkedController(null);
		
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {		
		
//		TRControllerTile controllerTile = (TRControllerTile) world.getTileEntity(pos);
//		ItemStack heldItemStack = player.getHeldItem(hand);
//		
//		if (heldItemStack.getItem() == AunisItems.analyzerAncient) {
//			if (!world.isRemote) {
//				TransportRingsTile ringsTile = controllerTile.getLinkedRingsTile(world);
//				
//				if (ringsTile != null) {
//					AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(ringsTile.getPos(), StateTypeEnum.GUI_STATE, ringsTile.getState(StateTypeEnum.GUI_STATE)), (EntityPlayerMP) player);
//				}				
//			}
//		}
		
		if (world.isRemote) {
			if (hand == EnumHand.MAIN_HAND)
				RaycasterRingsController.INSTANCE.onActivated(world, pos, player);
		}
		
		return false;
	}
	
	// ------------------------------------------------------------------------
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TRControllerTile createTileEntity(World world, IBlockState state) {
		return new TRControllerTile();
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(AunisProps.FACING_HORIZONTAL)) {
	        case NORTH:
	        	return new AxisAlignedBB(0.15, 0, 1, 0.85, 1, 0.85);
	        	
	        case SOUTH:
	        	return new AxisAlignedBB(0.15, 0, 0.15, 0.85, 1, 0);
	        	
        	case WEST:
        		return new AxisAlignedBB(0.85, 0, 0.15, 1, 1, 0.85);
        		
        	default:
        		return new AxisAlignedBB(0, 0, 0.15, 0.15, 1, 0.85);
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
    	switch (blockState.getValue(AunisProps.FACING_HORIZONTAL)) {
	        case NORTH:
	        	return new AxisAlignedBB(0.15, 0, 1, 0.85, 1, 0.85);
	        	
	        case SOUTH:
	        	return new AxisAlignedBB(0.15, 0, 0.15, 0.85, 1, 0);
	        	
	    	case WEST:
	    		return new AxisAlignedBB(0.85, 0, 0.15, 1, 1, 0.85);
	    		
	    	default:
	    		return new AxisAlignedBB(0, 0, 0.15, 0.15, 1, 0.85);
	    }
    }
}
