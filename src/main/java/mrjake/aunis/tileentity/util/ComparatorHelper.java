package mrjake.aunis.tileentity.util;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ComparatorHelper {
	
	public static int getComparatorLevel(IItemHandler itemHandler, int startingIndex) {
		return calcRedstoneFromInventory(itemHandler, startingIndex);
	}
	
	public static int getComparatorLevel(FluidTank fluidTank) {
		if (fluidTank.getFluidAmount() == 0)
			return 0;
		
		float percent = fluidTank.getFluidAmount() / (float)fluidTank.getCapacity();
		return Math.round(1 + (percent * 14));
	}
	
	public static int getComparatorLevel(IEnergyStorage energyStorage) {
		if (energyStorage.getEnergyStored() == 0)
			return 0;
		
		float percent = energyStorage.getEnergyStored() / (float)energyStorage.getMaxEnergyStored();
		return Math.round(1 + (percent * 14));
	}
	
	/**
	 * Copied from {@link ItemHandlerHelper}. Added starting index
     * This method uses the standard vanilla algorithm to calculate a comparator output for how "full" the inventory is.
     * This method is an adaptation of Container#calcRedstoneFromInventory(IInventory).
     * @param inv The inventory handler to test.
     * @return A redstone value in the range [0,15] representing how "full" this inventory is.
     */
    public static int calcRedstoneFromInventory(@Nullable IItemHandler inv, int startingIndex)
    {
        if (inv == null)
        {
            return 0;
        }
        else
        {
            int itemsFound = 0;
            float proportion = 0.0F;

            for (int j = startingIndex; j < inv.getSlots(); ++j)
            {
                ItemStack itemstack = inv.getStackInSlot(j);

                if (!itemstack.isEmpty())
                {
                    proportion += (float)itemstack.getCount() / (float)Math.min(inv.getSlotLimit(j), itemstack.getMaxStackSize());
                    ++itemsFound;
                }
            }

            proportion = proportion / (float)inv.getSlots();
            return MathHelper.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
        }
    }
}
