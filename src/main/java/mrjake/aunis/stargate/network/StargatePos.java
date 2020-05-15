package mrjake.aunis.stargate.network;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.stargate.teleportation.TeleportHelper;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class StargatePos implements INBTSerializable<NBTTagCompound> {
	
	public int dimensionID;
	public BlockPos gatePos;
	public SymbolTypeEnum symbolType;
	public List<SymbolInterface> additionalSymbols;
	
	public StargatePos(int dimensionID, BlockPos gatePos, StargateAddress stargateAddress) {
		this.dimensionID = dimensionID;
		this.gatePos = gatePos;
		
		this.symbolType = stargateAddress.getSymbolType();
		this.additionalSymbols = new ArrayList<>(2);
		this.additionalSymbols.addAll(stargateAddress.getAdditional());
	}
	
	public StargatePos(SymbolTypeEnum symbolType, NBTTagCompound compound) {
		this.symbolType = symbolType;
		this.additionalSymbols = new ArrayList<>(2);
		
		deserializeNBT(compound);
	}
	
	public World getWorld() {
		return TeleportHelper.getWorld(dimensionID);
	}
	
	public StargateAbstractBaseTile getTileEntity() {
		try {
			return (StargateAbstractBaseTile) getWorld().getTileEntity(gatePos);
		}
		
		catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public IBlockState getBlockState() {
		return getWorld().getBlockState(gatePos);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setInteger("dim", dimensionID);
		compound.setLong("pos", gatePos.toLong());
		compound.setInteger("last0", additionalSymbols.get(0).getId());
		compound.setInteger("last1", additionalSymbols.get(1).getId());
		
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		dimensionID = compound.getInteger("dim");
		gatePos = BlockPos.fromLong(compound.getLong("pos"));
		additionalSymbols.add(symbolType.valueOfSymbol(compound.getInteger("last0")));
		additionalSymbols.add(symbolType.valueOfSymbol(compound.getInteger("last1")));
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// Hashing
	
	@Override
	public String toString() {
		return String.format("[dim=%d, pos=%s, add=%s]", dimensionID, gatePos.toString(), additionalSymbols.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((additionalSymbols == null) ? 0 : additionalSymbols.hashCode());
		result = prime * result + dimensionID;
		result = prime * result + ((gatePos == null) ? 0 : gatePos.hashCode());
		result = prime * result + ((symbolType == null) ? 0 : symbolType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StargatePos other = (StargatePos) obj;
		if (additionalSymbols == null) {
			if (other.additionalSymbols != null)
				return false;
		} else if (!additionalSymbols.equals(other.additionalSymbols))
			return false;
		if (dimensionID != other.dimensionID)
			return false;
		if (gatePos == null) {
			if (other.gatePos != null)
				return false;
		} else if (!gatePos.equals(other.gatePos))
			return false;
		if (symbolType != other.symbolType)
			return false;
		return true;
	}
}
