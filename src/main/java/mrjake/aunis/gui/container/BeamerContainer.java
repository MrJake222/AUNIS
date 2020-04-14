package mrjake.aunis.gui.container;

import mrjake.aunis.beamer.BeamerModeEnum;
import mrjake.aunis.beamer.BeamerRoleEnum;
import mrjake.aunis.gui.util.ContainerHelper;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.BeamerTile;
import mrjake.aunis.tileentity.util.RedstoneModeEnum;
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
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BeamerContainer extends Container {

	public BeamerTile beamerTile;
	public SlotItemHandler slotCrystal;
	public FluidTank tank;
	
	public BlockPos pos;
	private int lastEnergyStored;
	private int energyTransferedLastTick;
	private int lastFluidStored;
	private boolean lastLinked;
	private BeamerRoleEnum lastRole;
	private RedstoneModeEnum lastRedstoneMode;
	private int lastStart;
	private int lastStop;
	private int lastIn;
	
	public BeamerContainer(IInventory playerInventory, World world, int x, int y, int z) {
		pos = new BlockPos(x, y, z);
		beamerTile = (BeamerTile) world.getTileEntity(pos);
		IItemHandler itemHandler = beamerTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		
		slotCrystal = new SlotItemHandler(itemHandler, 0, 80, 30);
		addSlotToContainer(slotCrystal);
		
		tank = (FluidTank) beamerTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
		
		for (int row=0; row<2; row++) {
			for (int col=0; col<2; col++) {				
				addSlotToContainer(new SlotItemHandler(itemHandler, row*2+col+1, 9+18*col, 18+18*row));
			}
		}
		
		for (Slot slot : ContainerHelper.generatePlayerSlots(playerInventory, 86))
			addSlotToContainer(slot);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack stack = getSlot(index).getStack();
		Item item = stack.getItem();
		
		// Transfering from Beamer to player's inventory
        if (index < 5) {
        	if (!mergeItemStack(stack, 5, inventorySlots.size(), false)) {
        		return ItemStack.EMPTY;
        	}
        	
        	putStackInSlot(index, ItemStack.EMPTY);
        }
        
		// Transfering from player's inventory to Beamer
        else {
        	if ((item == AunisItems.BEAMER_CRYSTAL_POWER || item == AunisItems.BEAMER_CRYSTAL_FLUID || item == AunisItems.BEAMER_CRYSTAL_ITEMS) && !slotCrystal.getHasStack()) {
        		if (!slotCrystal.getHasStack()) {
        			ItemStack stack1 = stack.copy();
    				stack1.setCount(1);
        			slotCrystal.putStack(stack1);
        			
    				stack.shrink(1);
        		}
        	}
        	
        	else if (beamerTile.getMode() == BeamerModeEnum.ITEMS) {
        		mergeItemStack(stack, 1, 5, false);
        	}
        	
        	return ItemStack.EMPTY;
        }
        
        return stack;
    }
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		StargateAbstractEnergyStorage energyStorage = (StargateAbstractEnergyStorage) beamerTile.getCapability(CapabilityEnergy.ENERGY, null);
		
		if (lastEnergyStored != energyStorage.getEnergyStored() || energyTransferedLastTick != beamerTile.getEnergyTransferredLastTick() || lastFluidStored != tank.getFluidAmount() || lastLinked != beamerTile.isLinked() || lastRole != beamerTile.getRole() || lastRedstoneMode != beamerTile.getRedstoneMode() || lastStart != beamerTile.getStart() || lastStop != beamerTile.getStop() || lastIn != beamerTile.getInactivity()) {
			for (IContainerListener listener : listeners) {
				if (listener instanceof EntityPlayerMP) {
					AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_UPDATE, beamerTile.getState(StateTypeEnum.GUI_UPDATE)), (EntityPlayerMP) listener);
				}
			}
			
			lastEnergyStored = energyStorage.getEnergyStored();
			energyTransferedLastTick = beamerTile.getEnergyTransferredLastTick();
			lastFluidStored = tank.getFluidAmount();
			lastLinked = beamerTile.isLinked();
			lastRole = beamerTile.getRole();
			lastRedstoneMode = beamerTile.getRedstoneMode();
			lastStart = beamerTile.getStart();
			lastStop = beamerTile.getStop();
			lastIn = beamerTile.getInactivity();
		}
	}
	
	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		
		if (listener instanceof EntityPlayerMP) {
			AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_UPDATE, beamerTile.getState(StateTypeEnum.GUI_UPDATE)), (EntityPlayerMP) listener);
		}
	} 
}
