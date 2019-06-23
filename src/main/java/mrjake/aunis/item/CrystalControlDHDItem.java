package mrjake.aunis.item;

import java.text.DecimalFormat;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.capability.CrystalControlDHDCapabilityProvider;
import mrjake.aunis.capability.EnergyStorageUncapped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class CrystalControlDHDItem extends Item {
	
	public static final String itemName = "crystal_control_dhd";
	
	public CrystalControlDHDItem() {		
		setRegistryName(Aunis.ModID + ":" + itemName);
		setTranslationKey(Aunis.ModID + "." + itemName);
		
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setMaxStackSize(1);
		setNoRepair();
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack itemStack = player.getHeldItem(hand);

		if (AunisConfig.debugConfig.allowHandCrystalCharging) {
			if (!world.isRemote) {
				IEnergyStorage energyStorage = itemStack.getCapability(CapabilityEnergy.ENERGY, null);
							
				((EnergyStorageUncapped) energyStorage).setEnergyStored(energyStorage.getMaxEnergyStored());
			}
			
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {		
		return new CrystalControlDHDCapabilityProvider();
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//		stack.
		IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
		
		String energy = String.format("%,d", energyStorage.getEnergyStored());
		String capacity = String.format("%,d", energyStorage.getMaxEnergyStored());
		
		tooltip.add(energy + " / " + capacity + " µI");
		String percent = new DecimalFormat("00.00").format(100 * energyStorage.getEnergyStored() / ((double)energyStorage.getMaxEnergyStored())); 
		
		tooltip.add(percent + " %");
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
		
		return 1 - (double)energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
	}
	
	@Override
	public boolean getShareTag() {
		return true;
	}
	
	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack) {
		IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
		
		NBTTagCompound compound = super.getNBTShareTag(stack);
		
		if (compound == null)
			compound = new NBTTagCompound();
		
		compound.setInteger("aunisEnergy", energyStorage.getEnergyStored());
		
		return compound;
	}
	
	@Override
	public void readNBTShareTag(ItemStack stack, NBTTagCompound nbt) {		
		if (nbt != null && nbt.hasKey("aunisEnergy")) {
			EnergyStorageUncapped energyStorage = (EnergyStorageUncapped) stack.getCapability(CapabilityEnergy.ENERGY, null);
		
			energyStorage.setEnergyStored(nbt.getInteger("aunisEnergy"));
		}
	}
}
