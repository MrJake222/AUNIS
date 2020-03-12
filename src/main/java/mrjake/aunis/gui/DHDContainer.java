package mrjake.aunis.gui;

import mrjake.aunis.Aunis;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class DHDContainer extends Container {

	public DHDContainer(IInventory playerInventory, World world, int x, int y, int z) {
		addPlayerSlots(playerInventory);
		
		DHDTile dhdTile = (DHDTile) world.getTileEntity(new BlockPos(x, y, z));
		IItemHandler itemHandler = dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		
		addSlotToContainer(new SlotItemHandler(itemHandler, 0, 80, 35));
		
		for (int row=0; row<2; row++) {
			for (int col=0; col<2; col++) {				
				addSlotToContainer(new SlotItemHandler(itemHandler, row*2+col+1, 9+18*col, 18+18*row));
			}
		}
	}
	
	private void addPlayerSlots(IInventory playerInventory) {		
		addSlotRow(playerInventory, 0, 8, 18, 142);
				
		for (int row=0; row<3; row++) {
			addSlotRow(playerInventory, 9*(row+1), 8, 18, 84 + 18*row);
		}
	}
	
	private void addSlotRow(IInventory playerInventory, int firstIndex, int xStart, int xOffset, int y) {		
		for (int col=0; col<9; col++) {
			addSlotToContainer(new Slot(playerInventory, firstIndex+col, 18*col + 8, y));
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}
