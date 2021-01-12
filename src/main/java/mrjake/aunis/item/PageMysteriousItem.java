package mrjake.aunis.item;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateGeneratorConfig;
import mrjake.aunis.item.notebook.PageNotebookItem;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.worldgen.stargate.GeneratedStargate;
import mrjake.aunis.worldgen.stargate.JsonStargateGenerator;
import mrjake.aunis.worldgen.stargate.StargateGenerationException;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
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
			final ItemStack held = player.getHeldItem(hand);
			if(held.hasTagCompound() && held.getTagCompound().hasKey("generator", Constants.NBT.TAG_STRING)) {
				String generatorName = held.getTagCompound().getString("generator");

				StargateAddress address = null;
				Biome biome = null;

				try {
					if(generatorName.equals("orlin")) {
						StargateNetwork network = StargateNetwork.get(world);

						if (!network.hasNetherGate()) {
							network.setNetherGate(StargateGeneratorConfig.findGenerator("orlin").generateStargate(world.rand, player.getPosition()).getAddress());
						}

						address = network.getNetherGate();
						StargatePos stargatePos = network.getStargate(address);
						biome = stargatePos.getWorld().getBiome(stargatePos.gatePos);
					} else {
						JsonStargateGenerator generator = StargateGeneratorConfig.findGenerator(generatorName);
						if(generator == null)
							throw new StargateGenerationException("Stargate generator `%s` not found!", generatorName);

						GeneratedStargate stargate = generator.generateStargate(world.rand, player.getPosition());
						if(stargate != null) {
							address = stargate.getAddress();
							biome = stargate.getWorld().getBiome(stargate.getPos());
						}
					}

					if (address != null && biome != null) {
						NBTTagCompound compound = PageNotebookItem.getCompoundFromAddress(address, false, biome.getRegistryName().getResourcePath());

						ItemStack stack = new ItemStack(AunisItems.PAGE_NOTEBOOK_ITEM, 1, 1);
						stack.setTagCompound(compound);

						if(!player.capabilities.isCreativeMode)
							held.shrink(1);

						if (held.isEmpty())
							player.setHeldItem(hand, stack);

						else {
							player.addItemStackToInventory(stack);
						}
					} else {
						throw new StargateGenerationException("%s after stargate generation. Generator: %s", address == null ? "Address is null" : "Biome is null", generatorName);
					}
				} catch (StargateGenerationException e) {
					ITextComponent msg = new TextComponentTranslation("item.aunis.mysterious_page.error");
					msg.getStyle().setColor(TextFormatting.DARK_RED);
					player.sendStatusMessage(msg, true);
					Aunis.logger.error("Error in mysterious page stargate generator", e);
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
