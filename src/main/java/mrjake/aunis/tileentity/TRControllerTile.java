package mrjake.aunis.tileentity;

import mrjake.aunis.renderer.transportrings.TRControllerRenderer;
import mrjake.aunis.tesr.SpecialRendererProviderInterface;
import mrjake.aunis.util.ILinkable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TRControllerTile extends TileEntity implements ILinkable, SpecialRendererProviderInterface {

	@Override
	public void onLoad() {
		if (world.isRemote) {
			renderer = new TRControllerRenderer(this);
		}
	}
	
	// ------------------------------------------------------------------------
	// Rings 
	private BlockPos linkedRings;
	
	public void setLinkedRings(BlockPos pos) {
		this.linkedRings = pos;
		
		markDirty();
	}
	
	public BlockPos getLinkedRings() {
		return linkedRings;
	}
	
	@Override
	public boolean isLinked() {
		return linkedRings != null;
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
	
	@Override
	public void render(double x, double y, double z, float partialTicks) {
		renderer.render(x, y, z, partialTicks);
	}
}
