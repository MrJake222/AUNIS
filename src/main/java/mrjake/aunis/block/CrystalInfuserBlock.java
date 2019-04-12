package mrjake.aunis.block;

import javax.annotation.Nullable;

import mrjake.aunis.item.AunisItems;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class CrystalInfuserBlock extends BlockFaced {

	public CrystalInfuserBlock() {
		super(Material.ROCK, SoundType.STONE, "crystal_infuser_block");
	}

	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			CrystalInfuserTile infTile = (CrystalInfuserTile) worldIn.getTileEntity(pos);
			ItemStackHandler itemStackHandler = (ItemStackHandler) infTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			
			ItemStack slotItemStack = itemStackHandler.getStackInSlot(0);
			ItemStack heldItemStack = playerIn.getHeldItem(hand);
					
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
				
				return true;
			}
		}
		
		return playerIn.getHeldItem(hand).getItem() == AunisItems.crystalControlDhd;
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		CrystalInfuserTile infuserTile = (CrystalInfuserTile) worldIn.getTileEntity(pos);
		
		if (!worldIn.isRemote) {
			ItemStack crystalItemStack = infuserTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).extractItem(0, 1, false);
			
			if (!crystalItemStack.isEmpty()) {				
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), crystalItemStack);
			}
		}
		
		super.breakBlock(worldIn, pos, state);
	}
	
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public CrystalInfuserTile createTileEntity(World world, IBlockState state) {
		return new CrystalInfuserTile();
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
        return new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.65, 0.75);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
    	return new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.65, 0.75);
    }
}
