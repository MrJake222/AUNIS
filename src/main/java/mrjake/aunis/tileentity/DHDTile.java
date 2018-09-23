package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdateRequestToServer;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.Renderer;
import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.TeleportHelper;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import scala.annotation.meta.param;

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
		compound.setBoolean("isLinkedGateEngaged", isLinkedGateEngaged);
		compound.setBoolean("hasUpgrade", hasUpgrade);
		compound.setBoolean("insertAnimation", insertAnimation);
		
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
		hasUpgrade = compound.getBoolean("hasUpgrade");
		insertAnimation = compound.getBoolean("insertAnimation");
		
		super.readFromNBT(compound);
	}
	
	private static final int scanDelayTicks = 20;
	
	private boolean firstTick = true;
	
	private long lastScan = 0;
	private AxisAlignedBB scanArea;
	private int emptyScans = 0;
	private boolean clearScans = true;
	
	@Override
	public void update() {
		
		// Can't do this in onLoad() because then world is not fully loaded
		if (firstTick) {
			firstTick = false;
			
			if (world.isRemote)
				AunisPacketHandler.INSTANCE.sendToServer( new TileUpdateRequestToServer(pos) );
/*			else 
				*/
		}
		
		// Server
		if (!world.isRemote) {
			StargateBaseTile gateTile = getLinkedGate(world);
			
			if (gateTile != null) {
				if (world.getTotalWorldTime() - lastScan > scanDelayTicks) {
					if (gateTile.isEngaged()) {
						if (!gateTile.isInitiating()) {
							if (clearScans) {
								emptyScans = 0;
								clearScans = false;
							}
							
							StargatePos targetGatePos = StargateNetwork.get(world).getStargate(gateTile.dialedAddress);
							World targetWorld = TeleportHelper.getWorld(targetGatePos.getDimension());
							BlockPos targetPos = targetGatePos.getPos();
							StargateBaseTile targetTile = (StargateBaseTile) TeleportHelper.getWorld(targetGatePos.getDimension()).getTileEntity(targetGatePos.getPos());
							
							BlockPos targetDHD = targetTile.getLinkedDHD();
														
							int entities = gateTile.getEntitiesPassed();
							
							if (entities > 0) {
								if (targetWorld.isBlockLoaded(targetPos)) {
									if (targetDHD != null) {
										scanArea = new AxisAlignedBB(targetDHD.add(new Vec3i(-5, -5, -5)), targetDHD.add(new Vec3i(5, 5, 5)));
										List<EntityPlayerMP> players = world.getEntitiesWithinAABB(EntityPlayerMP.class, scanArea);
										
										if (players.isEmpty())
											emptyScans++;
										else
											emptyScans = 0;
									}
								}
								else {
									emptyScans++;
								}
								
								if (emptyScans > 2) {
									clearScans = true;
									AunisPacketHandler.INSTANCE.sendToServer(new GateRenderingUpdatePacketToServer(EnumSymbol.BRB.id, targetDHD));
								}
							}
						}
					}
					
					lastScan = world.getTotalWorldTime();
				}
			}
			
			/*if (gateTile != null && (world.getTotalWorldTime() - lastScan) > scanDelayTicks ) {
				// If linked gate is engaged, perform periodical scanning for players
				// to automatically close the gate
				if ( gateTile.isEngaged()) {
					if (gateTile.isInitiating()) {
						if (gateTile.getEntitiesPassed() > 0) {
							List<EntityPlayerMP> players = world.getEntitiesWithinAABB(EntityPlayerMP.class, scanArea);
							
							if (players.isEmpty())
								emptyScans++;
							else
								emptyScans = 0;
							
							if (emptyScans > 5)
								AunisPacketHandler.INSTANCE.sendToServer(new GateRenderingUpdatePacketToServer(EnumSymbol.BRB.id, pos));
							
							lastScan = world.getTotalWorldTime();
						}
					}
					
					else {
						StargatePos stargatePos = StargateNetwork.get(world).getStargate(gateTile.dialedAddress);
						if (stargatePos.getWorld().isBlockLoaded(stargatePos.getPos()))
							emptyScans = 0;
						else
							emptyScans++;
						
						if (emptyScans > 5)
							AunisPacketHandler.INSTANCE.sendToServer(new GateRenderingUpdatePacketToServer(EnumSymbol.BRB.id, stargatePos.getPos()));
					}
				}
			}*/
		}
	}
	
	private boolean hasUpgrade = false;
	private boolean insertAnimation = false;
	
	public boolean hasUpgrade() {
		return hasUpgrade;
	}
	
	@Override
	public void setUpgrade(boolean hasUpgrade) {
		this.hasUpgrade = hasUpgrade;
		
		markDirty();
	}
	
    public boolean getInsertAnimation() {
		return insertAnimation;
	}

    @Override
	public void setInsertAnimation(boolean insertAnimation) {
		this.insertAnimation = insertAnimation;
		
		markDirty();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}
}
