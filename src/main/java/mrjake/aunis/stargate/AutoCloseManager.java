package mrjake.aunis.stargate;

import mrjake.aunis.AunisConfig;
import mrjake.aunis.packet.stargate.StargateRenderingUpdatePacketToServer;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class AutoCloseManager implements INBTSerializable<NBTTagCompound> {
	
	private StargateAbstractBaseTile gateTile;
	
	private int secondsPassed;
	private int playersPassed;
	
	public AutoCloseManager(StargateAbstractBaseTile gateTile) {
		this.gateTile = gateTile;
	}
	
	public void reset() {
		secondsPassed = 0;
		playersPassed = 0;
	}
	
	public void playerPassing() {
		playersPassed++;
	}
	
	/**
	 * AutoClose update function (on server) (engaged) (receiving gate).
	 * Scan for load status of the source gate every 20 ticks (1 second).
	 */
	public void update(StargatePos sourceStargatePos) {
		if (gateTile.getWorld().getTotalWorldTime() % 20 == 0) {			
			World sourceWorld = sourceStargatePos.getWorld();
			BlockPos sourcePos = sourceStargatePos.getPos();
			
			boolean sourceLoaded = sourceWorld.isBlockLoaded(sourcePos);
			
			if (playersPassed > 0) {
				if (sourceLoaded) {
					
					AxisAlignedBB scanBox = new AxisAlignedBB(sourcePos.add(new Vec3i(-10, -5, -10)), sourcePos.add(new Vec3i(10, 5, 10)));
					int playerCount = sourceWorld.getEntitiesWithinAABB(EntityPlayerMP.class, scanBox, player -> !player.isDead).size();
					
					if (playerCount == 0)
						secondsPassed++;
					else
						secondsPassed = 0;
				}
				
				else {
					secondsPassed++;
				}
			}
								
			if (secondsPassed >= AunisConfig.autoCloseConfig.secondsToAutoclose) {
				StargateRenderingUpdatePacketToServer.closeGatePacket(gateTile, false);
			}
		}			
	}

	
	// ------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setInteger("secondsPassed", secondsPassed);
		compound.setInteger("playersPassed", playersPassed);
		
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		if (compound == null)
			return;
		
		secondsPassed = compound.getInteger("secondsPassed");
		playersPassed = compound.getInteger("playersPassed");
	}
}
