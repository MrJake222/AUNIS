package mrjake.aunis.item.dialer;

import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.item.renderer.UniverseDialerBakedModel;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class UniverseDialerItem extends Item {

	public static final String ITEM_NAME = "universe_dialer";

	public UniverseDialerItem() {
		setRegistryName(new ResourceLocation(Aunis.ModID, ITEM_NAME));
		setTranslationKey(Aunis.ModID + "." + ITEM_NAME);
		
		setCreativeTab(Aunis.aunisCreativeTab);
		Aunis.proxy.setTileEntityItemStackRenderer(this);
	}
	
	private static NBTTagCompound initNbt() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("mode", UniverseDialerMode.NEARBY.id);
		compound.setByte("addressSelected", (byte) 0);
		compound.setTag("saved", new NBTTagList());
		
		return compound;
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			ItemStack stack = new ItemStack(this);
			stack.setTagCompound(initNbt());
			items.add(stack);
		}
	}
	
	public void registerCustomModel(IRegistry<ModelResourceLocation, IBakedModel> registry) {
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(getRegistryName(), "inventory");
		
		IBakedModel defaultModel = registry.getObject(modelResourceLocation);
		UniverseDialerBakedModel bakedModel = new UniverseDialerBakedModel(defaultModel);
		
		registry.putObject(modelResourceLocation, bakedModel);
	}
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) {
		stack.setTagCompound(initNbt());
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem();
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add("Saved gates: " + stack.getTagCompound().getTagList("saved", NBT.TAG_COMPOUND).tagCount());
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (world.getTotalWorldTime() % 20 == 0 && isSelected && !world.isRemote && stack.hasTagCompound()) {
			NBTTagCompound compound = stack.getTagCompound();
			BlockPos pos = entity.getPosition();
			
			if (compound.hasKey("linkedGate")) {
				int squared = AunisConfig.stargateConfig.universeDialerReach * AunisConfig.stargateConfig.universeDialerReach * 2;
				
				if (BlockPos.fromLong(compound.getLong("linkedGate")).distanceSq(pos) > squared) {
//					compound.setByte("mode", UniverseDialerMode.MEMORY.id);
					compound.removeTag("linkedGate");
					compound.removeTag("nearby");
				}
			}
			
			else {				
				for (BlockPos gatePos : BlockPos.getAllInBoxMutable(pos.add(-10, -10, -10), pos.add(10, 10, 10))) {
					if (world.getBlockState(gatePos).getBlock() == AunisBlocks.STARGATE_UNIVERSE_BASE_BLOCK) {
						gatePos = gatePos.toImmutable();
						
						NBTTagList nearbyList = new NBTTagList();
						int squared = AunisConfig.stargateConfig.universeGateNearbyReach * AunisConfig.stargateConfig.universeGateNearbyReach;
						
						for (Map.Entry<StargateAddress, StargatePos> entry : StargateNetwork.get(world).getMap().get(SymbolTypeEnum.UNIVERSE).entrySet()) {
							StargatePos stargatePos = entry.getValue();
							
							if (stargatePos.dimensionID != world.provider.getDimension())
								continue;
							
							if (stargatePos.gatePos.distanceSq(gatePos) > squared)
								continue;
							
							if (stargatePos.gatePos.equals(gatePos))
								continue;
							
							nearbyList.appendTag(entry.getKey().serializeNBT());
						}
						
						compound.setTag("nearby", nearbyList);
						compound.setLong("linkedGate", gatePos.toLong());
						
						break;
					}
				}
			}
		}
	}
}
