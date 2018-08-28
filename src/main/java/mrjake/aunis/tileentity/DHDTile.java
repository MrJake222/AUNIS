package mrjake.aunis.tileentity;

import java.util.ArrayList;

import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdateRequestToServer;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.Renderer;
import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.renderer.state.RendererState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDTile extends RenderedTileEntity implements ITickable {
	
	private boolean isLinkedGateEngaged;
	
	public void setLinkedGateEngagement(boolean engaged) {
		isLinkedGateEngaged = engaged;
		
		markDirty();
	}
	
	private BlockPos linkedGate = null;
	
	@SuppressWarnings("rawtypes")
	@Override
	public Renderer getRenderer() {
		if (renderer == null)
			renderer = new DHDRenderer(this);
		
		return (DHDRenderer) renderer;
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
		Aunis.log("Linking to gate at " + gate.toString());
		
		this.linkedGate = gate;
		markDirty();
	}
	
	public StargateBaseTile getLinkedGate(World world) {
		if (linkedGate == null)
			return null;
		else
			return (StargateBaseTile) world.getTileEntity(linkedGate);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		BlockPos gate;
		
		if (linkedGate == null) {
			Aunis.log("linkedGate is null!");
			gate = new BlockPos(0,0,0);
		}
		else
			gate = linkedGate;
		
		compound.setLong("linkedGate", gate.toLong());
		compound.setBoolean("isLinkedGateEngaged", isLinkedGateEngaged);
		
		getRendererState().toNBT(compound);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		DHDRendererState rendererState = new DHDRendererState(compound);
		isLinkedGateEngaged = compound.getBoolean("isLinkedGateEngaged");
		
		if (!isLinkedGateEngaged)
			rendererState.activeButtons = new ArrayList<Integer>();
		
		this.rendererState = rendererState;
		
		linkedGate = BlockPos.fromLong( compound.getLong("linkedGate") );
		
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
	
    @Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}
}
