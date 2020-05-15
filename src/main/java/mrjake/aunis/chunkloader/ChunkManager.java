package mrjake.aunis.chunkloader;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.Aunis;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class ChunkManager {
	
	private static Map<World, Ticket> worldTicketMap = new HashMap<>();
	
	public static Ticket requestTicket(World world) {
		if (!worldTicketMap.containsKey(world)) {
//			Aunis.info("Creating ticket for "+world.provider);
			worldTicketMap.put(world, ForgeChunkManager.requestTicket(Aunis.instance, world, Type.NORMAL));
		}
		
		return worldTicketMap.get(world);
	}
	
	public static void forceChunk(World world, ChunkPos chunk) {
		Aunis.info("Forcing chunk " + chunk + ", in world: " + world.provider);
		
		Ticket ticket = requestTicket(world);
		ForgeChunkManager.forceChunk(ticket, chunk);
		
		NBTTagList forcedChunks = ticket.getModData().getTagList("forcedChunks", NBT.TAG_COMPOUND);
		forcedChunks.appendTag(serializeChunk(chunk));
		ticket.getModData().setTag("forcedChunks", forcedChunks);
	}
	
	public static void unforceChunk(World world, ChunkPos chunk) {
		Ticket ticket = requestTicket(world);
		ForgeChunkManager.unforceChunk(ticket, chunk);
		
		NBTTagList forcedChunks = ticket.getModData().getTagList("forcedChunks", NBT.TAG_COMPOUND);
		int found = -1;
		
		for (int i=0; i<forcedChunks.tagCount(); i++) {
			if (serializedChunkEquals(chunk, forcedChunks.getCompoundTagAt(i))) {
				found = i;
				break;
			}
		}
	
		if (found != -1) {
			forcedChunks.removeTag(found);
			ticket.getModData().setTag("forcedChunks", forcedChunks);
		}
	}
	
	private static NBTTagCompound serializeChunk(ChunkPos chunk) {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("x", chunk.x);
		compound.setInteger("z", chunk.z);
		
		return compound;
	}
	
	public static ChunkPos deserializeChunk(NBTTagCompound compound) {
		return new ChunkPos(compound.getInteger("x"), compound.getInteger("z"));
	}
	
	private static boolean serializedChunkEquals(ChunkPos chunk, NBTTagCompound compound) {
		return compound.getInteger("x") == chunk.x && compound.getInteger("z") == chunk.z;
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		worldTicketMap.remove(event.getWorld());
	}
}
