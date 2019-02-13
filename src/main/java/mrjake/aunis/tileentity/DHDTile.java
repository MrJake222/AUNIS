package mrjake.aunis.tileentity;

import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdateRequestToServer;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.UpgradeRendererState;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.upgrade.DHDUpgradeRenderer;
import mrjake.aunis.upgrade.UpgradeRenderer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDTile extends TileEntity implements ITileEntityRendered, ITileEntityUpgradeable, ITickable {
	
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
			rendererState = new DHDRendererState(pos);
				
		return rendererState;
	}
	
	@Override
	public void setRendererState(RendererState rendererState) {		
		this.rendererState = rendererState;		
	}
	
	public void setLinkedGate(BlockPos gate) {		
		this.linkedGate = gate;
		markDirty();
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
			upgradeRendererState = new UpgradeRendererState(pos);
		
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
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		
		this.rendererState = new DHDRendererState(compound);
		this.upgradeRendererState = new UpgradeRendererState(compound);
		
		linkedGate = BlockPos.fromLong( compound.getLong("linkedGate") );
		hasUpgrade = compound.getBoolean("hasUpgrade");
		insertAnimation = compound.getBoolean("insertAnimation");
		
		super.readFromNBT(compound);
	}
		
	private boolean firstTick = true;
	
	@Override
	public void update() {
		
		// Can't do this in onLoad() because then world is not fully loaded
		if (firstTick) {
			firstTick = false;
			
			if (world.isRemote)
				AunisPacketHandler.INSTANCE.sendToServer( new TileUpdateRequestToServer(pos) );
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
//	public boolean hasUpgrade() {
//		return hasUpgrade;
//	}
//	
//	@Override
//	public void setUpgrade(boolean hasUpgrade) {
//		this.hasUpgrade = hasUpgrade;
//		
//		markDirty();
//	}
//	
//    public boolean getInsertAnimation() {
//		return insertAnimation;
//	}
//
//    @Override
//	public void setInsertAnimation(boolean insertAnimation) {
//		this.insertAnimation = insertAnimation;
//		
//		markDirty();
//	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}
}
