package mrjake.aunis.item.notebook;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.item.renderer.CustomModel;
import mrjake.aunis.item.renderer.CustomModelItemInterface;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NotebookItem extends Item implements CustomModelItemInterface {

	public static final String ITEM_NAME = "notebook";
	
	public NotebookItem() {
		setRegistryName(Aunis.ModID + ":" + ITEM_NAME);
		setUnlocalizedName(Aunis.ModID + "." + ITEM_NAME);
		
		// setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		if (stack.hasTagCompound()) {
//			tooltip.add("Saved gates: " + stack.getTagCompound().getTagList("addressList", NBT.TAG_COMPOUND).tagCount());
			NBTTagCompound compound = stack.getTagCompound();
			NBTTagList list = compound.getTagList("addressList", NBT.TAG_COMPOUND);
			
			for (NBTBase item : list) {
				NBTTagCompound pageTag = (NBTTagCompound) item;
				tooltip.add(TextFormatting.AQUA + PageNotebookItem.getNameFromCompound(pageTag));
			}
		}
	}
	
	private CustomModel customModel;
	
	@Override
	public void setCustomModel(CustomModel customModel) {
		this.customModel = customModel;
	}
	
	public TransformType getLastTransform() {
		return customModel.lastTransform;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public TileEntityItemStackRenderer createTEISR() {
		return new NotebookTEISR();
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (oldStack.getItem() != newStack.getItem())
			return true;
		
		if (!oldStack.hasTagCompound() || !newStack.hasTagCompound())
			return true;
		
		int oldSelected = oldStack.getTagCompound().getInteger("selected");
		int newSelected = newStack.getTagCompound().getInteger("selected");
		
		return oldSelected != newSelected;
	}
	
	
	// ------------------------------------------------------------------------------------------------------------
	// NBT handles
	
	public static NBTTagCompound getSelectedPageFromCompound(NBTTagCompound compound) {
		int selected = compound.getInteger("selected");
		NBTTagList list = compound.getTagList("addressList", NBT.TAG_COMPOUND);
		return list.getCompoundTagAt(selected);
	}
	
	public static void setNameForIndex(NBTTagList list, int index, String name) {
		NBTTagCompound page = list.getCompoundTagAt(index);
		PageNotebookItem.setName(page, name);
	}
}
