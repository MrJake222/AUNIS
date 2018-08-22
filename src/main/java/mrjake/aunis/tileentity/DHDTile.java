package mrjake.aunis.tileentity;

import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.DHDRendererState;
import mrjake.aunis.renderer.StargateRendererState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDTile extends TileEntity {
	
	private DHDRenderer renderer;
	private DHDRendererState rendererState;
	
	private BlockPos linkedGate = null;
		
	public void establishLinkToStargate(BlockPos gate) {
		Aunis.log("Linking to gate at " + gate.toString());
		
		this.linkedGate = gate;
		markDirty();
	}
	
	public BlockPos getLinkedGate() {
		return linkedGate;
	}
	
	public StargateBaseTile getLinkedGate(World world) {
		if (linkedGate == null)
			return null;
		else
			return (StargateBaseTile) world.getTileEntity(linkedGate);
	}
	
	public void setRendererState(DHDRendererState rendererState) {
		this.rendererState = rendererState;
		Aunis.info("DHDRendererState synced: "+rendererState.toString());
		
		markDirty();
	}
	
	public DHDRendererState getRendererState() {
		if (rendererState == null)
			rendererState = new DHDRendererState(pos);
		
		return rendererState;
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
		
		getRendererState().toNBT(compound);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		rendererState = new DHDRendererState(compound);
		
		linkedGate = BlockPos.fromLong( compound.getLong("linkedGate") );
		
		super.readFromNBT(compound);
	}
	
	public DHDRenderer getRenderer() {
		if (renderer == null)
			renderer = new DHDRenderer(this);
		
		return renderer;
	}
    @Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}
}
