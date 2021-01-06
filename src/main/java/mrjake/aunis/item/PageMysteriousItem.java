package mrjake.aunis.item;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateGeneratorConfig;
import mrjake.aunis.item.notebook.PageNotebookItem;
import mrjake.aunis.worldgen.stargate.GeneratedStargate;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class PageMysteriousItem extends Item {
	public static final String ITEM_NAME = "page_mysterious";

	public PageMysteriousItem() {
		setRegistryName(Aunis.ModID + ":" + ITEM_NAME);
		setUnlocalizedName(Aunis.ModID + "." + ITEM_NAME);
		
		setCreativeTab(Aunis.aunisCreativeTab);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(TextFormatting.ITALIC + Aunis.proxy.localize("item.aunis.mysterious_page.tooltip"));
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("generator", Constants.NBT.TAG_STRING))
			tooltip.add("Generator: " + stack.getTagCompound().getString("generator"));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		
		if (!world.isRemote) {
			ItemStack held = player.getHeldItem(hand);

			String generatorName = null;
			if(held.hasTagCompound() && held.getTagCompound().hasKey("generator", Constants.NBT.TAG_STRING)) {
				generatorName = held.getTagCompound().getString("generator");
			} else {
				generatorName = "overworld";
			}
			GeneratedStargate stargate = StargateGeneratorConfig.findGenerator(generatorName).generateStargate(world.rand, player.getPosition());

			if (stargate != null) {
				NBTTagCompound compound = PageNotebookItem.getCompoundFromAddress(stargate.getAddress(), false, stargate.getWorld().getBiome(stargate.getPos()).getRegistryName().getResourcePath());
				
				ItemStack stack = new ItemStack(AunisItems.PAGE_NOTEBOOK_ITEM, 1, 1);
				stack.setTagCompound(compound);

				held.shrink(1);
				
				if (held.isEmpty())				
					player.setHeldItem(hand, stack);
				
				else {
					player.setHeldItem(hand, held);
					player.addItemStackToInventory(stack);
				}

				if(AunisConfig.mysteriousConfig.pageCooldown > 0)
					player.getCooldownTracker().setCooldown(this, AunisConfig.mysteriousConfig.pageCooldown);
			}
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));	
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for(String generatorName : StargateGeneratorConfig.getMysteriousPageGenerators()) {
				ItemStack stack = new ItemStack(this);

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("generator", generatorName);
				stack.setTagCompound(nbt);

				items.add(stack);
			}
		}
	}
}
