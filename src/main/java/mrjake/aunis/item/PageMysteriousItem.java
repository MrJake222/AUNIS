package mrjake.aunis.item;

import mrjake.aunis.Aunis;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.item.notebook.PageNotebookItem;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.worldgen.AbstractStargateGenerator.GeneratedStargate;
import mrjake.aunis.worldgen.OverworldStargateGenerator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

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
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		
		if (!world.isRemote) {
			GeneratedStargate<StargateMilkyWayBaseTile> stargate = OverworldStargateGenerator.INSTANCE.generateStargate((WorldServer) world, null);
			
			if (stargate != null) {
				NBTTagCompound compound = PageNotebookItem.getCompoundFromAddress(stargate.getAddress(SymbolTypeEnum.MILKYWAY), false, stargate.getWorld().getBiome(stargate.getPos()).getRegistryName().getResourcePath());
				
				ItemStack stack = new ItemStack(AunisItems.PAGE_NOTEBOOK_ITEM, 1, 1);
				stack.setTagCompound(compound);
				
				ItemStack held = player.getHeldItem(hand);
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
}
