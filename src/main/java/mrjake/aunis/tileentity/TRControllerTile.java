package mrjake.aunis.tileentity;

import java.util.EnumSet;

import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import mrjake.aunis.renderer.transportrings.TRControllerRenderer;
import mrjake.aunis.tesr.RendererInterface;
import mrjake.aunis.tesr.RendererProviderInterface;
import mrjake.aunis.util.ILinkable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TRControllerTile extends TileEntity implements ITickable, ILinkable, RendererProviderInterface {
	
	private BiomeOverlayEnum biomeOverlay = BiomeOverlayEnum.NORMAL;
	
	public BiomeOverlayEnum getBiomeOverlay() {
		return biomeOverlay;
	}
	
	@Override
	public void onLoad() {
		if (world.isRemote) {
			renderer = new TRControllerRenderer(this);
			biomeOverlay = BiomeOverlayEnum.updateBiomeOverlay(world, pos, SUPPORTED_OVERLAYS);
		}
	}
	
	@Override
	public void update() {
		if (world.isRemote) {
			// Client
			
			if (world.getTotalWorldTime() % 40 == 0) {
				biomeOverlay = BiomeOverlayEnum.updateBiomeOverlay(world, pos, SUPPORTED_OVERLAYS);
			}
		}
	}
	
	public static final EnumSet<BiomeOverlayEnum> SUPPORTED_OVERLAYS = EnumSet.of(
			BiomeOverlayEnum.NORMAL,
			BiomeOverlayEnum.FROST,
			BiomeOverlayEnum.MOSSY);
	
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
	
	public boolean isLinked() {
		return linkedRings != null;
	}
	
	public TransportRingsTile getLinkedRingsTile(World world) {
		return (linkedRings != null ? ((TransportRingsTile) world.getTileEntity(linkedRings)) : null);
	}
	
	@Override
	public boolean canLinkTo() {
		return !isLinked();
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
	public RendererInterface getRenderer() {
		return renderer;
	}
}
