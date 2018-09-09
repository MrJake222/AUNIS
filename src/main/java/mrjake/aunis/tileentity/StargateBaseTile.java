package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.vecmath.Vector2f;

import mrjake.aunis.AunisSoundEvents;
import mrjake.aunis.block.BlockFaced;
import mrjake.aunis.block.BlockTESRMember;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.dhd.renderingUpdate.ClearLinkedDHDButtons;
import mrjake.aunis.packet.gate.teleportPlayer.RetrieveMotionToClient;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdateRequestToServer;
import mrjake.aunis.renderer.Renderer;
import mrjake.aunis.renderer.StargateRenderer;
import mrjake.aunis.renderer.StargateRenderer.EnumVortexState;
import mrjake.aunis.renderer.state.LimitedStargateRendererState;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.StargateRendererState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.stargate.TeleportHelper;
import mrjake.aunis.stargate.merge.MergeHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class StargateBaseTile extends RenderedTileEntity implements ITickable {
	
	private static final int maxChevrons = 8;

	private LimitedStargateRendererState limitedState;
	private BlockPos linkedDHD = null;
	
	private boolean isEngaged;
	private boolean isInitiating;
	
	private long waitForEngage;
	private long waitForClose;
	private boolean unstableVortex;
	private boolean isClosing;
	
	private int dialedChevrons;
		
	public List<EnumSymbol> gateAddress;
	public List<EnumSymbol> dialedAddress = new ArrayList<EnumSymbol>();
	
	public boolean addSymbolToAddress(EnumSymbol symbol, DHDTile dhdTile) {		
		if (dialedAddress.contains(symbol)) 
			return false;
		
		if ( dialedAddress.size() == (dhdTile.hasUpgrade() ? 8 : 7) )
			return false;
		
		dialedAddress.add(symbol);
		
		getStargateRendererState().activeChevrons++;
		getStargateRendererState().isFinalActive = dialedAddress.size() == maxChevrons;
		
		return true;
	}
	
	public void clearAddress() {		
		dialedAddress.clear();
	}
	
	public void openGate(boolean initiating, Integer incomingChevrons) {
		isInitiating = initiating;
		
		if (isInitiating)
			dialedChevrons = dialedAddress.size();
		else
			dialedChevrons = incomingChevrons;
		
		unstableVortex = true;
		waitForEngage = world.getTotalWorldTime();
	}
	
	private void engageGate() {	
		unstableVortex = false;
		isEngaged = true;
		
		setRendererState();
		
		markDirty();
	}
	
	public void closeGate() {
		waitForClose = world.getTotalWorldTime();
		
		isClosing = true;
		isEngaged = false;
	}
	
	private void disconnectGate() {	
		isClosing = false;
		
		setRendererState();
		
		markDirty();
	}
	
	public boolean isEngaged() {
		return isEngaged;
	}
	
	public boolean isInitiating() {
		return isInitiating;
	}
	
	public void updateMergeState(boolean isMerged) {
		IBlockState state = world.getBlockState(pos);
		
		world.setBlockState( pos, state.withProperty(BlockTESRMember.RENDER, !isMerged) );
		MergeHelper.updateChevRingMergeState(this, state, isMerged);
	}

	private void setRendererState() {
		if (isEngaged)
			rendererState = new StargateRendererState(pos, dialedChevrons, true, getLimitedState().ringAngularRotation, true, EnumVortexState.STILL, true, true);
		else
			rendererState = new StargateRendererState(pos, 0, false, getLimitedState().ringAngularRotation, false, EnumVortexState.FORMING, false, false);
	}
	
	private LimitedStargateRendererState getLimitedState() {
		if (limitedState == null)
			limitedState = new LimitedStargateRendererState(pos);
		
		return limitedState;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Renderer getRenderer() {
		if (renderer == null)
			renderer = new StargateRenderer(this);
		
		return (StargateRenderer) renderer;
	}
	
	public StargateRendererState getStargateRendererState() {
		return (StargateRendererState) getRendererState();
	}
	
	@Override
	public RendererState getRendererState() {
		if (rendererState == null)
			setRendererState();
				
		return rendererState;
	}
	
	@Override
	public void setRendererState(RendererState rendererState) {
		if (rendererState instanceof LimitedStargateRendererState) {
			float ringAngularRotation = ((LimitedStargateRendererState) rendererState).ringAngularRotation;
			
			getStargateRendererState().ringAngularRotation = ringAngularRotation;
		}
	}
	
	public int getMaxSymbols() {
		return maxChevrons;
	}
	
	public int getEnteredSymbolsCount() {
		return dialedAddress.size();
	}
	
	public boolean checkForPointOfOrigin() {
		EnumSymbol last = dialedAddress.get( dialedAddress.size() - 1 );
		
		return last.equals( EnumSymbol.ORIGIN );
	}
	
	public BlockPos getLinkedDHD() {
		return linkedDHD;
	}
	
	public DHDTile getLinkedDHD(World world) {
		return (DHDTile) world.getTileEntity(linkedDHD);
	}
	
	public boolean isLinked() {
		return linkedDHD != null;
	}
	
	public void setLinkedDHD(BlockPos dhdPos) {
		this.linkedDHD = dhdPos;
		
		markDirty();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		BlockPos dhd;
		
		if (linkedDHD == null)
			dhd = new BlockPos(0,0,0);
		else
			dhd = linkedDHD;
		
		compound.setLong("linkedDHD", dhd.toLong());
				
		if (gateAddress != null) {
			for (int i=0; i<maxChevrons-1; i++) {
				compound.setInteger("symbol"+i, gateAddress.get(i).id);
			}
		}
		
		compound.setBoolean("isEngaged", isEngaged);
		compound.setBoolean("isInitiating", isInitiating);
		compound.setBoolean("hasUpgrade", hasUpgrade);
		compound.setBoolean("insertAnimation", insertAnimation);
		
		if (isEngaged && isInitiating) {
			compound.setInteger("dialedAddressLength", dialedAddress.size());
			
			for (int i=0; i<dialedAddress.size(); i++) {
				compound.setInteger("dialedSymbol"+i, dialedAddress.get(i).id);
			}
		}
		
		compound.setBoolean("unstableVortex", unstableVortex);
		compound.setLong("waitForEngage", waitForEngage);
		compound.setBoolean("isClosing", isClosing);
		compound.setLong("waitForClose", waitForClose);
		compound.setBoolean("clearingButtons", clearingButtons);
		compound.setLong("waitForClear", waitForClear);
		
		compound.setInteger("dialedChevrons", dialedChevrons);
		
		getLimitedState().toNBT(compound);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.linkedDHD = BlockPos.fromLong( compound.getLong("linkedDHD") );
		
		boolean compoundHasAddress = compound.hasKey("symbol0");
		
		if (compoundHasAddress) {		
			gateAddress = new ArrayList<EnumSymbol>();
			
			for (int i=0; i<maxChevrons-1; i++) {
				int id = compound.getInteger("symbol"+i);
				gateAddress.add( EnumSymbol.valueOf(id) );
			}
		}
		
		isEngaged = compound.getBoolean("isEngaged");
		isInitiating = compound.getBoolean("isInitiating");
		hasUpgrade = compound.getBoolean("hasUpgrade");
		insertAnimation = compound.getBoolean("insertAnimation");

		if (isEngaged && isInitiating) {
			dialedAddress.clear();
			int dialedAddressLength = compound.getInteger("dialedAddressLength");
			
			for (int i=0; i<dialedAddressLength; i++) {
				dialedAddress.add( EnumSymbol.valueOf(compound.getInteger("dialedSymbol"+i)) );
			}
		}
		
		unstableVortex = compound.getBoolean("unstableVortex");
		waitForEngage = compound.getLong("waitForEngage");
		isClosing = compound.getBoolean("isClosing");
		waitForClose = compound.getLong("waitForClose");
		clearingButtons = compound.getBoolean("clearingButtons");
		waitForClear = compound.getLong("waitForClear");
		
		dialedChevrons = compound.getInteger("dialedChevrons");
		
		limitedState = new LimitedStargateRendererState(compound);
		
		super.readFromNBT(compound);
	}
	
	private boolean firstTick = true;
	
	// Where to place event horizon
	private final float placement = 0.5f;
	
	// How wide it should be
	private final float delta = 0.2f;
	
	// Other dimensions
	private final float left = -2;
	private final float right = 3;
	private final float up = 8;
	
	// Calculated box
	public AxisAlignedBB horizonBoundingBox;
		
	public static class TeleportPacket {
		private BlockPos sourceGatePos;
		private BlockPos targetGatePos;
		
		private int rotation;
		private String sourceAxisName;
		
		private Vector2f motionVector;
		private WorldProvider targetWorldProvider;
		
		public TeleportPacket(BlockPos source, BlockPos target, int rotation, EnumFacing.Axis sourceAxis, World targetWorld) {
			sourceGatePos = source;
			targetGatePos = target;
			
			this.rotation = rotation;
			sourceAxisName = sourceAxis.getName();
			targetWorldProvider = targetWorld.provider;
		}
		
		public void teleport(Entity entity, World sourceWorld) {
			TeleportHelper.teleportServer(entity, sourceGatePos, targetGatePos, rotation, sourceAxisName, motionVector, targetWorldProvider.getDimension(), (float) (sourceWorld.provider.getMovementFactor()/targetWorldProvider.getMovementFactor()));
			
			if (entity instanceof EntityPlayerMP)
				entity.getEntityWorld().playSound(null, targetGatePos, AunisSoundEvents.wormholeGo, SoundCategory.BLOCKS, 1.0f, 1.0f);
		}

		public TeleportPacket setMotion(Vector2f motion) {
			this.motionVector = motion;
			
			return this;
		}
	}
	
	public Map<Integer, TeleportPacket> scheduledTeleportMap = new HashMap<Integer, TeleportPacket>();
	
	public void teleportEntity(int entityId) {
		Entity entity = world.getEntityByID(entityId);
		scheduledTeleportMap.get(entityId).teleport(entity, world);
		
		removeEntityFromTeleportList(entityId);
	}
	
	public void removeEntityFromTeleportList(int entityId) {		
		scheduledTeleportMap.remove(entityId);
	}
	
	private boolean clearingButtons;
	private long waitForClear;
	private int clearDelay;
	
	public void clearLinkedDHDButtons(boolean dialingFailed) {
		clearDelay = dialingFailed ? 29 : 52;
		
		waitForClear = world.getTotalWorldTime();
		clearingButtons = true;
	}
	
	@Override
	public void update() {
		if (firstTick) {
			firstTick = false;
			
			// Client loaded region, need to update
			// Send rendererState to client's renderer
			if (world.isRemote)
				AunisPacketHandler.INSTANCE.sendToServer( new TileUpdateRequestToServer(pos) );
			
			// Can't do this in onLoad(), because in that method, world isn't fully loaded
			generateAddress();
			
			updateMergeState(MergeHelper.checkBlocks(this));
			
			if (!world.isRemote) {
				EnumFacing sourceGateFacing = world.getBlockState(pos).getValue(BlockFaced.FACING);
				
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				
				if (sourceGateFacing.getAxis().getName() == "z") {
					// North - South
					// Stargate divided on X axis
					
					float z1 = placement - delta;
					float z2 = placement + delta;
					
					horizonBoundingBox = new AxisAlignedBB(x+left, y+1, z+z1,  x+right, y+up, z+z2);
				}
					
				else {
					// West - East
					//  Stargate divided on Z axis
						
					float x1 = placement - delta;
					float x2 = placement + delta;
					
					horizonBoundingBox = new AxisAlignedBB(x+x1, y+1, z+left,  x+x2, y+up, z+right);
				}
			}
		}
		
		if (!world.isRemote && horizonBoundingBox != null && isEngaged && isInitiating) {
			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, horizonBoundingBox);
			
			for (Entity entity : entities) {
				int entId = entity.getEntityId();
				
				if ( !scheduledTeleportMap.containsKey(entId) ) {
					StargatePos targetGate = StargateNetwork.get(world).getStargate( dialedAddress );
					World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
					
					BlockPos targetPos = targetGate.getPos();
					// StargateBaseTile targetTile = (StargateBaseTile) targetWorld.getTileEntity(targetPos);
					
					EnumFacing sourceFacing = world.getBlockState(pos).getValue(BlockFaced.FACING);
					EnumFacing targetFacing = targetWorld.getBlockState(targetPos).getValue(BlockFaced.FACING);
					
					int rotation = (int) (sourceFacing.getHorizontalAngle() - targetFacing.getHorizontalAngle());
					//rotation = (float) Math.toRadians( /*.getOpposite()*/.getHorizontalAngle() );
					
					TeleportPacket packet = new TeleportPacket(pos, targetPos, rotation, sourceFacing.getAxis(), targetWorld);
					
					if (entity instanceof EntityPlayerMP) {
						scheduledTeleportMap.put(entId, packet);
						AunisPacketHandler.INSTANCE.sendTo(new RetrieveMotionToClient(pos), (EntityPlayerMP) entity);
					}
					
					else {
						Vector2f motion = new Vector2f( (float)entity.motionX, (float)entity.motionZ );
						
						if (TeleportHelper.frontSide(sourceFacing, motion)) {
							scheduledTeleportMap.put(entId, packet.setMotion(motion) );
							teleportEntity(entId);
						}
						
						/*else {
							// TODO Make custom message appear
							// entity.onKillCommand();
						}*/
					}
				}
			}
		}
		
		if (unstableVortex) {
			if (world.getTotalWorldTime()-waitForEngage >= 86 && !isClosing)
				engageGate();
		}
		
		if (isClosing) {
			if (world.getTotalWorldTime()-waitForClose >= 56) 
				disconnectGate();
		}
		
		if (clearingButtons) {
			if (world.getTotalWorldTime()-waitForClear >= clearDelay) { 
				if (linkedDHD != null)
					AunisPacketHandler.INSTANCE.sendToAllAround(new ClearLinkedDHDButtons(linkedDHD), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
				clearingButtons = false;
			}
		}
	}
	
	public void generateAddress() {
		
		// Server
		if ( !world.isRemote ) {			
			if ( gateAddress == null ) {
				Random rand = new Random();
				List<EnumSymbol> address = new ArrayList<EnumSymbol>(); 
					
				while (true) {
					while (address.size() < maxChevrons-1) {
						EnumSymbol symbol = EnumSymbol.valueOf( rand.nextInt(38) );
							
						if ( !address.contains(symbol) && symbol != EnumSymbol.ORIGIN ) {
							address.add(symbol);
						}
					}
					
					// Check if SOMEHOW Stargate with the same address doesn't exists
					if ( !StargateNetwork.get(world).checkForStargate(address) )
						break;
				}
							
				gateAddress = address;
				markDirty();
								
				// Add Stargate to the "network" - WorldSavedData
				StargateNetwork.get(world).addStargate(gateAddress, world.provider.getDimension(), pos);
				
				// Possibly TODO: Add region, so if we break the stargate and place it nearby, it keeps the address
			}			
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
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		if (oldState.getBlock() == newSate.getBlock())
			return oldState.withProperty(BlockTESRMember.RENDER, false) != newSate.withProperty(BlockTESRMember.RENDER, false);
		
		return true;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-6, 0, -6), getPos().add(7, 12, 7));
	}
}
