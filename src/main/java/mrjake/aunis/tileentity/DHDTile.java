package mrjake.aunis.tileentity;

import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.packet.state.StateUpdateRequestToServer;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.state.UpgradeRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.state.DHDActivateButtonState;
import mrjake.aunis.state.DHDRendererState;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.ITileEntityStateProvider;
import mrjake.aunis.state.State;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.upgrade.DHDUpgradeRenderer;
import mrjake.aunis.upgrade.UpgradeRenderer;
import mrjake.aunis.util.ILinkable;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DHDTile extends TileEntity implements ITileEntityUpgradeable, ITickable, ILinkable, ITileEntityStateProvider {
	
	private DHDRenderer renderer;
	private DHDRendererState rendererState;
	
	private DHDUpgradeRenderer upgradeRenderer;
	private UpgradeRendererState upgradeRendererState;
	
	private BlockPos linkedGate = null;
	
	public DHDRenderer getDHDRenderer() {
		if (renderer == null)
			renderer = new DHDRenderer(this);
		
		return renderer;
	}
	
	public DHDRendererState getDHDRendererState() {
		if (rendererState == null)
			rendererState = new DHDRendererState();
		
		return rendererState;
	}

	public void setLinkedGate(BlockPos gate) {		
		this.linkedGate = gate;
		
		markDirty();
	}
	
	@Override
	public boolean isLinked() {
		return this.linkedGate != null;
	}
	
	@Override
	public UpgradeRenderer getUpgradeRenderer() {		
		if (upgradeRenderer == null)
			upgradeRenderer = new DHDUpgradeRenderer(world, getDHDRenderer().getHorizontalRotation());
		
		return upgradeRenderer;
	}
	
	@Override
	public UpgradeRendererState getUpgradeRendererState() {
		if (upgradeRendererState == null)
			upgradeRendererState = new UpgradeRendererState();
		
		return upgradeRendererState;
	}
	
	public StargateBaseTile getLinkedGate(World world) {
		if (linkedGate == null)
			return null;
		
		return (StargateBaseTile) world.getTileEntity(linkedGate);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		BlockPos gate;
		
		if (linkedGate == null)
			gate = new BlockPos(0,0,0);
		else
			gate = linkedGate;
		
		compound.setLong("linkedGate", gate.toLong());
		compound.setBoolean("hasUpgrade", hasUpgrade);
		compound.setBoolean("insertAnimation", insertAnimation);
		
		compound.setTag("rendererState", getDHDRendererState().serializeNBT());
		getUpgradeRendererState().toNBT(compound);
		
		compound.setTag("inventory", inventory.serializeNBT());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		
		if (compound.hasKey("rendererState") && compound.getTag("rendererState") instanceof NBTTagCompound) // Check for versions below 1.5
			getDHDRendererState().deserializeNBT((NBTTagCompound) compound.getTag("rendererState"));
		
		getUpgradeRendererState().fromNBT(compound);
		
		linkedGate = BlockPos.fromLong( compound.getLong("linkedGate") );
		hasUpgrade = compound.getBoolean("hasUpgrade");
		insertAnimation = compound.getBoolean("insertAnimation");
		
		if (compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));

		super.readFromNBT(compound);
	}
		
	private boolean firstTick = true;
	
	@Override
	public void update() {
		
		// Can't do this in onLoad() because then world is not fully loaded
		if (firstTick) {
			firstTick = false;
			
			if (world.isRemote)
				AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), EnumStateType.RENDERER_STATE));
		}
	}
	
	private boolean hasUpgrade = false;
	private boolean insertAnimation = false;
	
	@Override
	public boolean hasUpgrade() {
		return hasUpgrade;
	}
	
	@Override
	public void setUpgrade(boolean hasUpgrade) {
		this.hasUpgrade = hasUpgrade;
		
		markDirty();
	}
	
	@Override
	public Item getAcceptedUpgradeItem() {
		return AunisItems.crystalGlyphDhd;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}
	
	// -----------------------------------------------------------------------------
	// Symbol activation
	public void activateSymbol(int id) {
		activateSymbols(Arrays.asList(id));
	}
	
	public void activateSymbols(List<Integer> idList) {		
		if (idList.size() == 1) {
			if (idList.get(0) == EnumSymbol.BRB.id)
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.DHD_PRESS_BRB, 0.5f);
			else
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.DHD_PRESS, 0.5f);
		}
		
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, EnumStateType.DHD_ACTIVATE_BUTTON_STATE, new DHDActivateButtonState(idList)), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
		
		getDHDRendererState().activeButtons.addAll(idList);
		markDirty();
	}
	
	public void clearSymbols() {
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, EnumStateType.DHD_ACTIVATE_BUTTON_STATE, new DHDActivateButtonState(true)), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
		
		getDHDRendererState().activeButtons.clear();
		markDirty();
	}
	
	// -----------------------------------------------------------------------------
	// States
	@Override
	public State getState(EnumStateType stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return getDHDRendererState();
		
			case DHD_ACTIVATE_BUTTON_STATE:
				return null;
				// Should send this directly from the server
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}

	@Override
	public State createState(EnumStateType stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return new DHDRendererState();
		
			case DHD_ACTIVATE_BUTTON_STATE:
				return new DHDActivateButtonState();
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}

	@Override
	public void setState(EnumStateType stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				getDHDRenderer().activateSymbols(((DHDRendererState) state).activeButtons); 
				
				break;
		
			case DHD_ACTIVATE_BUTTON_STATE:
				DHDActivateButtonState activateState = (DHDActivateButtonState) state;
				
				if (activateState.clearOnly)
					getDHDRenderer().clearSymbols();
				else
					getDHDRenderer().activateSymbols(activateState.idList);
				
				break;
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}
	
	// -----------------------------------------------------------------------------
	private ItemStackHandler inventory = new ItemStackHandler(1) {
		
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			
			markDirty();
		};
	};
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T)inventory : super.getCapability(capability, facing);	
	}
}
