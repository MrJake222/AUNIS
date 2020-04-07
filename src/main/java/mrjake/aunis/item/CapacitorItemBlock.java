package mrjake.aunis.item;

import java.util.List;

import mrjake.aunis.block.CapacitorBlock;
import mrjake.aunis.capability.CapacitorCapabilityProvider;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class CapacitorItemBlock extends ItemBlock {

	public CapacitorItemBlock(Block block) {
		super(block);
		
		setRegistryName(CapacitorBlock.BLOCK_NAME);
		setMaxStackSize(1);
		setHasSubtypes(true);
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			items.add(new ItemStack(this));
			
			ItemStack stack = new ItemStack(this);
			StargateAbstractEnergyStorage energyStorage = (StargateAbstractEnergyStorage) stack.getCapability(CapabilityEnergy.ENERGY, null);
			energyStorage.setEnergyStored(energyStorage.getMaxEnergyStored());
			items.add(stack);
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
		IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
		
		String energy = String.format("%,d", energyStorage.getEnergyStored());
		String capacity = String.format("%,d", energyStorage.getMaxEnergyStored());
		
		tooltip.add(energy + " / " + capacity + " RF");
		
		String energyPercent = String.format("%.2f", energyStorage.getEnergyStored()/(float)energyStorage.getMaxEnergyStored() * 100) + " %";
		tooltip.add(energyPercent);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CapacitorCapabilityProvider();
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);

		return 1 - (energyStorage.getEnergyStored() / (double)energyStorage.getMaxEnergyStored());
	}
	
	@Override
	public boolean getShareTag() {
		return true;
	}
	
	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack) {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setInteger("energy", stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored());
		
		return compound;
	}
	
	@Override
	public void readNBTShareTag(ItemStack stack, NBTTagCompound compound) {
		if (compound != null) {
			((StargateAbstractEnergyStorage) stack.getCapability(CapabilityEnergy.ENERGY, null)).setEnergyStored(compound.getInteger("energy"));
		}
	}
}
