package mrjake.aunis.datafixer;

import java.util.Random;

import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.network.StargatePos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class StargateNetworkReader18 {
	
	public static void readOldMap(NBTTagCompound compound, StargateNetwork network) {
		int size = compound.getInteger("size");
		
		for (int i=0; i<size; i++) {
			BlockPos pos = BlockPos.fromLong(compound.getLong("pos"+i));
			int dim = compound.getInteger("dim"+i);
			
			Random random = new Random(pos.hashCode() * 31 + dim);
			StargateAddress address = new StargateAddress(SymbolTypeEnum.MILKYWAY);
			address.generate(random);
			StargatePos stargatePos = new StargatePos(dim, pos, address);
			
			Aunis.info("Adding old gate: " + address);
			network.addStargate(address, stargatePos);
		}
	}
}
