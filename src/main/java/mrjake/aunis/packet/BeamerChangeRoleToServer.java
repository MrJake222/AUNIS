package mrjake.aunis.packet;

import mrjake.aunis.tileentity.BeamerTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BeamerChangeRoleToServer extends PositionedPacket {
	public BeamerChangeRoleToServer() {}
		
	public BeamerChangeRoleToServer(BlockPos pos) {
		super(pos);
	}
	
	
	public static class BeamerChangeRoleServerHandler implements IMessageHandler<BeamerChangeRoleToServer, IMessage> {

		@Override
		public IMessage onMessage(BeamerChangeRoleToServer message, MessageContext ctx) {
			WorldServer world = (WorldServer) ctx.getServerHandler().player.getEntityWorld();
			
			world.addScheduledTask(() -> {
				BeamerTile beamerTile = (BeamerTile) world.getTileEntity(message.pos);
				
				if (beamerTile != null) {
					beamerTile.setNextRole();
				}
			});
			
			return null;
		}
		
	}

}
