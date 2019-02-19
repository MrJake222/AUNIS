package mrjake.aunis.packet.gate.addressUpdate;

import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GateAddressRequestToServer extends PositionedPacket {
	public GateAddressRequestToServer() {}
	
	public GateAddressRequestToServer(BlockPos pos) {
		super(pos);
	}
	
	public static class GateAddressRequestToServerHandler implements IMessageHandler<GateAddressRequestToServer, GateAddressPacketToClient> {

		@Override
		public GateAddressPacketToClient onMessage(GateAddressRequestToServer message, MessageContext ctx) {
			World world = ctx.getServerHandler().player.getEntityWorld();
			BlockPos gatePos = message.pos;
			
			if ( world.isBlockLoaded(gatePos) ) {
				TileEntity te = world.getTileEntity(gatePos);
				
				if (te instanceof StargateBaseTile) {
					StargateBaseTile gateTile = (StargateBaseTile) te;
					
					return new GateAddressPacketToClient(message.pos, gateTile.gateAddress);
				}				
			}
			
			return null;
		}
		
	}
}
