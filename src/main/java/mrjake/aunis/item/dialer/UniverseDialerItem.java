package mrjake.aunis.item.dialer;

import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.capability.endpoint.ItemEndpointCapability;
import mrjake.aunis.capability.endpoint.ItemEndpointInterface;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.item.renderer.CustomModel;
import mrjake.aunis.item.renderer.CustomModelItemInterface;
import mrjake.aunis.stargate.StargateClosedReasonEnum;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
import mrjake.aunis.tileentity.TransportRingsTile;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseBaseTile;
import mrjake.aunis.transportrings.TransportRings;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UniverseDialerItem extends Item implements CustomModelItemInterface {

	public static final String ITEM_NAME = "universe_dialer";

	public UniverseDialerItem() {
		setRegistryName(new ResourceLocation(Aunis.ModID, ITEM_NAME));
		setUnlocalizedName(Aunis.ModID + "." + ITEM_NAME);
		
		setCreativeTab(Aunis.aunisCreativeTab);
		// setMaxStackSize(1);
	}

	// TODO replace with capabilities. If item will have NBT like "display:Name" it will not init custom NBT! -- slava110
	// MrJake: Capabilities are meh in 1.12. Hope they've fixed them in 1.16.
	private static void checkNBT(ItemStack stack) {
		if(!stack.hasTagCompound()) {
			initNBT(stack);
		}
	}
	
	private static void initNBT(ItemStack stack) {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("mode", UniverseDialerMode.NEARBY.id);
		compound.setByte("selected", (byte) 0);
		compound.setTag("saved", new NBTTagList());
		
		stack.setTagCompound(compound);
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			ItemStack stack = new ItemStack(this);
			initNBT(stack);
			items.add(stack);
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
		return new UniverseDialerTEISR();
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new UniverseDialerCapabilityProvider();
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem();
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			NBTTagList list = stack.getTagCompound().getTagList("saved", NBT.TAG_COMPOUND);
			tooltip.add(TextFormatting.GRAY + Aunis.proxy.localize("item.aunis.universe_dialer.saved_gates", list.tagCount()));

			for (int i=0; i<list.tagCount(); i++) {
				NBTTagCompound compound = list.getCompoundTagAt(i);
				
				if (compound.hasKey("name")) {
					tooltip.add(TextFormatting.AQUA + compound.getString("name"));
				}
			}
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (!world.isRemote) {
			checkNBT(stack);
			NBTTagCompound compound = stack.getTagCompound();
			
			if (world.getTotalWorldTime() % 20 == 0 && isSelected) {
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
										StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) world.getTileEntity(targetPos);
										
										if (!gateTile.isMerged())
											continue;
										
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
											
											StargateAbstractBaseTile targetGateTile = stargatePos.getTileEntity();
											
											if (!(targetGateTile instanceof StargateClassicBaseTile))
												continue;
											
											if (!targetGateTile.isMerged())
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
		
			// Server side
			ItemEndpointInterface endpointStack = stack.getCapability(ItemEndpointCapability.ENDPOINT_CAPABILITY, null);
			endpointStack.checkAndUpdateEndpoint(world.getTotalWorldTime());
		}
	}
	
	@Override
	public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {		
		stack.getCapability(ItemEndpointCapability.ENDPOINT_CAPABILITY, null).removeEndpoint();
		
		return super.onDroppedByPlayer(stack, player);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote) {
			checkNBT(player.getHeldItem(hand));
			NBTTagCompound compound = player.getHeldItem(hand).getTagCompound();
			UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
			int selected = compound.getByte("selected");

			if (mode.linkable && !compound.hasKey(mode.tagPosName))
				return super.onItemRightClick(world, player, hand);
						
			BlockPos linkedPos = BlockPos.fromLong(compound.getLong(mode.tagPosName));
			NBTTagList tagList = compound.getTagList(mode.tagListName, NBT.TAG_COMPOUND);
			
			if (selected >= tagList.tagCount())
				return super.onItemRightClick(world, player, hand);
				
			NBTTagCompound selectedCompound = tagList.getCompoundTagAt(selected);

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
							gateTile.attemptClose(StargateClosedReasonEnum.REQUESTED);
							break;
						
						case ENGAGED:
							player.sendStatusMessage(new TextComponentTranslation("tile.aunis.dhd_block.incoming_wormhole_warn"), true);
							break;
							
						default:
							player.sendStatusMessage(new TextComponentTranslation("item.aunis.universe_dialer.gate_busy"), true);
							break;
					}
					
					break;
					
				case RINGS:
					TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(linkedPos);
					ringsTile.attemptTransportTo(new TransportRings(selectedCompound).getAddress(), 0).sendMessageIfFailed(player);
						
					break;
					
				case OC:
					UniverseDialerOCMessage message = new UniverseDialerOCMessage(selectedCompound);					
					Aunis.logger.debug("Sending OC message: " + message.toString());
					Aunis.ocWrapper.sendWirelessPacketPlayer(player, player.getHeldItem(hand), message.address, message.port, message.getData());
					break;
			}
		}
		
		return super.onItemRightClick(world, player, hand);
	}
	
	// ------------------------------------------------------------------------------------------------------------
	// NBT handles
	
	public static void setMemoryNameForIndex(NBTTagList list, int index, String name) {
		list.getCompoundTagAt(index).setString("name", name);
	}
	
	public static void changeOCMessageAtIndex(NBTTagList list, int index, ChangeMessage changeMessage) {
		UniverseDialerOCMessage message = new UniverseDialerOCMessage(list.getCompoundTagAt(index));
		changeMessage.change(message);
		list.set(index, message.serializeNBT());
	}
	
	public static interface ChangeMessage {
		public void change(UniverseDialerOCMessage message);
	}
}
