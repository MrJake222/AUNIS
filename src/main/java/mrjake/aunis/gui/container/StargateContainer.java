package mrjake.aunis.gui.container;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.gui.util.ContainerHelper;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.stargate.StargateClassicEnergyStorage;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile.StargateUpgradeEnum;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class StargateContainer extends Container {

	public StargateClassicBaseTile gateTile;
	
	private BlockPos pos;
	private int lastEnergyStored;
	
	public StargateContainer(IInventory playerInventory, World world, int x, int y, int z) {
		pos = new BlockPos(x, y, z);
		gateTile = (StargateClassicBaseTile) world.getTileEntity(pos);
		IItemHandler itemHandler = gateTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		
		// Upgrades 2x2 (index 0-3)
		for (int row=0; row<2; row++) {
			for (int col=0; col<2; col++) {				
				addSlotToContainer(new SlotItemHandler(itemHandler, row*2+col, 9+18*col, 18+18*row));
			}
		}
		
		// Capacitors 1x3 (index 4-6)
		for (int col=0; col<3; col++) {				
			addSlotToContainer(new SlotItemHandler(itemHandler, col+4, 115+18*col, 40));
		}
		
		for (Slot slot : ContainerHelper.generatePlayerSlots(playerInventory, 86))
			addSlotToContainer(slot);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack stack = getSlot(index).getStack();
		
		// Transfering from Stargate to player's inventory
        if (index < 7) {
        	if (!mergeItemStack(stack, 7, inventorySlots.size(), false)) {
        		return ItemStack.EMPTY;
        	}
        	
			putStackInSlot(index, ItemStack.EMPTY);
        }
        
		// Transfering from player's inventory to DHD
        else {
        	// Capacitors
        	if (stack.getItem() == Item.getItemFromBlock(AunisBlocks.CAPACITOR_BLOCK)) {
        		for (int i=4; i<7; i++) {
        			if (!getSlot(i).getHasStack()) {
        				ItemStack stack1 = stack.copy();
        				stack1.setCount(1);

	        			putStackInSlot(i, stack1);
	        			stack.shrink(1);
	        			
	        			return stack;
	        		}
        		}
        	}
        	
        	else 
        	
        	if (StargateUpgradeEnum.contains(stack.getItem())) {
        		for (int i=0; i<4; i++) {
        			if (!getSlot(i).getHasStack()) {
        				ItemStack stack1 = stack.copy();
        				stack1.setCount(1);
        				
        				putStackInSlot(i, stack1);
        				stack.shrink(1);
        				
        				return stack;
        			}
        		}
        	}
        	
        	return ItemStack.EMPTY;
        }
        
        return stack;
    }

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		StargateClassicEnergyStorage energyStorage = (StargateClassicEnergyStorage) gateTile.getCapability(CapabilityEnergy.ENERGY, null);
		
		if (lastEnergyStored != energyStorage.getEnergyStoredInternally()) {
			for (IContainerListener listener : listeners) {
				if (listener instanceof EntityPlayerMP) {
					AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_UPDATE, new StargateContainerGuiUpdate(energyStorage.getEnergyStoredInternally())), (EntityPlayerMP) listener);
				}
			}
		}
	}
	
	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		
		if (listener instanceof EntityPlayerMP)
			AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_STATE, new StargateContainerGuiState(gateTile.gateAddress, false, 0, 0, 0, 0)), (EntityPlayerMP) listener);
	}
}
