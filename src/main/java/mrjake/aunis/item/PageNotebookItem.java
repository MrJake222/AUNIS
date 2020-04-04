package mrjake.aunis.item;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.item.renderer.PageNotebookBakedModel;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
			tooltip.add(Aunis.proxy.localize("item.aunis.page_mysterious.empty"));
		}
		
		else {			
			if (stack.hasTagCompound()) {
				NBTTagCompound compound = stack.getTagCompound();
				
				SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(compound.getInteger("symbolType"));
				StargateAddress stargateAddress = new StargateAddress(compound.getCompoundTag("address"));
				int maxSymbols = symbolType.getMaxSymbolsDisplay(compound.getBoolean("hasUpgrade"));
								
				for (int i=0; i<maxSymbols; i++) {
					tooltip.add(TextFormatting.ITALIC + "" + (i > 5 ? TextFormatting.DARK_PURPLE : TextFormatting.AQUA) + stargateAddress.get(i).localize());
				}
			}
		}
	}
	
	/**
	 * Returns color from the Biome
	 * 
	 * @param registryPath - Registry path of the Biome
	 * @return color
	 */
	public static int getColorForBiome(String registryPath) {
		int color = 0x303000;
		
		if (registryPath.contains("ocean") || registryPath.contains("river")) color = 0x2131A0;
		else if (registryPath.contains("plains")) color = 0x48703D;
		else if (registryPath.contains("desert") || registryPath.contains("beach")) color = 0x9B9C6E;
		else if (registryPath.contains("extreme_hills")) color = 0x736150;
		else if (registryPath.contains("forest")) color = 0x507341;
		else if (registryPath.contains("taiga")) color = 0x7BA9A9;
		else if (registryPath.contains("swamp")) color = 0x6B7337;
		else if (registryPath.contains("hell")) color = 0x962A0B;
		else if (registryPath.contains("sky")) color = 0x67897A;
		else if (registryPath.contains("ice")) color = 0x69B8C6;
		else if (registryPath.contains("mushroom")) color = 0x544B4D;
		else if (registryPath.contains("jungle")) color = 0x104004;
		else if (registryPath.contains("savanna")) color = 0x66622D;
		else if (registryPath.contains("mesa")) color = 0x804117;
		
		return color;
	}
	
	public static String getRegistryPathFromWorld(World world, BlockPos pos) {
		return world.getBiome(pos).getRegistryName().getPath();
	}
	
	public static NBTTagCompound getCompoundFromAddress(StargateAddress address, boolean hasUpgrade, String registryPath) {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("symbolType", address.getSymbolType().id);
		compound.setTag("address", address.serializeNBT());
		compound.setBoolean("hasUpgrade", hasUpgrade);
		compound.setInteger("color", PageNotebookItem.getColorForBiome(registryPath));
		
		return compound;
	}
}
