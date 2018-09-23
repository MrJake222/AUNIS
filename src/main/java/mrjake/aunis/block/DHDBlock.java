package mrjake.aunis.block;

import javax.annotation.Nullable;

import mrjake.aunis.AunisConfig;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.upgrade.UpgradeSlotInteractToClient;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.TileEntityRotated;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class DHDBlock extends TileEntityRotated<DHDTile> {
	
	public DHDBlock() {
		super(Material.IRON, SoundType.METAL, "dhd_block");
	}
	
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		
		super.onBlockPlacedBy(world, pos, state, placer, stack);		
		
		// Server side
		if ( !world.isRemote ) {
			
			// Find Stargate and create link
			
			BlockPos range = AunisConfig.dhdRange;
			DHDTile dhd = (DHDTile) world.getTileEntity(pos);
			
			for ( BlockPos sg : BlockPos.getAllInBox(pos.subtract(range), pos.add(range)) ) {
				IBlockState gateState = world.getBlockState(sg);
				
				if ( gateState.getBlock() instanceof StargateBaseBlock) {		
					StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(sg);					
					if ( !gateTile.isLinked() && !gateState.getValue(BlockTESRMember.RENDER) ) {
						dhd.setLinkedGate(sg);
						gateTile.setLinkedDHD(pos);
						break;
					}
				}
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {		
		if (!worldIn.isRemote && hand == EnumHand.MAIN_HAND) {
			EnumFacing dhdFacingOpposite = EnumFacing.getHorizontal( Math.round(state.getValue(BlockRotated.ROTATE)/4.0f) );
			
			// Back side of block
			if (facing == dhdFacingOpposite) {
				DHDTile dhdTile = (DHDTile) worldIn.getTileEntity(pos);
								
				ItemStack itemStack = playerIn.getHeldItemMainhand();								
				boolean hasUpgrade = dhdTile.hasUpgrade();
				boolean isHoldingUpgrade = itemStack.getItem() == AunisItems.dhdControlCrystal;
				
				if (!dhdTile.getInsertAnimation()) {
					// Reduce ItemStack
					if (!hasUpgrade && isHoldingUpgrade)
						playerIn.setHeldItem(hand, new ItemStack(itemStack.getItem(), itemStack.getCount()-1) );
					
					dhdTile.setInsertAnimation(true);
				}
				
				AunisPacketHandler.INSTANCE.sendToAllAround( new UpgradeSlotInteractToClient(pos, hasUpgrade, isHoldingUpgrade), new TargetPoint(worldIn.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512) );
			}
		}
		
		return true;
	}
		
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		DHDTile dhdTile = (DHDTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {
			StargateBaseTile gateTile = dhdTile.getLinkedGate(world);
			
			if (gateTile != null)
				gateTile.setLinkedDHD(null);
			
			if (dhdTile.hasUpgrade() || dhdTile.getInsertAnimation()) {
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(AunisItems.dhdControlCrystal));
			}
		}
		
		super.breakBlock(world, pos, state);
	}
	
	
	
	@Override
	public Class<DHDTile> getTileEntityClass() {
		return DHDTile.class;
	}

	@Override
	public DHDTile createTileEntity(World world, IBlockState state) {
		return new DHDTile();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
    	return new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75);
    }
}
