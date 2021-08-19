package mrjake.aunis.stargate.power;

import mrjake.aunis.config.AunisConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;

public final class StargateItemEnergyStorage extends StargateAbstractEnergyStorage {
    private final ItemStack stack;

    public StargateItemEnergyStorage(ItemStack stack) {
        super(AunisConfig.powerConfig.stargateEnergyStorage/4, AunisConfig.powerConfig.stargateMaxEnergyTransfer);
        this.stack = stack;
        this.energy = getEnergyStored();
    }

    @Override
    public int getEnergyStored() {
        return getOrCreateCompound(stack).getInteger("energy");
    }

    @Override
    protected void onEnergyChanged() {
        getOrCreateCompound(stack).setInteger("energy", energy);
    }

    private NBTTagCompound getOrCreateCompound(ItemStack stack) {
        if(!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }
}
