package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdateRequestToServer;
import mrjake.aunis.packet.transportrings.StartPlayerFadeOutToClient;
import mrjake.aunis.packet.transportrings.StartRingsAnimationToClient;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.TransportRingsRendererState;
import mrjake.aunis.renderer.transportrings.PlayerFadeOutRenderEvent;
import mrjake.aunis.renderer.transportrings.TransportRingsRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class TransportRingsTile extends TileEntity implements ITileEntityRendered, ITickable {

	// ---------------------------------------------------------------------------------
	// Ticking
	private boolean firstTick = true;
	private boolean waitForFadeOut = false;
	private boolean waitForTeleport = false;
	private boolean waitForClearout = false;
	
//	private boolean doLightUpdate = false;
	
	private static final int fadeOutTimeout = (int) (30 + TransportRingsRenderer.uprisingInterval*TransportRingsRenderer.ringCount + TransportRingsRenderer.animationDiv * Math.PI);
	public static final int fadeOutTotalTime = 2 * 20; // 2s
	
	private static final int teleportTimeout = fadeOutTimeout + fadeOutTotalTime/2;
	private static final int clearoutTimeout = (int) (100 + TransportRingsRenderer.fallingInterval*TransportRingsRenderer.ringCount + TransportRingsRenderer.animationDiv * Math.PI);
	
//	private long tickStartFog;
	
	@Override
	public void update() {
		if (firstTick) {
			firstTick = false;
			
			if (world.isRemote) {
				AunisPacketHandler.INSTANCE.sendToServer(new TileUpdateRequestToServer(pos));
			}
		}
		
		if (!world.isRemote) {
			long effTick = world.getTotalWorldTime() - getTransportRingsRendererState().animationStart;
			
			// 30 - offset
			if (waitForFadeOut && effTick >= fadeOutTimeout) {
				waitForFadeOut = false;
				waitForTeleport = true;
				
//				doLightUpdate = true;
				
//				tickStartFog = world.getTotalWorldTime();
				
				
				
				for (Entity entity : teleportList) {
					if (entity instanceof EntityPlayerMP) {
						AunisPacketHandler.INSTANCE.sendTo(new StartPlayerFadeOutToClient(), (EntityPlayerMP) entity);
					}
				}
			}
				
				
			else if (waitForTeleport && effTick >= teleportTimeout) {
				waitForTeleport = false;
				waitForClearout = true;
				
				BlockPos target = ringsMap.get(closestAddress);
				BlockPos teleportVector = target.subtract(pos);
				
				for (Entity entity : teleportList) {
					BlockPos ePos = entity.getPosition().add(teleportVector);
										
					entity.setPositionAndUpdate(ePos.getX(), ePos.getY(), ePos.getZ());
				}
			}
				
			else if (waitForClearout && effTick >= clearoutTimeout) {
				waitForClearout = false;

				setBarrierBlocks(false);
			}
			
			
			// Setting light to blocks
//			if (doLightUpdate) {
//				int light = (int) (PlayerFadeOutRenderEvent.calcFog(world, tickStartFog, 0) * 15.0f);
//				
//				if (light < 0)
//					doLightUpdate = false;
//				
//				else {
//					boolean updateShown = false;
//					
//					for (BlockPos invPos : invisibleBlocks) {
//						IBlockState state = world.getBlockState(invPos);
//						
//						if (state.getValue(AunisProps.LIGHT_LEVEL) != light) {						
//							if (!updateShown) {
//								updateShown = true;
//								
//								Aunis.info("update[light="+light+", tick=" + (world.getTotalWorldTime()-tickStartFog) + "]");
//							}
//							
//							world.setBlockState(invPos, state.withProperty(AunisProps.LIGHT_LEVEL, light), 2);
//						}
//					}
//				}
//			}
		}
	}
	
	
	// ---------------------------------------------------------------------------------
	// Rings network
	private int address = -1;
	private Map<Integer, BlockPos> ringsMap = new HashMap<>();
	
	private double closestDistance;
	private int closestAddress = -1;
	
	private List<Entity> teleportList;
	
	public int getAddress() {
		if (address == -1) {
			address = (int)Math.round(Math.random() * 100);
		}
		
		return address;
	}
	
	public TransportRingsTile getClosest() {
		BlockPos ringsPos = ringsMap.get(closestAddress);
		
		return ringsPos == null ? null : (TransportRingsTile) world.getTileEntity(ringsPos);
	}
	
	public void addRings(TransportRingsTile ringsTile) {
		ringsMap.put(ringsTile.getAddress(), ringsTile.getPos());
		
		checkClosest(ringsTile.getPos(), ringsTile.getAddress());
	}
	
	public void removeRings(int address) {
		ringsMap.remove(address);
		
		if (address == closestAddress) {
			Aunis.info("resetting closest of " + getAddress());
			
			resetClosest();
		}
	}
	
	public void resetClosest() {
		closestAddress = -1;
		
		for (int address : ringsMap.keySet()) {
			checkClosest(ringsMap.get(address), address);
		}
	}
	
	/**
	 * Iterates over ringsMap and calls removeRings() on all of them
	 */
	public void unlinkAllRings() {
		for (BlockPos rings : ringsMap.values()) {
			TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(rings);
			
			ringsTile.removeRings(getAddress());
		}
	}
	
	public void listAllRings() {
		Aunis.info(pos + " rings list: ");
		
		for (Integer address : ringsMap.keySet()) {
			Aunis.info("\t"+address+": "+ringsMap.get(address) + (address == closestAddress ? " CLOSEST" : ""));
		}
	}
	
	private void checkClosest(BlockPos targetPos, int address) {
		double distance = targetPos.getDistance(pos.getX(), pos.getY(), pos.getZ());
		
		if (distance < closestDistance || closestAddress == -1) {
			closestDistance = distance;
			closestAddress = address;
		}
	}
	
	// ---------------------------------------------------------------------------------
	// Teleportation
	
	public void startAnimationAndTeleport() {
		animationStart();
		
		setBarrierBlocks(true);		
		teleportList = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.add(-2, 2, -2), pos.add(3, 6, 3)));
		
		waitForFadeOut = true;
	}
	
	
	private static final List<BlockPos> invisibleBlocksTemplate = Arrays.asList(
			new BlockPos(0, 2, 3),
			new BlockPos(1, 2, 3),
			new BlockPos(2, 2, 2),
			new BlockPos(3, 2, 1)
	);
	
	private List<BlockPos> invisibleBlocks = new ArrayList<BlockPos>();
	
	private void setBarrierBlocks(boolean set) {
		if (set) {
			invisibleBlocks.clear();
		
			for(int y=1; y<4; y++) {
				for (Rotation rotation : Rotation.values()) {
					for (BlockPos invPos : invisibleBlocksTemplate) {
						
						BlockPos newPos = new BlockPos(this.pos).add(invPos.rotate(rotation)).add(0, y, 0);
												
						world.setBlockState(newPos, AunisBlocks.invisibleBlock.getDefaultState());
						
//						if (y == 1)
						invisibleBlocks.add(newPos);
					}
				}
			}
		}
		
		else {
			for (BlockPos invPos : invisibleBlocks) {
				world.setBlockToAir(invPos);
			}
		}
//		if (set)world.setBlockState(pos.add(0,2,0), Blocks.GLASS.getDefaultState());
	}
	
	// ---------------------------------------------------------------------------------
	// NBT data
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		getRendererState().toNBT(compound);
		
		compound.setInteger("address", address);
		compound.setInteger("ringsMapLength", ringsMap.size());
		
		int i = 0;
		for (int address : ringsMap.keySet()) {
			compound.setInteger("ringsAddress" + i, address);
			compound.setLong("ringsPos" + i, ringsMap.get(address).toLong());
			
			i++;
		}
		
		compound.setDouble("closestDistance", closestDistance);
		compound.setInteger("closestAddress", closestAddress);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		getRendererState().fromNBT(compound);
		
		address = compound.hasKey("address") ? compound.getInteger("address") : getAddress();
		
		if (compound.hasKey("ringsMapLength")) {
			int len = compound.getInteger("ringsMapLength");
			
			ringsMap.clear();
			
			for (int i=0; i<len; i++) {
				int addr = compound.getInteger("ringsAddress" + i);
				BlockPos pos = BlockPos.fromLong(compound.getLong("ringsPos" + i));
				
				ringsMap.put(addr, pos);
			}
		}
		
		closestDistance = compound.getDouble("closestDistance");
		closestAddress = compound.getInteger("closestAddress");
		
		super.readFromNBT(compound);
	}
	
	
	public void animationStart() {
		getTransportRingsRendererState().animationStart = world.getTotalWorldTime();
		getTransportRingsRendererState().ringsUprising = true;
		getTransportRingsRendererState().isAnimationActive = true;
		
		Aunis.info("animationStart: " + getTransportRingsRendererState().animationStart);
		
		TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		AunisPacketHandler.INSTANCE.sendToAllAround(new StartRingsAnimationToClient(pos, getTransportRingsRendererState().animationStart), point);
	}
	
	
	// ---------------------------------------------------------------------------------
	// Renders
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
