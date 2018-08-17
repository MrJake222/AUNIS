package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.addressUpdate.GateAddressRequestToServer;
import mrjake.aunis.renderer.StargateRenderer;
import mrjake.aunis.stargate.EnumSymbol;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateBaseTile extends TileEntity {
	
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
		
		compound.setInteger("linkedDHDX", dhd.getX());
		compound.setInteger("linkedDHDY", dhd.getY());
		compound.setInteger("linkedDHDZ", dhd.getZ());
		
		if (gateAddress != null) {
			for (int i=0; i<maxChevrons-1; i++) {
				compound.setInteger( "symbol"+i, gateAddress.get(i).id );
			}
		}
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {				
		int x = compound.getInteger("linkedDHDX");
		int y = compound.getInteger("linkedDHDY");
		int z = compound.getInteger("linkedDHDZ");
		
		BlockPos pos = new BlockPos(x,y,z);

		Aunis.log("Relinking to DHD at " + pos.toString());
		linkedDHD = pos;
		
		// Aunis.info("Reading from NBT... side:"+FMLCommonHandler.instance.getEffectiveSide());
		
		//Aunis.proxy.generateAddress(this, compound);
		
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
	
	@Override
	public void onLoad() {
		// Server
		if ( !world.isRemote && gateAddress == null ) {
			Random rand = new Random();
			List<EnumSymbol> address = new ArrayList<EnumSymbol>(); 
				
			// for (int i=0; i<maxChevrons; i++) {
			while (address.size() < maxChevrons-1) {
				EnumSymbol symbol = EnumSymbol.valueOf( rand.nextInt(38) );
					
				if ( !address.contains(symbol) ) {
					address.add(symbol);
				}
			}
						
			gateAddress = address;
			markDirty();
			
			Aunis.info("Generated address "+address.toString());
		}
		
		super.onLoad();
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-6, 0, -6), getPos().add(7, 12, 7));
	}
}
