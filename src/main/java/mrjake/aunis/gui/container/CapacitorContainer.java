package mrjake.aunis.gui.container;

import mrjake.aunis.gui.util.ContainerHelper;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.CapacitorTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

public class CapacitorContainer extends Container {

	public CapacitorTile capTile;
	
	private BlockPos pos;
	private int lastEnergyStored;
	private int energyTransferedLastTick;
	
	public CapacitorContainer(IInventory playerInventory, World world, int x, int y, int z) {
		pos = new BlockPos(x, y, z);
		capTile = (CapacitorTile) world.getTileEntity(pos);
		
		for (Slot slot : ContainerHelper.generatePlayerSlots(playerInventory, 86))
			addSlotToContainer(slot);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		StargateAbstractEnergyStorage energyStorage = (StargateAbstractEnergyStorage) capTile.getCapability(CapabilityEnergy.ENERGY, null);

		if (lastEnergyStored != energyStorage.getEnergyStored() || energyTransferedLastTick != capTile.getEnergyTransferedLastTick()) {
			for (IContainerListener listener : listeners) {
				if (listener instanceof EntityPlayerMP) {
					AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_UPDATE, capTile.getState(StateTypeEnum.GUI_UPDATE)), (EntityPlayerMP) listener);
				}
			}
			
			lastEnergyStored = energyStorage.getEnergyStored();
			energyTransferedLastTick = capTile.getEnergyTransferedLastTick();
		}
	}
}
