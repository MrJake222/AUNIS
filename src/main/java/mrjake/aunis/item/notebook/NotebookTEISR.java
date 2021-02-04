package mrjake.aunis.item.notebook;

import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.renderer.AunisFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class NotebookTEISR extends TileEntityItemStackRenderer {

	@Override
	public void renderByItem(ItemStack itemStack) {
		if (itemStack.hasTagCompound()) {
			NBTTagCompound compound = itemStack.getTagCompound();
			NBTTagCompound pageTag = NotebookItem.getSelectedPageFromCompound(compound);
			PageRenderer.renderByCompound(AunisItems.NOTEBOOK_ITEM.getLastTransform(), pageTag);
			
			int selected = compound.getInteger("selected");
			NBTTagList list = compound.getTagList("addressList", NBT.TAG_COMPOUND);
			
			boolean first = (selected == 0);
			boolean last = (selected == list.tagCount()-1);
			
			String namePrev = null, nameNext = null;
			
			if (!first)
				namePrev = "< " + PageNotebookItem.getNameFromCompound(list.getCompoundTagAt(selected-1));
			
			if (!last)
				nameNext = PageNotebookItem.getNameFromCompound(list.getCompoundTagAt(selected+1)) + " >";
			
			
			final float scale = 0.0031f;
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.55f-AunisConfig.avConfig.pageNarrowing, 0.295f, 0.011f);
			GlStateManager.rotate(180, 0, 0, 1);
			GlStateManager.scale(scale, scale, scale);
			
			if (namePrev != null)
				AunisFontRenderer.getFontRenderer().drawString(namePrev, 0, 0, 0x383228, false);
			
			if (nameNext != null) {
				final float width = Minecraft.getMinecraft().fontRenderer.getStringWidth(nameNext) * scale;
				
				GlStateManager.translate((-0.6335f + width)/scale, 0, 0);
				AunisFontRenderer.getFontRenderer().drawString(nameNext, 0, 0, 0x383228, false);
			}
			
			GlStateManager.popMatrix();
		}
	}
	
}
