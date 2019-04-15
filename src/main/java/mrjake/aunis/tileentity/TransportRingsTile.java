package mrjake.aunis.tileentity;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdateRequestToServer;
import mrjake.aunis.packet.transportrings.StartRingsAnimationToClient;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.TransportRingsRendererState;
import mrjake.aunis.renderer.transportrings.TransportRingsRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class TransportRingsTile extends TileEntity implements ITileEntityRendered, ITickable {

	public boolean firstTick = true;
	
	@Override
	public void update() {
		if (firstTick) {
			firstTick = false;
			
			if (world.isRemote) {
				AunisPacketHandler.INSTANCE.sendToServer(new TileUpdateRequestToServer(pos));
			}
		}
		
//		getRingAnimatorServer().animate();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		getRendererState().toNBT(compound);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		getRendererState().fromNBT(compound);
		
		super.readFromNBT(compound);
	}
	
	
	// ---------------------------------------------------------------------------------
//	private RingAnimatorServer ringAnimatorServer;
//	
//	private RingAnimatorServer getRingAnimatorServer() {
//		if (ringAnimatorServer == null)
//			ringAnimatorServer = new RingAnimatorServer(world, getTransportRingsRendererState().animatorState);
//		
//		return ringAnimatorServer;
//	}
//	
	public void animationStart() {
		getTransportRingsRendererState().animationStart = world.getTotalWorldTime();
		getTransportRingsRendererState().ringsUprising = true;
		getTransportRingsRendererState().isAnimationActive = true;
		
		TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		AunisPacketHandler.INSTANCE.sendToAllAround(new StartRingsAnimationToClient(pos, getTransportRingsRendererState().animationStart), point);
	}
	
	// ---------------------------------------------------------------------------------
	TransportRingsRenderer renderer;
	TransportRingsRendererState rendererState;
	
	@Override
	public ISpecialRenderer<TransportRingsRendererState> getRenderer() {
		if (renderer == null)
			renderer = new TransportRingsRenderer(this);
		
		return renderer;
	}
	
	public TransportRingsRenderer getTransportRingsRenderer() {
		return (TransportRingsRenderer) getRenderer();
	}

	@Override
	public RendererState getRendererState() {
		if (rendererState == null)
			rendererState = new TransportRingsRendererState();
		
		return rendererState;
	}
	
	public TransportRingsRendererState getTransportRingsRendererState() {
		return (TransportRingsRendererState) getRendererState();
	}

	@Override
	public RendererState createRendererState(ByteBuf buf) {
		return new TransportRingsRendererState().fromBytes(buf);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos.add(-4, 0, -4), pos.add(4, 7, 4));
	}
}
