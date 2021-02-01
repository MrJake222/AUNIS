package mrjake.aunis.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Modified version of {@link ItemStackHandler}.
 * Respects resizing of the item handlers.
 */
public class AunisItemStackHandler extends ItemStackHandler {
	
	public AunisItemStackHandler(int size) {
		super(size);
	}

	@Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");

            if (slot >= 0 && slot < stacks.size())
            {
                stacks.set(slot, new ItemStack(itemTags));
            }
        }
        onLoad();
    }
}
