package mrjake.aunis.tileentity.stargate;

import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.gui.container.StargateContainerGuiState;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import mrjake.aunis.renderer.stargate.StargateClassicRendererState;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateSoundEventEnum;
import mrjake.aunis.stargate.StargateSoundPositionedEnum;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.DHDTile.DHDUpgradeEnum;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;
import mrjake.aunis.util.FacingToRotation;
import mrjake.aunis.util.ItemHandlerHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * This class wraps common behavior for the fully-functional Stargates i.e.
 * all of them (right now) except Orlin's.
 * 
 * @author MrJake222
 *
 */
public abstract class StargateClassicBaseTile extends StargateAbstractBaseTile {
	
	// ------------------------------------------------------------------------
	// Stargate state
		
	@Override
	protected void disconnectGate() {
		super.disconnectGate();
		
		updateChevronLight();
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
	}
	
	@Override
	protected void failGate() {
		super.failGate();
		
		updateChevronLight();
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
	}
	
	@Override
	public void onBlockBroken() {
		super.onBlockBroken();
		
		playPositionedSound(StargateSoundPositionedEnum.GATE_RING_ROLL, false);
		ItemHandlerHelper.dropInventoryItems(world, pos, itemStackHandler);
	}
	
	
	// ------------------------------------------------------------------------
	// Stargate Network
	// TODO canAddSymbol
	
	@Override
	protected void addSymbolToAddress(EnumSymbol symbol) {
		super.addSymbolToAddress(symbol);
		
		updateChevronLight();
	}
	
	@Override
	public void incomingWormhole(List<EnumSymbol> incomingAddress, int dialedAddressSize) {
		super.incomingWormhole(incomingAddress, dialedAddressSize);
		
		playSoundEvent(StargateSoundEventEnum.INCOMING, 0.5f);
		sendRenderingUpdate(EnumGateAction.LIGHT_UP_CHEVRONS, dialedAddressSize, true);
	}
	
	@Override
	public void dialingFailed() {
		super.dialingFailed();

		playSoundEvent(StargateSoundEventEnum.DIAL_FAILED, 0.3f);
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, 53));
	}
	
	
	// ------------------------------------------------------------------------
	// NBT
			
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("itemHandler", itemStackHandler.serializeNBT());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		itemStackHandler.deserializeNBT(compound.getCompoundTag("itemHandler"));
		
		if (compound.getBoolean("hasUpgrade")) {
			itemStackHandler.setStackInSlot(0, new ItemStack(AunisItems.crystalGlyphStargate));
		}
		
		super.readFromNBT(compound);
	}
	
	
	// ------------------------------------------------------------------------
	// Rendering
	
	protected void updateChevronLight() {
		for (int i=0; i<9; i++) {
			BlockPos chevPos = getMergeHelper().getChevronBlocks().get(i);
			
			if (getMergeHelper().matchBase(world.getBlockState(chevPos))) {
				// TODO Change MemberTiles...
				StargateMilkyWayMemberTile memberTile = (StargateMilkyWayMemberTile) world.getTileEntity(chevPos.rotate(FacingToRotation.get(facing)).add(pos));
				
				memberTile.setLitUp(dialedAddress.size() > i);
			}
		}
	}
	
	@Override
	protected StargateAbstractRendererState getRendererStateServer() {
		return StargateClassicRendererState.builder()
				.setActiveChevrons(dialedAddress.size())
				.setFinalActive(isFinalActive)
				.setStargateState(stargateState).build();
	}
	
	@Override
	protected StargateAbstractRendererState createRendererStateClient() {
		return new StargateClassicRendererState();
	}
	
	@Override
	public StargateClassicRendererState getRendererStateClient() {
		return (StargateClassicRendererState) super.getRendererStateClient();
	}
	
	
	// -----------------------------------------------------------------
	// States
	
	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case GUI_STATE:
				return new StargateContainerGuiState();
				
			default:
				return super.createState(stateType);
		}
	}
	
	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {		
			case RENDERER_UPDATE:
				StargateRendererActionState gateActionState = (StargateRendererActionState) state;
				
				switch (gateActionState.action) {
					case CHEVRON_ACTIVATE:
						if (gateActionState.modifyFinal)
							getRendererStateClient().chevronTextureList.activateFinalChevron(world.getTotalWorldTime());
						else
							getRendererStateClient().chevronTextureList.activateNextChevron(world.getTotalWorldTime());
						
						break;
					
					case CLEAR_CHEVRONS:
						getRendererStateClient().chevronTextureList.clearChevrons(world.getTotalWorldTime());
						break;
						
					case LIGHT_UP_CHEVRONS:
						getRendererStateClient().chevronTextureList.lightUpChevrons(world.getTotalWorldTime(), gateActionState.chevronCount);
						break;
						
					case CHEVRON_ACTIVATE_BOTH:
						getRendererStateClient().chevronTextureList.activateNextChevron(world.getTotalWorldTime());
						getRendererStateClient().chevronTextureList.activateFinalChevron(world.getTotalWorldTime());
						break;
						
					case CHEVRON_DIM:
						getRendererStateClient().chevronTextureList.deactivateFinalChevron(world.getTotalWorldTime());
						break;
						
					default:
						super.setState(stateType, gateActionState);
						break;
				}
				
				break;
		
			case GUI_STATE:
				StargateContainerGuiState guiState = (StargateContainerGuiState) state;
				gateAddress = guiState.getGateAddress();
				Aunis.info(this + ": Setting address to " + gateAddress);
				
				break;
				
			default:
				super.setState(stateType, state);
		}
	}

	
	// -----------------------------------------------------------------
	// Scheduled tasks
	
	@Override
	public void executeTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		switch (scheduledTask) {
			case STARGATE_ACTIVATE_CHEVRON:
				stargateState = EnumStargateState.IDLE;
				markDirty();
				
				playSoundEvent(StargateSoundEventEnum.CHEVRON_OPEN, 1.0f);
				sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE, -1, customData.getBoolean("final"));
//				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, new StargateRendererActionState(EnumGateAction.CHEVRON_ACTIVATE, -1, customData.getBoolean("final"))), targetPoint);
				break;
				
			case STARGATE_SPIN_FINISHED:
				playPositionedSound(StargateSoundPositionedEnum.GATE_RING_ROLL, false);
				playSoundEvent(StargateSoundEventEnum.CHEVRON_SHUT, 1.0f);
				
				markDirty();
				break;
				
			default:
				super.executeTask(scheduledTask, customData);
		}
	}
	
	
	// -----------------------------------------------------------------------------
	// Item handler
	
	private ItemStackHandler itemStackHandler = new ItemStackHandler(10) {
		
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			Item item = stack.getItem();
			
			switch (slot) {
				case 0:				
				case 1:
				case 2:
				case 3:
					return StargateUpgradeEnum.contains(item);
					
				case 4:
				case 5:
				case 6:
					return true;
					
				case 7:
				case 8:
				case 9:
					return item == AunisItems.pageNotebookItem;
					
				default:
					return true;
			}
		}
		
		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return 1;
		}
		
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			
			markDirty();
		}
	};
	
	public static enum StargateUpgradeEnum implements EnumKeyInterface<Item> {
		MILKYWAY_GLYPHS(AunisItems.crystalGlyphMilkyWay),
		PEGASUS_GLYPHS(AunisItems.crystalGlyphPegasus),
		UNIVERSE_GLYPHS(AunisItems.crystalGlyphUniverse),
		CHEVRON_UPGRADE(AunisItems.crystalGlyphStargate);
		
		public Item item;

		private StargateUpgradeEnum(Item item) {
			this.item = item;
		}
		
		@Override
		public Item getKey() {
			return item;
		}
		
		private static EnumKeyMap<Item, StargateUpgradeEnum> idMap = new EnumKeyMap<Item, StargateClassicBaseTile.StargateUpgradeEnum>(values());
		
		public static StargateUpgradeEnum valueOf(Item item) {
			return idMap.valueOf(item);
		}
		
		public static boolean contains(Item item) {
			return idMap.contains(item);
		}
	}
	
	public boolean hasUpgradeInstalled(StargateUpgradeEnum upgrade) {
		for (int slot=0; slot<4; slot++) {
			if (itemStackHandler.getStackInSlot(slot).getItem() == upgrade.item)
				return true;
		}
		
		return false;
	}
	
	
	// -----------------------------------------------------------------------------
	// Capabilities
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
		
		return super.getCapability(capability, facing);	
	}
}
