package mrjake.aunis.tileentity;

import mrjake.aunis.renderer.transportrings.TRControllerRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TRControllerTile extends TileEntity {

	
	
	// ------------------------------------------------------------------------
	// Rings 
	private BlockPos linkedRings;
	
	public void setLinkedRings(BlockPos pos) {
		this.linkedRings = pos;
	}
	
	public BlockPos getLinkedRings() {
		return linkedRings;
	}
	
	public TransportRingsTile getLinkedRingsTile(World world) {
		return (linkedRings != null ? ((TransportRingsTile) world.getTileEntity(linkedRings)) : null);
	}
	
	
	// ------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (linkedRings != null)
			compound.setLong("linkedRings", linkedRings.toLong());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("linkedRings"))
			linkedRings = BlockPos.fromLong(compound.getLong("linkedRings"));
		
		super.readFromNBT(compound);
	}
	
	
	// ------------------------------------------------------------------------
	// Renderer
	private TRControllerRenderer renderer;
	
	public TRControllerRenderer getRenderer() {
		if (renderer == null)
			renderer = new TRControllerRenderer(this);
		
		return renderer;
	}
}
