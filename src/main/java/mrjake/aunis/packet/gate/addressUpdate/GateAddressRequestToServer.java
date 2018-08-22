package mrjake.aunis.packet.gate.addressUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GateAddressRequestToServer implements IMessage {
	public GateAddressRequestToServer() {}
	
	public GateAddressRequestToServer(BlockPos gatePos) {
		this.gatePos = gatePos;
	}
	
	private BlockPos gatePos;
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( gatePos.toLong() );		
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		gatePos = BlockPos.fromLong( buf.readLong() );
	}
	
	public static class GateAddressRequestToServerHandler implements IMessageHandler<GateAddressRequestToServer, GateAddressPacketToClient> {

		@Override
		public GateAddressPacketToClient onMessage(GateAddressRequestToServer message, MessageContext ctx) {
			World world = ctx.getServerHandler().player.getEntityWorld();
			BlockPos gatePos = message.gatePos;
			
			if ( world.isBlockLoaded(gatePos) ) {
				TileEntity te = world.getTileEntity(gatePos);
				
				if (te instanceof StargateBaseTile) {
					StargateBaseTile gateTile = (StargateBaseTile) te;
					
					return new GateAddressPacketToClient(message.gatePos, gateTile.gateAddress);
				}				
			}
			
			return null;
		}
		
	}
}
