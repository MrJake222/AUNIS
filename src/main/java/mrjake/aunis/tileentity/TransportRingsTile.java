package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.gui.RingsGUI;
import mrjake.aunis.gui.state.RingsGuiState;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.transportrings.StartPlayerFadeOutToClient;
import mrjake.aunis.packet.transportrings.StartRingsAnimationToClient;
import mrjake.aunis.packet.update.renderer.RendererUpdateRequestToServer;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.TransportRingsRendererState;
import mrjake.aunis.renderer.transportrings.TransportRingsRenderer;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.ITileEntityStateProvider;
import mrjake.aunis.state.State;
import mrjake.aunis.transportrings.TransportRings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TransportRingsTile extends TileEntity implements ITileEntityRendered, ITickable, ITileEntityStateProvider {

//	public TransportRingsTile() {
////		stateMap.put(EnumStateType.GUI_STATE, new RingsGuiState());
//	}
	
	// ---------------------------------------------------------------------------------
	// Ticking
	private boolean firstTick = true;
	private boolean waitForStart = false;
	private boolean waitForFadeOut = false;
	private boolean waitForTeleport = false;
	private boolean waitForClearout = false;
	
	private long buttonPressed;
	
//	private boolean doLightUpdate = false;
	
	private static final int fadeOutTimeout = (int) (30 + TransportRingsRenderer.uprisingInterval*TransportRingsRenderer.ringCount + TransportRingsRenderer.animationDiv * Math.PI);
	public static final int fadeOutTotalTime = 2 * 20; // 2s
	
	private static final int teleportTimeout = fadeOutTimeout + fadeOutTotalTime/2;
	private static final int clearoutTimeout = (int) (100 + TransportRingsRenderer.fallingInterval*TransportRingsRenderer.ringCount + TransportRingsRenderer.animationDiv * Math.PI);
	
//	private long tickStartFog;
	
	private List<Entity> teleportList;
	
	@Override
	public void update() {
		if (firstTick) {
			firstTick = false;
			
			if (world.isRemote) {
				AunisPacketHandler.INSTANCE.sendToServer(new RendererUpdateRequestToServer(pos));
			}
		}
		
		if (!world.isRemote) {
			long effTick = world.getTotalWorldTime();
			
			effTick -= waitForStart ? buttonPressed : getTransportRingsRendererState().animationStart;
			
			if (waitForStart && effTick >= 20) {
				waitForStart = false;
				waitForFadeOut = true;
				
				animationStart();
				setBarrierBlocks(true);		
			}
			
			else if (waitForFadeOut && effTick >= fadeOutTimeout) {
				waitForFadeOut = false;
				waitForTeleport = true;
				
//				doLightUpdate = true;
				
//				tickStartFog = world.getTotalWorldTime();
				
				teleportList = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.add(-2, 2, -2), pos.add(3, 6, 3)));
				
				for (Entity entity : teleportList) {
					if (entity instanceof EntityPlayerMP) {
						AunisPacketHandler.INSTANCE.sendTo(new StartPlayerFadeOutToClient(), (EntityPlayerMP) entity);
					}
				}
			}
				
				
			else if (waitForTeleport && effTick >= teleportTimeout) {
				waitForTeleport = false;
				waitForClearout = true;
				
				BlockPos teleportVector = targetRingsPos.subtract(pos);
				
				for (Entity entity : teleportList) {
					if (!excludedEntities.contains(entity)) {
						BlockPos ePos = entity.getPosition().add(teleportVector);		
						
						entity.setPositionAndUpdate(ePos.getX(), ePos.getY(), ePos.getZ());
					}
				}
			}
				
			else if (waitForClearout && effTick >= clearoutTimeout) {
				waitForClearout = false;

				setBarrierBlocks(false);
			}
			
			
//			// Setting light to blocks
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
	// Teleportation
	private BlockPos targetRingsPos;
	private List<Entity> excludedEntities;
	
	public List<Entity> startAnimationAndTeleport(BlockPos targetRingsPos, List<Entity> excludedEntities) {
		this.targetRingsPos = targetRingsPos;
		this.excludedEntities = excludedEntities;
		
		waitForStart = true;
		buttonPressed = world.getTotalWorldTime();
		
		return world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.add(-2, 2, -2), pos.add(3, 6, 3)));
	}
	
	public void animationStart() {
		getTransportRingsRendererState().animationStart = world.getTotalWorldTime();
		getTransportRingsRendererState().ringsUprising = true;
		getTransportRingsRendererState().isAnimationActive = true;
				
		TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		AunisPacketHandler.INSTANCE.sendToAllAround(new StartRingsAnimationToClient(pos, getTransportRingsRendererState().animationStart), point);
	}

	/**
	 * Checks if Rings are linked to Rings at given address.
	 * If yes, it starts teleportation.
	 * 
	 * @param address Target rings address
	 */
	public void attemptTransportTo(int address) {
		TransportRings rings = ringsMap.get(address);
				
		// Binding exists
		if (rings != null) {
			BlockPos targetRingsPos = rings.getPos();
			
			List<Entity> excludedFromReceivingSite = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.add(-2, 2, -2), pos.add(3, 6, 3)));
			
			TransportRingsTile targetRingsTile = (TransportRingsTile) world.getTileEntity(targetRingsPos);
			
			List<Entity> excludedEntities = targetRingsTile.startAnimationAndTeleport(pos, excludedFromReceivingSite);
			startAnimationAndTeleport(targetRingsPos, excludedEntities);
		}
		
		else {
			Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.transportrings_block.non_existing_address")), true);
		}
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
												
						world.setBlockState(newPos, AunisBlocks.invisibleBlock.getDefaultState(), 3);
						
//							if (y == 1)
						invisibleBlocks.add(newPos);
					}
				}
			}
		}
		
		else {
			for (BlockPos invPos : invisibleBlocks) {
//					world.setBlockState(invPos, Blocks.DIAMOND_BLOCK.getDefaultState(), 3);
				world.setBlockToAir(invPos);
			}
		}
//			if (set)world.setBlockState(pos.add(0,2,0), Blocks.GLASS.getDefaultState());
	}
	
	
	// ---------------------------------------------------------------------------------
	// Controller
	private boolean isLinked = false;
	
	public void setLinked(boolean linked) {
		isLinked = linked;
		
		markDirty();
	}
	
	public boolean isLinked() {
		return isLinked;
	}
	
	
	// ---------------------------------------------------------------------------------
	// Rings network
	private TransportRings rings;
	private TransportRings getRings() {
		if (rings == null)
			rings = new TransportRings(pos);
		
		return rings;
	}
	
	/**
	 * Gets clone of {@link TransportRingsTile#rings} object. Sets the distance from
	 * callerPos to this tile. Called from {@link TransportRingsTile#addRings(TransportRingsTile)}.
	 * 
	 * @param callerPos - calling tile position
	 * @return - clone of this rings info
	 */
	public TransportRings getClonedRings(BlockPos callerPos) {
		return getRings().cloneWithNewDistance(callerPos);
	}
	
	/**
	 * Contains neighborhooding rings(clones of {@link TransportRingsTile#rings}) with distance set to this tile
	 */
	public Map<Integer, TransportRings> ringsMap = new HashMap<>();
	
	/**
	 * Adds rings to {@link TransportRingsTile#ringsMap}, by cloning caller's {@link TransportRingsTile#rings} and
	 * setting distance
	 * 
	 * @param caller - Caller rings tile
	 */
	public void addRings(TransportRingsTile caller) {
		TransportRings clonedRings = caller.getClonedRings(this.pos);
		
		if (clonedRings.isInGrid()) {
			ringsMap.put(clonedRings.getAddress(), clonedRings);
			
			markDirty();
		}
	}
	
	public void removeRings(int address) {	
		if (ringsMap.remove(address) != null)
			markDirty();
	}
	
	public void removeAllRings() {
		for (TransportRings rings : ringsMap.values()) {
			
			TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(rings.getPos());
			ringsTile.removeRings(getRings().getAddress());
		}
	}
	
	public void setRingsParams(int address, String name) {
		removeAllRings();
		
		getRings().setAddress(address);
		getRings().setName(name);
		
		int x = pos.getX();
		int z = pos.getZ();
		
		int radius = AunisConfig.ringsConfig.rangeFlat;
		
		for (BlockPos newRings : BlockPos.getAllInBoxMutable(new BlockPos(x-radius, 0, z-radius), new BlockPos(x+radius, 255, z+radius))) {
			
			if (world.getBlockState(newRings).getBlock() == AunisBlocks.transportRingsBlock && !pos.equals(newRings)) {
				
				TransportRingsTile newRingsTile = (TransportRingsTile) world.getTileEntity(newRings);		
				
				this.addRings(newRingsTile);
				newRingsTile.addRings(this);
			}
		}
		
		markDirty();
	}
	
	
	// ---------------------------------------------------------------------------------
	// NBT data
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		getRendererState().toNBT(compound);
		
		compound.setTag("ringsData", getRings().serializeNBT());
		compound.setBoolean("isLinked", isLinked);
		
		compound.setInteger("ringsMapLength", ringsMap.size());
		
		int i = 0;
		for (TransportRings rings : ringsMap.values()) {
			compound.setTag("ringsMap" + i, rings.serializeNBT());
			
			i++;
		}
		
		
//		for (EnumStateType stateType : stateMap.keySet()) {
//			compound.setTag(stateType.getKey(), stateMap.get(stateType).serializeNBT());
//		}
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		getRendererState().fromNBT(compound);
		
		if (compound.hasKey("ringsData"))
			getRings().deserializeNBT((NBTTagCompound) compound.getTag("ringsData"));
		
		isLinked = compound.getBoolean("isLinked");
		
		if (compound.hasKey("ringsMapLength")) {
			int len = compound.getInteger("ringsMapLength");
			
			ringsMap.clear();
			
			for (int i=0; i<len; i++) {
				TransportRings rings = new TransportRings(null).deserializeNBT((NBTTagCompound) compound.getTag("ringsMap" + i));
				
				ringsMap.put(rings.getAddress(), rings);
			}
		}
		
//		for (EnumStateType stateType : stateMap.keySet()) {
//			State state = stateMap.get(stateType);
//			
//			state.deserializeNBT((NBTTagCompound) compound.getTag(stateType.getKey()));
//			
//			stateMap.put(stateType, state);
//		}
		
		super.readFromNBT(compound);
	}
	
	
	// ---------------------------------------------------------------------------------	
	// States
//	private Map<EnumStateType, State> stateMap = new HashMap<>();

	@Override
	public State getState(EnumStateType stateType) {
		switch (stateType) {
			case GUI_STATE:
				return new RingsGuiState(getRings(), ringsMap.values());
				
			default:
				return null;
		}
	}
	
	@Override
	public State createState(EnumStateType stateType) {
		switch (stateType) {
			case GUI_STATE:
				return new RingsGuiState();
				
			default:
				return null;
		}
	}
	
	@SideOnly(Side.CLIENT)
	private RingsGUI openGui;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(EnumStateType stateType, State state) {
//		Mouse.setGrabbed(false);
		
//		Aunis.guiProxy.openGui("ringsGui", stateType, state);
		
//		RingsGUI.class.newInstance();
		
		switch (stateType) {
			case GUI_STATE:
				
				if (openGui == null || !openGui.isOpen) {
					openGui = new RingsGUI(pos, (RingsGuiState) state);
					Minecraft.getMinecraft().displayGuiScreen(openGui);
				}
				
				else {
					openGui.state = (RingsGuiState) state;
				}
				
				break;
				
			default:
				break;
		}
	}
	
	// ---------------------------------------------------------------------------------
	// Renders
	// TODO: To be removed and replaced by States
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
