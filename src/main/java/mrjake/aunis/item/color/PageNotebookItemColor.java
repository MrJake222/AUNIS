package mrjake.aunis.item.color;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PageNotebookItemColor implements IItemColor {
		
	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {		
		NBTTagCompound compound = stack.getTagCompound();
		int color = 0xffffff;
		
		if (compound != null && compound.hasKey("color"))
			color = compound.getInteger("color");
		
		return tintIndex == 1 ? color : 0xFFFFFF;
	}

}
