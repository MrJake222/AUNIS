package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.StargateRenderer;
import mrjake.aunis.stargate.EnumSymbol;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateBaseTile extends TileEntity {
	
	private StargateRenderer renderer;
	private BlockPos linkedDHD = null;
	
	private static final int maxChevrons = 7;
	private boolean isEngaged;
	
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

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		BlockPos dhd;
		
		if (linkedDHD == null) {
			Aunis.log("linkedDHD is null!");
			dhd = new BlockPos(0,0,0);
		}
		else
			dhd = linkedDHD;
		
		compound.setInteger("linkedGateX", dhd.getX());
		compound.setInteger("linkedGateY", dhd.getY());
		compound.setInteger("linkedGateZ", dhd.getZ());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {		
		super.readFromNBT(compound);
		
		int x = compound.getInteger("linkedGateX");
		int y = compound.getInteger("linkedGateY");
		int z = compound.getInteger("linkedGateZ");
		
		BlockPos pos = new BlockPos(x,y,z);

		Aunis.log("Relinking to Stargate at " + pos.toString());
		linkedDHD = pos;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-6, 0, -6), getPos().add(7, 12, 7));
	}
}
