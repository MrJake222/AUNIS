package mrjake.aunis.block;

import javax.annotation.Nullable;

import mrjake.aunis.AunisConfig;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.upgrade.UpgradeHelper;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DHDBlock extends BlockRotated {
	
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
	
	/*
	 * Late-future TODO:
	 * Rewrite upgrade system using GUIs not some stupid lazy-ass sides ;)
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack itemStack = playerIn.getHeldItemMainhand();
		
		if (!worldIn.isRemote) {
			if (hand == EnumHand.MAIN_HAND) {
				EnumFacing dhdFacingOpposite = EnumFacing.getHorizontal( Math.round(state.getValue(BlockRotated.ROTATE)/4.0f) );
								
				// Back side of block
				if (facing == dhdFacingOpposite) {
					ITileEntityUpgradeable upgradeable = (ITileEntityUpgradeable) worldIn.getTileEntity(pos);
	
					return UpgradeHelper.upgradeInteract((EntityPlayerMP) playerIn, upgradeable, itemStack);				
				}
				
				/*
				 * Check if player is clicking front of the DHD
				 * If so, check if control/energy crystal is in slot
				 * 	True: eject the crystal into player's inventory
				 * 	False: Check if holding the crystal, if true then insert it
				 */
				DHDTile dhdTile = (DHDTile) worldIn.getTileEntity(pos);
				ItemStackHandler itemStackHandler = (ItemStackHandler) dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				
				ItemStack slotItemStack = itemStackHandler.getStackInSlot(0);
				ItemStack heldItemStack = playerIn.getHeldItem(hand);
				
				float rotation = state.getValue(BlockRotated.ROTATE) * 360 / 16f;
				
				if (facing == EnumFacing.fromAngle(rotation).getOpposite()) {
					if (slotItemStack.isEmpty()) {
						if (heldItemStack.getItem() == AunisItems.crystalControlDhd) {
							// Insert the crystal
							
							ItemStack remainder = itemStackHandler.insertItem(0, heldItemStack, false);
							playerIn.setHeldItem(hand, remainder);
						}
					}
					
					else {
						if (heldItemStack.isEmpty())
							playerIn.setHeldItem(hand, slotItemStack);
						else
							playerIn.addItemStackToInventory(slotItemStack);
						
						itemStackHandler.setStackInSlot(0, ItemStack.EMPTY);
					}
				}
			}
		}
		
		// Client side
		else {
			return	itemStack.getItem() == AunisItems.crystalGlyphDhd || 
					itemStack.getItem() == AunisItems.crystalControlDhd ||
					itemStack.getItem() == Items.AIR;
		}
		
		return false;
	}
		
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		DHDTile dhdTile = (DHDTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {
			StargateBaseTile gateTile = dhdTile.getLinkedGate(world);
			
			if (gateTile != null)
				gateTile.setLinkedDHD(null);
			
			// Supports upgrades
			if (dhdTile instanceof ITileEntityUpgradeable) {			
				if (dhdTile.hasUpgrade() || dhdTile.getUpgradeRendererState().doInsertAnimation) {
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(AunisItems.crystalGlyphDhd));
				}
			}
		}
		
		super.breakBlock(world, pos, state);
	}


	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
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
