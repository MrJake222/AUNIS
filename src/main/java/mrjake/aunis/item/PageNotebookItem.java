package mrjake.aunis.item;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class PageNotebookItem extends Item {

	public static final String ITEM_NAME = "page_notebook";

	public PageNotebookItem() {
		setRegistryName(Aunis.ModID + ":" + ITEM_NAME);
		setTranslationKey(Aunis.ModID + "." + ITEM_NAME);
		
		setCreativeTab(Aunis.aunisCreativeTab);
		
		Aunis.proxy.setTileEntityItemStackRenderer(this);
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	public void registerCustomModel(IRegistry<ModelResourceLocation, IBakedModel> registry) {
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(getRegistryName() + "_filled", "inventory");
		
		IBakedModel defaultModel = registry.getObject(modelResourceLocation);
		PageNotebookBakedModel memberBlockBakedModel = new PageNotebookBakedModel(defaultModel);
		
		registry.putObject(modelResourceLocation, memberBlockBakedModel);
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		if (stack.getItemDamage() == 0) {			
			tooltip.add("Empty");
		}
		
		else {			
			NBTTagCompound compound = stack.getTagCompound();
			if (compound != null) {
				long serialized = compound.getLong("address");
				List<Integer> address = EnumSymbol.fromLong(serialized);
								
				for (int id : address) {
					tooltip.add(TextFormatting.ITALIC + "" + TextFormatting.AQUA + EnumSymbol.valueOf(id).name);
				}
				
				if (compound.hasKey("7th")) {
		    		EnumSymbol seventh = EnumSymbol.valueOf(compound.getInteger("7th"));
		    		
					tooltip.add(TextFormatting.ITALIC + "" + TextFormatting.DARK_PURPLE + seventh.name);
				}
			}
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			Block block = world.getBlockState(pos).getBlock();
			
			if (block == AunisBlocks.stargateMilkyWayBaseBlock) {
				StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(pos);
				NBTTagCompound compound = player.getHeldItem(hand).getTagCompound();
				
				if (compound == null)
					compound = new NBTTagCompound();
				else
					compound = compound.copy();
				
				long serialized = EnumSymbol.toLong(gateTile.gateAddress);
				compound.setLong("address", serialized);
				
				if (gateTile.hasUpgrade())
					compound.setInteger("7th", gateTile.gateAddress.get(6).id);
				else
					compound.removeTag("7th");
				
				
				String reg = world.getBiome(pos).getRegistryName().getPath();
				compound.setInteger("color", getColorForBiome(reg));
				
				ItemStack stack = new ItemStack(AunisItems.pageNotebookItem, 1, 1);
				stack.setTagCompound(compound);
				
				ItemStack held = player.getHeldItem(hand);
				held.shrink(1);
				
				if (held.isEmpty())				
					player.setHeldItem(hand, stack);
				
				else {
					player.setHeldItem(hand, held);
					player.addItemStackToInventory(stack);
				}
			}
		}
	
		return EnumActionResult.PASS;
	}
	
	
	/**
	 * Returns color from the Biome
	 * 
	 * @param reg - Registry path of the Biome
	 * @return color
	 */
	public static int getColorForBiome(String reg) {
		int color = 0x303000;
		
		if (reg.contains("ocean") || reg.contains("river")) color = 0x2131A0;
		else if (reg.contains("plains")) color = 0x48703D;
		else if (reg.contains("desert") || reg.contains("beach")) color = 0x9B9C6E;
		else if (reg.contains("extreme_hills")) color = 0x736150;
		else if (reg.contains("forest")) color = 0x507341;
		else if (reg.contains("taiga")) color = 0x7BA9A9;
		else if (reg.contains("swamp")) color = 0x6B7337;
		else if (reg.contains("hell")) color = 0x962A0B;
		else if (reg.contains("sky")) color = 0x67897A;
		else if (reg.contains("ice")) color = 0x69B8C6;
		else if (reg.contains("mushroom")) color = 0x544B4D;
		else if (reg.contains("jungle")) color = 0x104004;
		else if (reg.contains("savanna")) color = 0x66622D;
		else if (reg.contains("mesa")) color = 0x804117;
		
		return color;
	}
}
