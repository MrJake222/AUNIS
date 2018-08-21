package mrjake.aunis.packet.gate.tileUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TileUpdatePacketToClient implements IMessage {
	public TileUpdatePacketToClient() {}
	
	private BlockPos tilePos;
	private BlockPos linkedDHD;
	
	public TileUpdatePacketToClient(BlockPos tilePos, BlockPos linkedDHD) {
		this.tilePos = tilePos;
		this.linkedDHD = linkedDHD;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( tilePos.toLong() );
		buf.writeLong( linkedDHD.toLong() );		
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tilePos = BlockPos.fromLong( buf.readLong() );
		linkedDHD = BlockPos.fromLong( buf.readLong() );		
	}
	
	
	public static class TileUpdateClientHandler implements IMessageHandler<TileUpdatePacketToClient, IMessage> {

		@Override
		public IMessage onMessage(TileUpdatePacketToClient message, MessageContext ctx) {
			
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			World world = player.getEntityWorld();
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				
				Aunis.info("Updating "+message.tilePos+" with dhd="+message.linkedDHD);
				
				StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.tilePos);
				gateTile.setLinkedDHD(message.linkedDHD);
				
			});
			
			return null;
		}
		
	}
	
}
