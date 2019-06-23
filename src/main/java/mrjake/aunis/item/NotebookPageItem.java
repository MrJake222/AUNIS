package mrjake.aunis.item;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.item.renderer.NotebookPageTEISR;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.StargateBaseTile;
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

public class NotebookPageItem extends Item {

	public static final String ITEM_NAME = "notebook_page";

	public NotebookPageItem() {
		setRegistryName(Aunis.ModID + ":" + ITEM_NAME);
		setTranslationKey(Aunis.ModID + "." + ITEM_NAME);
		
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setTileEntityItemStackRenderer(new NotebookPageTEISR());
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	public void registerCustomModel(IRegistry<ModelResourceLocation, IBakedModel> registry) {
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(getRegistryName() + "_filled", "inventory");
		
		IBakedModel defaultModel = registry.getObject(modelResourceLocation);
		NotebookPageBakedModel memberBlockBakedModel = new NotebookPageBakedModel(defaultModel);
		
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
		if (world.getBlockState(pos).getBlock() == AunisBlocks.stargateBaseBlock) {
						
			if (!world.isRemote) {
				StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
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
								
				ItemStack stack = new ItemStack(AunisItems.notebookPageItem, 1, 1);
				stack.setTagCompound(compound);
				
				player.setHeldItem(hand, stack);
			}
			
			return EnumActionResult.SUCCESS;
		}
	
		return EnumActionResult.PASS;
	}
}
