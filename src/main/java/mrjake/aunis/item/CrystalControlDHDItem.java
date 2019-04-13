package mrjake.aunis.item;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.capability.CrystalControlDHDCapabilityProvider;
import mrjake.aunis.capability.EnergyStorageSerializable;
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
		setUnlocalizedName(Aunis.ModID + "." + itemName);
		
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setMaxStackSize(1);
		setNoRepair();
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack itemStack = player.getHeldItem(hand);
		
		if (!world.isRemote) {
			IEnergyStorage energyStorage = itemStack.getCapability(CapabilityEnergy.ENERGY, null);
						
			energyStorage.receiveEnergy(1400000, false);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);

	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {		
		return new CrystalControlDHDCapabilityProvider();
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
		
		String energy = String.format("%,d", energyStorage.getEnergyStored());
		String capacity = String.format("%,d", energyStorage.getMaxEnergyStored());
		
		tooltip.add(energy + " / " + capacity + " µI");
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		EnergyStorageSerializable energyStorage = (EnergyStorageSerializable) stack.getCapability(CapabilityEnergy.ENERGY, null);
		
		return 1 - (double)energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
	}
}
