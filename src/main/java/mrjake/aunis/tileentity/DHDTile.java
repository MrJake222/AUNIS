package mrjake.aunis.tileentity;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.update.renderer.RendererUpdateRequestToServer;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.UpgradeRendererState;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.upgrade.DHDUpgradeRenderer;
import mrjake.aunis.upgrade.UpgradeRenderer;
import mrjake.aunis.util.ILinkable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DHDTile extends TileEntity implements ITileEntityRendered, ITileEntityUpgradeable, ITickable, ILinkable {
	
	private ISpecialRenderer<DHDRendererState> renderer;
	private RendererState rendererState;
	
	private DHDUpgradeRenderer upgradeRenderer;
	private UpgradeRendererState upgradeRendererState;
	
	private BlockPos linkedGate = null;
	
	@Override
	public ISpecialRenderer<DHDRendererState> getRenderer() {
		if (renderer == null)
			renderer = new DHDRenderer(this);
		
		return (DHDRenderer) renderer;
	}
	
	public DHDRenderer getDHDRenderer() {
		return (DHDRenderer) getRenderer();
	}
	
	public DHDRendererState getDHDRendererState() {
		return (DHDRendererState) getRendererState();
	}
	
	@Override
	public RendererState getRendererState() {	
		if (rendererState == null)
			rendererState = new DHDRendererState();
				
		return rendererState;
	}
	
	@Override
	public RendererState createRendererState(ByteBuf buf) {
		return new DHDRendererState().fromBytes(buf);
	}
	
	public void clearRendererButtons() {
		getDHDRendererState().activeButtons.clear();
		
		markDirty();
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
			upgradeRenderer = new DHDUpgradeRenderer(this);
		
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
		
		getRendererState().toNBT(compound);
		getUpgradeRendererState().toNBT(compound);
		
		compound.setTag("inventory", inventory.serializeNBT());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		
		getRendererState().fromNBT(compound);
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
				AunisPacketHandler.INSTANCE.sendToServer( new RendererUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide()) );
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
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
//		boolean b = oldState.getBlock() != newState.getBlock();
		
		Aunis.info("dhdTile refresh");
		
		return true;
	}
}
