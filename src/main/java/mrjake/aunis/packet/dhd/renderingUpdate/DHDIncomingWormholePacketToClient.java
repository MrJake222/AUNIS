package mrjake.aunis.packet.dhd.renderingUpdate;

import java.util.List;

import io.netty.buffer.ByteBuf;
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
	private long dialedAddress;
	
	public DHDIncomingWormholePacketToClient(BlockPos dhdPos, List<EnumSymbol> dialedAddress) {		
		this.dhdPos = dhdPos;
		this.dialedAddress = EnumSymbol.toLong(dialedAddress);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( dhdPos.toLong() );
		buf.writeLong( dialedAddress );
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		dhdPos = BlockPos.fromLong( buf.readLong() );
		dialedAddress = buf.readLong();
	}

	public static class DHDIncomingWormholePacketToClientHandler implements IMessageHandler<DHDIncomingWormholePacketToClient, IMessage> {

		@Override
		public IMessage onMessage(DHDIncomingWormholePacketToClient message, MessageContext ctx) {
			World world = Minecraft.getMinecraft().world;
			
			// Aunis.log("Received DHDIncomingWormholePacketToClient:  dhdPos: " + message.dhdPos.toString() + ",  dialedAddress: " + message.dialedAddress.toString());
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				DHDTile te = (DHDTile) world.getTileEntity( message.dhdPos );
				
				// message.dialedAddress.add( EnumSymbol.ORIGIN.id );
				List<Integer> address = EnumSymbol.fromLong(message.dialedAddress);
				address.add(EnumSymbol.ORIGIN.id);
				
				te.getDHDRenderer().smoothlyActivateButtons(address);
			});
			
			return null;
		}
		
	}
	
}
