package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.addressUpdate.GateAddressRequestToServer;
import mrjake.aunis.renderer.StargateRenderer;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateBaseTile extends TileEntity implements ITickable {
	
	private static final int maxChevrons = 7;
	
	private StargateRenderer renderer;
	private BlockPos linkedDHD = null;
	
	private boolean isEngaged;
	
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
	
	public void engageGate() {
		Aunis.log("Initiating connection with "+dialedAddress.toString());
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
			Aunis.log("linkedDHD is null!");
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

		Aunis.log("Relinking to DHD at " + pos.toString());
		linkedDHD = pos;
		
		boolean compoundHasAddress = compound.hasKey("symbol0");

		if (compoundHasAddress) {		
			gateAddress = new ArrayList<EnumSymbol>();
			
			for (int i=0; i<maxChevrons-1; i++) {
				int id = compound.getInteger("symbol"+i);
				gateAddress.add( EnumSymbol.valueOf(id) );
			}
			
			Aunis.info("Read address: "+gateAddress.toString());
		}
		
		super.readFromNBT(compound);
	}
	
	private boolean firstTick = true;
	
	@Override
	public void update() {
		
		// Can't do this in onLoad(), because in that method, world isn't fully loaded
		if (firstTick) {
			firstTick = false;
			
			onLoaded();
		}
		
	}
	
	public void onLoaded() {
		
		// Server
		if ( !world.isRemote ) {			
			if ( gateAddress == null ) {
				Random rand = new Random();
				List<EnumSymbol> address = new ArrayList<EnumSymbol>(); 
					
				while (true) {
					while (address.size() < maxChevrons-1) {
						EnumSymbol symbol = EnumSymbol.valueOf( rand.nextInt(38) );
							
						if ( !address.contains(symbol) ) {
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
