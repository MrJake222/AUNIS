package mrjake.aunis.util;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

public class NBTHelper {
	public static NBTTagList serializeFluidStackList(List<FluidStack> list) {
		NBTTagList nbt = new NBTTagList();
		
		for (FluidStack stack : list) {
			NBTTagCompound compound = new NBTTagCompound();
			stack.writeToNBT(compound);
			nbt.appendTag(compound);
		}
		
		return nbt;
	}
	
	public static void deserializeFluidStackList(NBTTagList nbt, List<FluidStack> list) {
		
		for (NBTBase base : nbt) {
			FluidStack stack = FluidStack.loadFluidStackFromNBT((NBTTagCompound) base);
			list.add(stack);
		}
	}
	
	
	public static NBTTagList serializeItemStackList(List<ItemStack> list) {
		NBTTagList nbt = new NBTTagList();
		
		for (ItemStack stack : list) {
			NBTTagCompound compound = new NBTTagCompound();
			stack.writeToNBT(compound);
			nbt.appendTag(compound);
		}
		
		return nbt;
	}
	
	public static void deserializeItemStackList(NBTTagList nbt, List<ItemStack> list) {
		
		for (NBTBase base : nbt) {
			ItemStack stack = new ItemStack((NBTTagCompound) base);
			list.add(stack);
		}
	}
}
