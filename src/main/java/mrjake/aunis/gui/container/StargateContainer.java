package mrjake.aunis.gui.container;

import mrjake.aunis.gui.util.ContainerHelper;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class StargateContainer extends Container {

	public StargateAbstractBaseTile gateTile;
	public int powerTier;
	
	private BlockPos pos;
	
	public StargateContainer(IInventory playerInventory, World world, int x, int y, int z) {
		pos = new BlockPos(x, y, z);
		gateTile = (StargateAbstractBaseTile) world.getTileEntity(pos);
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
        }
        
		// Transfering from player's inventory to DHD
        else {
        	// Capacitors
//        	if (stack.getItem() == AunisItems.crystalControlDhd) {
//        		if (!slotCrystal.getHasStack()) {
//        			slotCrystal.putStack(stack.copy());
//        			stack.shrink(1);
//        		}
//        	}
//        	
//        	else 
        	
        	if (StargateMilkyWayBaseTile.SUPPORTED_UPGRADES.contains(stack.getItem())) {
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
	}
	
	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		
		if (listener instanceof EntityPlayerMP)
			AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_STATE, new StargateContainerGuiState(gateTile.gateAddress, false, 0, 0, 0, 0)), (EntityPlayerMP) listener);
	}
}
