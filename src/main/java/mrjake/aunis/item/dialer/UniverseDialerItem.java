package mrjake.aunis.item.dialer;

import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.item.renderer.UniverseDialerBakedModel;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
import mrjake.aunis.tileentity.TransportRingsTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseBaseTile;
import mrjake.aunis.transportrings.TransportRings;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextComponentTranslation;
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
		compound.setByte("selected", (byte) 0);
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
			
			int reachSquared = AunisConfig.stargateConfig.universeDialerReach * AunisConfig.stargateConfig.universeDialerReach * 2;
			UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
			
			if (mode.linkable) {
				if (compound.hasKey(mode.tagPosName)) {
					BlockPos tilePos = BlockPos.fromLong(compound.getLong(mode.tagPosName));
					
					if (!mode.matcher.apply(world.getBlockState(tilePos)) || tilePos.distanceSq(pos) > reachSquared) {
						compound.removeTag(mode.tagPosName);
					}
				}
				
				else {
					boolean found = false;
					
					for (BlockPos targetPos : BlockPos.getAllInBoxMutable(pos.add(-10, -10, -10), pos.add(10, 10, 10))) {
						if (mode.matcher.apply(world.getBlockState(targetPos))) {
							switch (mode) {
								case MEMORY:
								case NEARBY:
									NBTTagList nearbyList = new NBTTagList();
									int squaredGate = AunisConfig.stargateConfig.universeGateNearbyReach * AunisConfig.stargateConfig.universeGateNearbyReach;
									
									for (Map.Entry<StargateAddress, StargatePos> entry : StargateNetwork.get(world).getMap().get(SymbolTypeEnum.UNIVERSE).entrySet()) {
										StargatePos stargatePos = entry.getValue();
										
										if (stargatePos.dimensionID != world.provider.getDimension())
											continue;
										
										if (stargatePos.gatePos.distanceSq(targetPos) > squaredGate)
											continue;
										
										if (stargatePos.gatePos.equals(targetPos))
											continue;
										
										if (stargatePos.getTileEntity() instanceof StargateOrlinBaseTile)
											continue;
										
										nearbyList.appendTag(entry.getKey().serializeNBT());
									}
									
									compound.setTag(UniverseDialerMode.NEARBY.tagListName, nearbyList);
									compound.setLong(mode.tagPosName, targetPos.toLong());
									found = true;
									break;
									
								case RINGS:
									TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(targetPos);
									NBTTagList ringsList = new NBTTagList();

									for (TransportRings rings : ringsTile.ringsMap.values()) {
										ringsList.appendTag(rings.serializeNBT());
									}
									
									compound.setTag(mode.tagListName, ringsList);
									compound.setLong(mode.tagPosName, targetPos.toLong());
									found = true;
									break;
									
								default:
									break;
							}
						}
						
						if (found)
							break;
					}
				}
			}
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote) {
			NBTTagCompound compound = player.getHeldItem(hand).getTagCompound();
			UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
			int selected = compound.getByte("selected");

			if (mode.linkable && !compound.hasKey(mode.tagPosName))
				return super.onItemRightClick(world, player, hand);
			
			BlockPos linkedPos = BlockPos.fromLong(compound.getLong(mode.tagPosName));
			NBTTagCompound selectedCompound = compound.getTagList(mode.tagListName, NBT.TAG_COMPOUND).getCompoundTagAt(selected);

			switch (mode) {
				case MEMORY:
				case NEARBY:
					StargateUniverseBaseTile gateTile = (StargateUniverseBaseTile) world.getTileEntity(linkedPos);
					
					switch (gateTile.getStargateState()) {
						case IDLE:
							int maxSymbols = SymbolUniverseEnum.getMaxSymbolsDisplay(selectedCompound.getBoolean("hasUpgrade"));
							gateTile.dial(new StargateAddress(selectedCompound), maxSymbols);
							break;
						
						case ENGAGED_INITIATING:
							gateTile.attemptClose();
							break;
						
						case ENGAGED:
							player.sendStatusMessage(new TextComponentTranslation("tile.aunis.dhd_block.incoming_wormhole_warn"), true);
							break;
							
						default:
							player.sendStatusMessage(new TextComponentTranslation("item.aunis.universe_dialer.gate_busy"), true);
							Aunis.info("state: " + gateTile.getStargateState());
							break;
					}
					
				case RINGS:
					TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(linkedPos);
					ringsTile.attemptTransportTo((EntityPlayerMP) player, new TransportRings(selectedCompound).getAddress());
					
				case OC:
					UniverseDialerOCMessage message = new UniverseDialerOCMessage(selectedCompound);					
					Aunis.ocWrapper.sendWirelessPacketPlayer(player, message.address, message.port, message.getData());
					break;
			}
		}
		
		return super.onItemRightClick(world, player, hand);
	}
}
