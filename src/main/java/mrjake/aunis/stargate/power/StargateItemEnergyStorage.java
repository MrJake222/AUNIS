package mrjake.aunis.stargate.power;

import mrjake.aunis.config.AunisConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;

public final class StargateItemEnergyStorage implements IEnergyStorage {
    private final ItemStack stack;

    public StargateItemEnergyStorage(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyStored = getEnergyStored();
        int energyReceived = Math.min(getMaxEnergyStored() - energyStored, Math.min(AunisConfig.powerConfig.stargateMaxEnergyTransfer, maxReceive));
        if (!simulate)
            setEnergyStored(energyStored + energyReceived);
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    public void setEnergyStored(int energy){
        getOrCreateCompound(stack).setInteger("energy", energy);
    }

    @Override
    public int getEnergyStored() {
        return getOrCreateCompound(stack).getInteger("energy");
    }

    @Override
    public int getMaxEnergyStored() {
        return AunisConfig.powerConfig.stargateEnergyStorage/4;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    private final NBTTagCompound getOrCreateCompound(ItemStack stack) {
        if(!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }
}
