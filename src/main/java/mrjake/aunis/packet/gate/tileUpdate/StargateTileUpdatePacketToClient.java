package mrjake.aunis.packet.gate.tileUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.StargateRendererState;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StargateTileUpdatePacketToClient implements IMessage {
	public StargateTileUpdatePacketToClient() {}
	
	private StargateRendererState rendererState;
	private BlockPos linkedDHD;
	
	public StargateTileUpdatePacketToClient(StargateRendererState rendererState, BlockPos linkedDHD) {
		this.rendererState = rendererState;
		this.linkedDHD = linkedDHD;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		rendererState.toBytes(buf);
		
		if (linkedDHD == null)
			buf.writeLong( 0 );
		else
			buf.writeLong( linkedDHD.toLong() );		
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		rendererState = new StargateRendererState(buf);
		linkedDHD = BlockPos.fromLong( buf.readLong() );		
	}
	
	
	public static class StargateTileUpdateClientHandler implements IMessageHandler<StargateTileUpdatePacketToClient, IMessage> {

		@Override
		public IMessage onMessage(StargateTileUpdatePacketToClient message, MessageContext ctx) {
			
			Aunis.info("Received StargateTileUpdatePacketToClient: "+message.rendererState.toString());
			
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			World world = player.getEntityWorld();
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				
				StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.rendererState.pos);
				
				gateTile.getRenderer().setState(message.rendererState);
				gateTile.setLinkedDHD(message.linkedDHD);
				
			});
			
			return null;
		}
		
	}
	
}
