package mrjake.aunis.packet.gate.renderingUpdate;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DHDIncomingWormholePacketToClient implements IMessage {
	public DHDIncomingWormholePacketToClient() {}
	
	private BlockPos dhdPos;
	private List<Integer> dialedAddress;
	
	public DHDIncomingWormholePacketToClient(BlockPos dhdPos, List<EnumSymbol> dialedAddress) {		
		this.dhdPos = dhdPos;
		this.dialedAddress = new ArrayList<Integer>();
		
		for (EnumSymbol symbol : dialedAddress) {
			this.dialedAddress.add( symbol.id );
		}
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( dhdPos.toLong() );
		buf.writeInt( dialedAddress.size() );
		
		for (int id : dialedAddress) {
			buf.writeInt(id);
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		dhdPos = BlockPos.fromLong( buf.readLong() );
		
		int len = buf.readInt();
		dialedAddress = new ArrayList<Integer>();
		
		for (int i=0; i<len; i++) {
			dialedAddress.add( buf.readInt() );
		}
	}

	public static class DHDIncomingWormholePacketToClientHandler implements IMessageHandler<DHDIncomingWormholePacketToClient, IMessage> {

		@Override
		public IMessage onMessage(DHDIncomingWormholePacketToClient message, MessageContext ctx) {
			World world = Minecraft.getMinecraft().world;
			
			Aunis.log("Received DHDIncomingWormholePacketToClient:  dhdPos: " + message.dhdPos.toString() + ",  dialedAddress: " + message.dialedAddress.toString());
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				DHDTile te = (DHDTile) world.getTileEntity( message.dhdPos );
				
				te.getRenderer().smoothlyActivateButtons(message.dialedAddress);
			});
			
			return null;
		}
		
	}
	
}
