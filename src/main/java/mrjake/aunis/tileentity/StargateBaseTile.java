package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.vecmath.Vector2f;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisSoundEvents;
import mrjake.aunis.block.BlockFaced;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.addressUpdate.GateAddressRequestToServer;
import mrjake.aunis.packet.gate.teleportPlayer.PlayWormholeSoundPacketToClient;
import mrjake.aunis.packet.gate.teleportPlayer.RetrieveMotionToClient;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdatePacketToClient;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdateRequestToServer;
import mrjake.aunis.renderer.StargateRenderer;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.TeleportHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class StargateBaseTile extends TileEntity implements ITickable {
	
	private static final int maxChevrons = 7;
	
	private StargateRenderer renderer;
	private BlockPos linkedDHD = null;
	
	private boolean isEngaged;
	private boolean isInitiating;
	
	private long waitForEngage;
	private boolean unstableVortex;
	
	public List<EnumSymbol> gateAddress;
	public List<EnumSymbol> dialedAddress = new ArrayList<EnumSymbol>();
	
	public boolean addSymbolToAddress(EnumSymbol symbol) {		
		if (dialedAddress.contains(symbol)) 
			return false;
		
		if (dialedAddress.size() == maxChevrons)
			return false;
		
		dialedAddress.add(symbol);
		return true;
	}
	
	public void clearAddress() {
		dialedAddress.clear();
	}
	
	public void openGate(boolean initiating) {
		isInitiating = initiating;
		
		unstableVortex = true;
		waitForEngage = world.getTotalWorldTime();
	}
	
	public void engageGate() {
		Aunis.log("Initiating connection with "+dialedAddress.toString());
		
		unstableVortex = false;
		isEngaged = true;
	}
	
	public void disconnectGate() {
		Aunis.log("Disconnecting gate");
		isEngaged = false;
	}
	
	public boolean isEngaged() {
		return isEngaged;
	}

	public StargateRenderer getRenderer() {
		if (renderer == null)
			renderer = new StargateRenderer(this);
		
		return renderer;
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
		
		if (!world.isRemote) {
			TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64);
			
			AunisPacketHandler.INSTANCE.sendToAllAround(new TileUpdatePacketToClient(pos, linkedDHD), point);
		}
		
		markDirty();
	}

	public List<EnumSymbol> getAddress() {
		
		// Client and address null 
		if (world.isRemote && gateAddress == null) {
			AunisPacketHandler.INSTANCE.sendToServer( new GateAddressRequestToServer(pos) );
			
			return null;
		}
		
		else return gateAddress;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		BlockPos dhd;
		
		if (linkedDHD == null) {
			Aunis.log(pos.toString()+":  linkedDHD is null!");
			dhd = new BlockPos(0,0,0);
		}
		else
			dhd = linkedDHD;
		
		compound.setLong("linkedDHD", dhd.toLong());
		
		if (gateAddress != null) {
			for (int i=0; i<maxChevrons-1; i++) {
				compound.setInteger( "symbol"+i, gateAddress.get(i).id );
			}
		}
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {				
		BlockPos pos = BlockPos.fromLong( compound.getLong("linkedDHD") );

		Aunis.log(pos.toString()+": Relinking to DHD at " + pos.toString());
		linkedDHD = pos;
		
		boolean compoundHasAddress = compound.hasKey("symbol0");

		if (compoundHasAddress) {		
			gateAddress = new ArrayList<EnumSymbol>();
			
			for (int i=0; i<maxChevrons-1; i++) {
				int id = compound.getInteger("symbol"+i);
				gateAddress.add( EnumSymbol.valueOf(id) );
			}
			
			Aunis.log("Read address: "+gateAddress.toString());
		}
		
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
	
	// private int tickWait = 0;
	
	public static class TeleportPacket {
		private BlockPos sourceGatePos;
		private BlockPos targetGatePos;
		
		private float rotation;
		private String sourceAxisName;
		
		public Vector2f motionVector;
		/*public float oldMotionX;
		public float oldMotionZ;*/
		
		public TeleportPacket(BlockPos source, BlockPos target, float rot, EnumFacing.Axis sourceAxis) {
			sourceGatePos = source;
			targetGatePos = target;
			
			rotation = rot;
			sourceAxisName = sourceAxis.getName();
		}
		
		public void teleport(EntityPlayerMP player) {
			TeleportHelper.teleportServer(player, sourceGatePos, targetGatePos, rotation, sourceAxisName, motionVector);
			
			player.getEntityWorld().playSound(player, targetGatePos, AunisSoundEvents.wormholeGo, SoundCategory.BLOCKS, 1.0f, 1.0f);
			
			AunisPacketHandler.INSTANCE.sendTo(new PlayWormholeSoundPacketToClient(targetGatePos), player);
		}
	}
	
	// Map<PlayerID, TeleportPacket>
	public Map<Integer, TeleportPacket> scheduledTeleportMap = new HashMap<Integer, TeleportPacket>();
	
	public void teleportPlayer(int entityId) {
		EntityPlayerMP player = (EntityPlayerMP) world.getEntityByID(entityId);
		scheduledTeleportMap.get(entityId).teleport(player);
		
		scheduledTeleportMap.remove(entityId);
	}
	
	@Override
	public void update() {
		if (firstTick) {
			firstTick = false;
			
			// Sync linkedDHD to client for use in renderer
			if (world.isRemote)
				AunisPacketHandler.INSTANCE.sendToServer( new TileUpdateRequestToServer(pos) );
			
			// Can't do this in onLoad(), because in that method, world isn't fully loaded
			generateAddress();
			
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
				
				// Aunis.info(pos.toString()+": horizonBoundingBox: "+horizonBoundingBox.toString());
			}
		}
		
		if (!world.isRemote && horizonBoundingBox != null /*&& pos.equals( new BlockPos(-117, 69, 165) )*/ && isEngaged && isInitiating) {
			List<EntityPlayerMP> players = world.getEntitiesWithinAABB(EntityPlayerMP.class, horizonBoundingBox);
			
			for (EntityPlayerMP player : players) {
				int entId = player.getEntityId();
				
				if ( !scheduledTeleportMap.containsKey(entId) ) {
					BlockPos targetPos = StargateNetwork.get(world).getStargate(dialedAddress);
					
					EnumFacing sourceFacing = world.getBlockState(pos).getValue(BlockFaced.FACING);
					EnumFacing targetFacing = world.getBlockState(targetPos).getValue(BlockFaced.FACING);
					
					float rotation = (float) Math.toRadians( sourceFacing.getHorizontalAngle() - targetFacing.getHorizontalAngle() );
					
					float axisDiff = 0;
					
					if (sourceFacing.getAxis() == Axis.X)
						axisDiff = (float) (pos.getX()+0.5 - player.posX);
					else
						axisDiff = (float) (pos.getZ()+0.5 - player.posZ);
					
					// Block is oriented to positive numbers and player is standing in front of it
					// player pos is greater, so diff is negative
					
					AxisDirection direction = sourceFacing.getAxisDirection();
					
					// Player entered front side of event horizon
					if ( (direction == AxisDirection.POSITIVE && axisDiff < 0) || (direction == AxisDirection.NEGATIVE && axisDiff > 0) ) {		
						// Aunis.info("Front");
						
						scheduledTeleportMap.put( entId, new TeleportPacket(pos, targetPos, rotation, sourceFacing.getAxis()) );
						AunisPacketHandler.INSTANCE.sendTo(new RetrieveMotionToClient(pos), player);
						
						world.playSound(player, pos, AunisSoundEvents.wormholeGo, SoundCategory.BLOCKS, 1.0f, 1.0f);
					}
					
					else {
						Aunis.info("Back side");
					}
				}
			}
			
			//tickWait = 0;
		}
		
		if (unstableVortex && world.getTotalWorldTime()-waitForEngage >= 86) {
			engageGate();
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
				
				Aunis.log("Adding new gate, Generated address "+address.toString());
				
				// Add Stargate to the "network" - WorldSavedData
				StargateNetwork.get(world).addStargate(gateAddress, pos);
				
				// Possibly TODO: Add region, so if we break the stargate and place it nearby, it keeps the address
			}			
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-6, 0, -6), getPos().add(7, 12, 7));
	}
}
