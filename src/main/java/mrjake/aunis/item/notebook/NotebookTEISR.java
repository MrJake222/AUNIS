package mrjake.aunis.item.notebook;

import mrjake.aunis.item.AunisItems;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NotebookTEISR extends TileEntityItemStackRenderer {

	@Override
	public void renderByItem(ItemStack itemStack) {
		if (itemStack.hasTagCompound()) {
			NBTTagCompound compound = itemStack.getTagCompound();
			NBTTagCompound pageTag = NotebookItem.getSelectedPageFromCompound(compound);
			
			PageRenderer.renderByCompound(AunisItems.NOTEBOOK_ITEM.getLastTransform(), pageTag);
		}
	}
	
}
