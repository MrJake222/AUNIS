package mrjake.aunis.chunkloader;

import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.util.Constants.NBT;

public class ChunkLoadingCallback implements LoadingCallback {
	
	public static final ChunkLoadingCallback INSTANCE = new ChunkLoadingCallback();
	
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {		
		for (Ticket ticket : tickets) {
			NBTTagList forcedChunks = ticket.getModData().getTagList("forcedChunks", NBT.TAG_COMPOUND);
			
			for (NBTBase forcedChunk : forcedChunks) {
				ChunkPos chunk = ChunkManager.deserializeChunk((NBTTagCompound) forcedChunk);
				ChunkManager.forceChunk(world, chunk);
			}
			
			ForgeChunkManager.releaseTicket(ticket);
		}
	}
};
