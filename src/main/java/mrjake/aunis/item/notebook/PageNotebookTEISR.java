package mrjake.aunis.item.notebook;

import mrjake.aunis.item.AunisItems;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

public class PageNotebookTEISR extends TileEntityItemStackRenderer {
	
	@Override
	public void renderByItem(ItemStack itemStack) {
		if (itemStack.hasTagCompound()) {
			PageRenderer.renderByCompound(AunisItems.PAGE_NOTEBOOK_ITEM.getLastTransform(), itemStack.getTagCompound());
		}
	}
}
