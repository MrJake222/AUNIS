package mrjake.aunis.chunk;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.Aunis;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class AunisChunkLoader {
	private static Map<Integer, Ticket> chunkLoaderTicketMap = new HashMap<Integer, Ticket>();
	
	public static void genTicketForWorld(World world) {		
		chunkLoaderTicketMap.put( world.provider.getDimension(), ForgeChunkManager.requestTicket(Aunis.instance, world, Type.NORMAL) );
	}
	
	public static void removeTicketForWorld(World world) {
		chunkLoaderTicketMap.remove( world.provider.getDimension() );
	}
	
	public static void forceChunk(World world, ChunkPos pos) {
		Ticket ticket = chunkLoaderTicketMap.get( world.provider.getDimension() );
		
		if (ticket == null)
			genTicketForWorld(world);
		
		ForgeChunkManager.forceChunk(ticket, pos);
	}
	
	public static void unforceChunk(World world, ChunkPos pos) {
		Ticket ticket = chunkLoaderTicketMap.get( world.provider.getDimension() );
		
		ForgeChunkManager.unforceChunk(ticket, pos);
	}
}
