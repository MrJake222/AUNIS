package mrjake.aunis.item.color;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public class CrystalControlDHDItemColor implements IItemColor {
	
	private final int min = 32;
	
	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		EnergyStorage energyStorage = (EnergyStorage) stack.getCapability(CapabilityEnergy.ENERGY, null);
		
		float fraction = (float)(energyStorage.getEnergyStored()) / energyStorage.getMaxEnergyStored();		
		int color = (int) (fraction * (255 - min)) + min;
		
		return color << 16 | color << 8 | color;
	}

}
