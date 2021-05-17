package mrjake.aunis.tileentity;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.gui.container.DHDContainerGuiUpdate;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.renderer.DHDRendererState;
import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.stargate.network.StargateAddressDynamic;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.state.DHDActivateButtonState;
import mrjake.aunis.state.StargateBiomeOverrideState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.util.IUpgradable;
import mrjake.aunis.tileentity.util.ReactorStateEnum;
import mrjake.aunis.util.AunisItemStackHandler;
import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.ILinkable;
import mrjake.aunis.util.ItemMetaPair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DHDTile extends TileEntity implements ILinkable, IUpgradable, StateProviderInterface, ITickable {
	
	// ---------------------------------------------------------------------------------------------------
	// Gate linking
	
	private BlockPos linkedGate = null;

	private int linkId = -1;
	
	@Override
	public void rotate(Rotation rotation) {
		IBlockState state = world.getBlockState(pos);
		
		int rotationOrig = state.getValue(AunisProps.ROTATION_HORIZONTAL);
		world.setBlockState(pos, state.withProperty(AunisProps.ROTATION_HORIZONTAL, rotation.rotate(rotationOrig, 16)));
	}
	
	public void setLinkedGate(BlockPos gate, int linkId) {		
		this.linkedGate = gate;
		this.linkId = linkId;
		
		markDirty();
	}
	
	public boolean isLinked() {
		return this.linkedGate != null;
	}
	
	public StargateAbstractBaseTile getLinkedGate(IBlockAccess world) {
		if (linkedGate == null)
			return null;
		
		return (StargateAbstractBaseTile) world.getTileEntity(linkedGate);
	}
	
	@Override
	public boolean canLinkTo() {
		return !isLinked();
	}

	@Override
	public int getLinkId() {
		return linkId;
	}

	public void updateLinkStatus(World world, BlockPos pos) {
		BlockPos closestGate = LinkingHelper.findClosestUnlinked(world, pos, LinkingHelper.getDhdRange(), AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK, this.getLinkId());
		int linkId = -1;

		if (closestGate != null) {
			linkId = LinkingHelper.getLinkId();
			StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(closestGate);
			gateTile.setLinkedDHD(pos, linkId);
		}

		setLinkedGate(closestGate, linkId);
	}
	
	// ---------------------------------------------------------------------------------------------------
	// Renderer state
	
	private DHDRendererState rendererStateClient;
	
	public DHDRendererState getRendererStateClient() {
		return rendererStateClient;
	}
	
	// ---------------------------------------------------------------------------------------------------
	// Loading and ticking
	
	private BlockPos lastPos = BlockPos.ORIGIN;

	private TargetPoint targetPoint;
	private ReactorStateEnum reactorState = ReactorStateEnum.STANDBY;
	
	public ReactorStateEnum getReactorState() {
		return reactorState;
	}
	
	@Override
	public void onLoad() {
		if (!world.isRemote) {
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
			hadControlCrystal = hasControlCrystal();
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.RENDERER_STATE));
		}
	}
	
	@Override
	public void update() {
		if (!world.isRemote) {
			if (!lastPos.equals(pos)) {
				lastPos = pos;
				this.updateLinkStatus(world, pos);
			}

			// Has crystal
			if (hasControlCrystal()) {
				if (isLinked()) {
					StargateAbstractBaseTile gateTile = getLinkedGate(world);
					if (gateTile == null) {
						setLinkedGate(null, -1);
						Aunis.logger.error("Gate didn't unlink properly, forcing...");
						return;
					}
					
					IEnergyStorage energyStorage = (IEnergyStorage) gateTile.getCapability(CapabilityEnergy.ENERGY, null);
					
					int amount = 1 * AunisConfig.dhdConfig.powerGenerationMultiplier;
					
					if (reactorState != ReactorStateEnum.STANDBY) {
						FluidStack simulatedDrain = fluidHandler.drainInternal(amount, false);
						
						if (simulatedDrain != null && simulatedDrain.amount >= amount)
							reactorState = ReactorStateEnum.ONLINE;
						else
							reactorState = ReactorStateEnum.NO_FUEL;
					}
					
					if (reactorState == ReactorStateEnum.ONLINE || reactorState == ReactorStateEnum.STANDBY) {
						float percent = energyStorage.getEnergyStored() / (float)energyStorage.getMaxEnergyStored();
//						Aunis.info("state: " + reactorState + ", percent: " + percent);
						
						if (percent < AunisConfig.dhdConfig.activationLevel)
							reactorState = ReactorStateEnum.ONLINE;
						
						else if (percent >= AunisConfig.dhdConfig.deactivationLevel)
							reactorState = ReactorStateEnum.STANDBY;
					}
					
					if (reactorState == ReactorStateEnum.ONLINE) {
						fluidHandler.drainInternal(amount, true);
						energyStorage.receiveEnergy(AunisConfig.dhdConfig.energyPerNaquadah * AunisConfig.dhdConfig.powerGenerationMultiplier, false);
					}
				}
				
				// Not linked
				else {
					reactorState = ReactorStateEnum.NOT_LINKED;
				}
			}
			
			// No crystal
			else {
				reactorState = ReactorStateEnum.NO_CRYSTAL;
			}
		}
		
		else {
			// Client
			
			// Each 2s check for the sky
			if (world.getTotalWorldTime() % 40 == 0 && rendererStateClient != null && getRendererStateClient().biomeOverride == null) {
				rendererStateClient.setBiomeOverlay(BiomeOverlayEnum.updateBiomeOverlay(world, pos, SUPPORTED_OVERLAYS));
			}
		}
	}
	
	// Server
	private BiomeOverlayEnum determineBiomeOverride() {
		ItemStack stack = itemStackHandler.getStackInSlot(BIOME_OVERRIDE_SLOT);
		
		if (stack.isEmpty()) {
			return null;
		}
		
		BiomeOverlayEnum biomeOverlay = AunisConfig.stargateConfig.getBiomeOverrideItemMetaPairs().get(new ItemMetaPair(stack));
		
		if (getSupportedOverlays().contains(biomeOverlay)) {
			return biomeOverlay;
		}
		
		return null;
	}	
	
	public static final EnumSet<BiomeOverlayEnum> SUPPORTED_OVERLAYS = EnumSet.of(
			BiomeOverlayEnum.NORMAL,
			BiomeOverlayEnum.FROST,
			BiomeOverlayEnum.MOSSY,
			BiomeOverlayEnum.SOOTY,
			BiomeOverlayEnum.AGED);
	
	public static EnumSet<BiomeOverlayEnum> getSupportedOverlays() {
		return SUPPORTED_OVERLAYS;
	}
	
	private boolean hadControlCrystal;
	
	public boolean hasControlCrystal() {
		return !itemStackHandler.getStackInSlot(0).isEmpty();
	}
	
	private void updateCrystal() {
		boolean hasControlCrystal = hasControlCrystal();
		
		if (hadControlCrystal != hasControlCrystal) {
			if (hasControlCrystal) {
				sendState(StateTypeEnum.RENDERER_STATE, getState(StateTypeEnum.RENDERER_STATE));
			}
			
			else {
				clearSymbols();
			}
			
			hadControlCrystal = hasControlCrystal;
		}
	}
	 
	// -----------------------------------------------------------------------------
	// Symbol activation
	
	public void activateSymbol(SymbolMilkyWayEnum symbol) {	
		// By Glen Jolley from his unaccepted PR
		StargateAbstractBaseTile gateTile = getLinkedGate(world);

		// When using OC to dial, don't play sound of the DHD button press
		if (!gateTile.getStargateState().dialingComputer()) {
			
			if (symbol.brb())
				AunisSoundHelper.playSoundEvent(world, pos, SoundEventEnum.DHD_MILKYWAY_PRESS_BRB);
			else
				AunisSoundHelper.playSoundEvent(world, pos, SoundEventEnum.DHD_MILKYWAY_PRESS);
		}
		
        world.notifyNeighborsOfStateChange(pos, AunisBlocks.DHD_BLOCK, true);
        
        sendState(StateTypeEnum.DHD_ACTIVATE_BUTTON, new DHDActivateButtonState(symbol));
	}
	
	public void clearSymbols() {
        world.notifyNeighborsOfStateChange(pos, AunisBlocks.DHD_BLOCK, true);
        
        sendState(StateTypeEnum.DHD_ACTIVATE_BUTTON, new DHDActivateButtonState(true));
	}
	
	
	// -----------------------------------------------------------------------------
	// States
	
	protected void sendState(StateTypeEnum type, State state) {
		if (world.isRemote)
			return;
		
		if (targetPoint != null) {
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, type, state), targetPoint);
		}
		
		else {
			Aunis.logger.debug("targetPoint was null trying to send " + type + " from " + this.getClass().getCanonicalName());
		}
	}
	
	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				StargateAddressDynamic address = new StargateAddressDynamic(SymbolTypeEnum.MILKYWAY);
				
				if (isLinked()) {
					StargateAbstractBaseTile gateTile = getLinkedGate(world);
					
					address.addAll(gateTile.getDialedAddress());
					boolean brbActive = false;
					
					switch (gateTile.getStargateState()) {
						case ENGAGED_INITIATING:
							brbActive = true;
							break;
							
						case ENGAGED:
							address.clear();
							brbActive = true;
							break;
							
						default:
							break;
					}
					
					return new DHDRendererState(address, brbActive, determineBiomeOverride());
				}
				
				return new DHDRendererState(address, false, determineBiomeOverride());
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return new DHDRendererState();
		
			case DHD_ACTIVATE_BUTTON:
				return new DHDActivateButtonState();
				
			case GUI_UPDATE:
				return new DHDContainerGuiUpdate();
				
			case BIOME_OVERRIDE_STATE:
				return new StargateBiomeOverrideState();
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}

	public boolean isLinkedClient;
	
	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				float horizontalRotation = world.getBlockState(pos).getValue(AunisProps.ROTATION_HORIZONTAL) * -22.5f;
				rendererStateClient = ((DHDRendererState) state).initClient(pos, horizontalRotation, BiomeOverlayEnum.updateBiomeOverlay(world, pos, SUPPORTED_OVERLAYS));
				
				break;
		
			case DHD_ACTIVATE_BUTTON:
				DHDActivateButtonState activateState = (DHDActivateButtonState) state;
				
				if (activateState.clearAll)
					getRendererStateClient().clearSymbols(world.getTotalWorldTime());
				else
					getRendererStateClient().activateSymbol(world.getTotalWorldTime(), activateState.symbol);
				
				break;
				
			case GUI_UPDATE:
				DHDContainerGuiUpdate guiState = (DHDContainerGuiUpdate) state;
				
				fluidHandler.setFluid(new FluidStack(AunisFluids.moltenNaquadahRefined, guiState.fluidAmount));
				fluidHandler.setCapacity(guiState.tankCapacity);
				reactorState = guiState.reactorState;
				isLinkedClient = guiState.isLinked;
				
				break;
				
			case BIOME_OVERRIDE_STATE:
				StargateBiomeOverrideState overrideState = (StargateBiomeOverrideState) state;
								
				if (rendererStateClient != null) {
					getRendererStateClient().biomeOverride = overrideState.biomeOverride;
				}
				
				break;
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}
	
	
	// -----------------------------------------------------------------------------
	// Item handler
	
	public static final List<Item> SUPPORTED_UPGRADES = Arrays.asList(
			AunisItems.CRYSTAL_GLYPH_DHD);
	
	public static final int BIOME_OVERRIDE_SLOT = 5;
	
	private final ItemStackHandler itemStackHandler = new AunisItemStackHandler(6) {
		
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			Item item = stack.getItem();
			
			switch (slot) {
				case 0:
					return item == AunisItems.CRYSTAL_CONTROL_DHD;
				
				case 1:
				case 2:
				case 3:
				case 4:
					return SUPPORTED_UPGRADES.contains(item) && !hasUpgrade(item);
					
				case BIOME_OVERRIDE_SLOT:
					BiomeOverlayEnum override = AunisConfig.stargateConfig.getBiomeOverrideItemMetaPairs().get(new ItemMetaPair(stack));
					if (override == null)
						return false;
					
					return getSupportedOverlays().contains(override);
					
				default:
					return true;
			}
		}
		
		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return 1;
		}
		
		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			super.setStackInSlot(slot, stack);
			
			if (!world.isRemote && slot == 0) {
				// Crystal changed
				updateCrystal();
			}
		};
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemStack out = super.extractItem(slot, amount, simulate);
			
			if (!world.isRemote && slot == 0 && amount > 0 && !simulate) {
				// Removing crystal
				updateCrystal();
			}
			
			return out;
		}
		
		@Override
		protected void onContentsChanged(int slot) {
			switch (slot) {
				case BIOME_OVERRIDE_SLOT:
					sendState(StateTypeEnum.BIOME_OVERRIDE_STATE, new StargateBiomeOverrideState(determineBiomeOverride()));
					break;
					
				default:
					break;
			}
			
			super.onContentsChanged(slot);
			markDirty();
		}
	};
	
	// TODO Get rid of EnumKeyInterface
	public static enum DHDUpgradeEnum implements EnumKeyInterface<Item> {
		CHEVRON_UPGRADE(AunisItems.CRYSTAL_GLYPH_DHD);
		
		public Item item;

		private DHDUpgradeEnum(Item item) {
			this.item = item;
		}

		@Override
		public Item getKey() {
			return item;
		}
	}
	
	// -----------------------------------------------------------------------------
	// Fluid handler
	
	private final FluidTank fluidHandler = new FluidTank(new FluidStack(AunisFluids.moltenNaquadahRefined, 0), AunisConfig.dhdConfig.fluidCapacity) {
		
		@Override
		public boolean canFillFluidType(FluidStack fluid) {
			if (fluid == null)
				return false;
			
			return fluid.getFluid() == AunisFluids.moltenNaquadahRefined;
		}
		
		protected void onContentsChanged() {
			markDirty();
		}
	};
	
	
	// -----------------------------------------------------------------------------
	// Capabilities
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing == EnumFacing.DOWN))
				|| super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
		
		else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing == EnumFacing.DOWN))
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
		
		return super.getCapability(capability, facing);	
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {		
		if (linkedGate != null) {
			compound.setLong("linkedGate", linkedGate.toLong());
			compound.setInteger("linkId", linkId);
		}
		
		compound.setTag("itemStackHandler", itemStackHandler.serializeNBT());
		
		NBTTagCompound fluidHandlerCompound = new NBTTagCompound();
		fluidHandler.writeToNBT(fluidHandlerCompound);
		compound.setTag("fluidHandler", fluidHandlerCompound);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		if (compound.hasKey("linkedGate")) {
			linkedGate = BlockPos.fromLong(compound.getLong("linkedGate"));
			if (linkedGate.equals(new BlockPos(0, 0, 0))) // 1.8 fix
				linkedGate = null;
		}

		if (compound.hasKey("linkId")) {
			linkId = compound.getInteger("linkId");
		}
		
		itemStackHandler.deserializeNBT(compound.getCompoundTag("itemStackHandler"));
		
		if (compound.getBoolean("hasUpgrade") || compound.getBoolean("insertAnimation")) {
			itemStackHandler.setStackInSlot(1, new ItemStack(AunisItems.CRYSTAL_GLYPH_DHD));
		}
		
		fluidHandler.readFromNBT(compound.getCompoundTag("fluidHandler"));
		
		if (compound.hasKey("inventory")) {				
			NBTTagCompound inventoryTag = compound.getCompoundTag("inventory");
			NBTTagList tagList = inventoryTag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
			
			if (tagList.tagCount() > 0) {
				itemStackHandler.setStackInSlot(0, new ItemStack(AunisItems.CRYSTAL_CONTROL_DHD));
				
				int energy = tagList.getCompoundTagAt(0).getCompoundTag("ForgeCaps").getCompoundTag("Parent").getInteger("energy");
				int fluidAmount = energy / AunisConfig.dhdConfig.energyPerNaquadah;
				fluidHandler.fillInternal(new FluidStack(AunisFluids.moltenNaquadahRefined, fluidAmount), true);
			}
		}
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// Rendering distance
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}
}
