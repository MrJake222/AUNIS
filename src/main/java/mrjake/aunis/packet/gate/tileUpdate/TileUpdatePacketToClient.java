package mrjake.aunis.packet.gate.tileUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.StargateRendererState;
import mrjake.aunis.tileentity.TileEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TileUpdatePacketToClient implements IMessage {
	public TileUpdatePacketToClient() {}
	
	private RendererState rendererState;
	
	public TileUpdatePacketToClient(RendererState rendererState) {
		this.rendererState = rendererState;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {		
		buf.writeInt( EnumTile.fromObject(rendererState) );		
		rendererState.toBytes(buf);	
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		EnumTile tile = EnumTile.fromInt(buf.readInt());
		
		if (tile == EnumTile.GATE_TILE)
			rendererState = new StargateRendererState(buf);
		else if (tile == EnumTile.DHD_TILE)
			rendererState = new DHDRendererState(buf);
	}
	
	public static class TileUpdateClientHandler implements IMessageHandler<TileUpdatePacketToClient, IMessage> {

		@SuppressWarnings("unchecked")
		@Override
		public IMessage onMessage(TileUpdatePacketToClient message, MessageContext ctx) {			
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			World world = player.getEntityWorld();
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				
				TileEntityRenderer te = (TileEntityRenderer) world.getTileEntity(message.rendererState.pos);
				te.getRenderer().setState(message.rendererState);
				
			});
			
			return null;
		}
		
	}
}
