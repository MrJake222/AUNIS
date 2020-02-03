package mrjake.aunis.block;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.upgrade.ITileEntityUpgradeable;
import mrjake.aunis.upgrade.UpgradeHelper;
import mrjake.aunis.util.LinkingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DHDBlock extends Block {
	
	private static final String blockName = "dhd_block";
	
	public DHDBlock() {		
		super(Material.IRON);
		
		setRegistryName(Aunis.ModID + ":" + blockName);
		setTranslationKey(Aunis.ModID + "." + blockName);
		
		setSoundType(SoundType.METAL); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.ROTATION_HORIZONTAL, 0));
		
		setLightOpacity(0);
		
		setHardness(3.0f);
		setHarvestLevel("pickaxe", 3);
	}
	
	// ------------------------------------------------------------------------
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AunisProps.ROTATION_HORIZONTAL);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state
				.getValue(AunisProps.ROTATION_HORIZONTAL);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(AunisProps.ROTATION_HORIZONTAL, meta);
	}
	
	// ------------------------------------------------------------------------
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {		
		// Server side
		if ( !world.isRemote ) {
			int facing = MathHelper.floor( (double)((placer.rotationYaw) * 16.0F / 360.0F) + 0.5D ) & 0x0F;
			world.setBlockState(pos, state.withProperty(AunisProps.ROTATION_HORIZONTAL, facing), 3);
			
			DHDTile dhdTile = (DHDTile) world.getTileEntity(pos);
			BlockPos closestGate = LinkingHelper.findClosestUnlinked(world, pos, LinkingHelper.getDhdRange(), AunisBlocks.stargateMilkyWayBaseBlock);
			
			if (closestGate != null) {
				StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(closestGate);
				
				dhdTile.setLinkedGate(closestGate);
				gateTile.setLinkedDHD(pos);
			}	
		}
	}
	
	/*
	 * Maybe not-so-late-future:
	 * TODO Rewrite upgrade system using GUIs not some stupid lazy-ass sides ;)
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack itemStack = playerIn.getHeldItemMainhand();
		
		if (!worldIn.isRemote) {
			if (hand == EnumHand.MAIN_HAND) {
				EnumFacing dhdFacingOpposite = EnumFacing.byHorizontalIndex( Math.round(state.getValue(AunisProps.ROTATION_HORIZONTAL)/4.0f) );
								
				// Back side of block
				if (facing == dhdFacingOpposite) {
					DHDTile dhdTile = (DHDTile) worldIn.getTileEntity(pos);
					ItemStackHandler itemStackHandler = (ItemStackHandler) dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					
					ItemStack slotItemStack = itemStackHandler.getStackInSlot(0);
					ItemStack heldItemStack = playerIn.getHeldItem(hand);
					
					if (slotItemStack.isEmpty()) {
						if (heldItemStack.getItem() == AunisItems.crystalControlDhd) {
							// Insert the crystal
							
							ItemStack remainder = itemStackHandler.insertItem(0, heldItemStack, false);
							playerIn.setHeldItem(hand, remainder);
						}
						
						else {
							ITileEntityUpgradeable upgradeable = (ITileEntityUpgradeable) worldIn.getTileEntity(pos);
							
							return UpgradeHelper.upgradeInteract((EntityPlayerMP) playerIn, upgradeable, itemStack);
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
			StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) dhdTile.getLinkedGate(world);
			
			if (gateTile != null)
				gateTile.setLinkedDHD(null);
			
			// Supports upgrades
			if (dhdTile instanceof ITileEntityUpgradeable) {			
				if (dhdTile.hasUpgrade() || dhdTile.getUpgradeRendererState().doInsertAnimation) {
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(AunisItems.crystalGlyphDhd));
				}
			}
			
			ItemStack crystalItemStack = dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).extractItem(0, 1, false);
			
			if (!crystalItemStack.isEmpty()) {				
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), crystalItemStack);
			}
		}
		
		super.breakBlock(world, pos, state);
	}

	// ------------------------------------------------------------------------
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
