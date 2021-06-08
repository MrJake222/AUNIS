package mrjake.aunis.tileentity.util;

import java.util.Iterator;
import java.util.stream.IntStream;

import mrjake.aunis.util.EnumKeyInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Simple interface to allow upgrades insert into TE. `tryInsertStack` should be triggered in block class on interact
 */
public interface IUpgradable {

    // Already TileEntity method, we're just calling it from the interface
    public <T> T getCapability(Capability<T> capability, EnumFacing facing);

    public default IItemHandler getItemHandler() {
        return getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    public default boolean hasUpgrade(EnumKeyInterface<Item> upgrade) {
        return hasUpgrade(upgrade.getKey());
    }

    public default boolean hasUpgrade(Item item) {
        final IItemHandler itemHandler = getItemHandler();
        final Iterator<Integer> iter = getUpgradeSlotsIterator();

        while (iter.hasNext()) {
            int slot = iter.next();
            if(itemHandler.getStackInSlot(slot).getItem() == item) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get upgrade slot iterator. Used in interface. You can use `IntStream.range(min, max).iterator()`
     * @return upgrade slot iterator
     */
    public default Iterator<Integer> getUpgradeSlotsIterator(){
        return IntStream.range(0, getItemHandler().getSlots()).iterator();
    }

    /**
     * Try insert upgrade item into TE
     * @param player player who inserted upgrade
     * @param hand used hand
     * @return true if inserted successfully, false if not
     */
    public default boolean tryInsertUpgrade(EntityPlayer player, EnumHand hand){
        ItemStack stack = player.getHeldItem(hand);
        if(stack.isEmpty())
            return false;

        IItemHandler itemHandler = getItemHandler();

        Iterator<Integer> iter = getUpgradeSlotsIterator();
        while (iter.hasNext()) {
            int slot = iter.next();
            if(itemHandler.getStackInSlot(slot).isEmpty() && itemHandler.isItemValid(slot, stack)) {
                // Maybe should not take item in creative mode
                player.setHeldItem(hand, itemHandler.insertItem(slot, stack, false));
                return true;
            }
        }
        return false;
    }
}
