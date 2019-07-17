package mrjake.aunis.packet.dhd.renderingUpdate;

import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClearLinkedDHDButtonsToClient extends PositionedPacket {
	public ClearLinkedDHDButtonsToClient() {}
		
	public ClearLinkedDHDButtonsToClient(BlockPos pos) {
		super(pos);
	}	
	
	public static class ClearLinkedDHDButtonsClientHandler implements IMessageHandler<ClearLinkedDHDButtonsToClient, IMessage> {

		@Override
		public IMessage onMessage(ClearLinkedDHDButtonsToClient message, MessageContext ctx) {
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			World world = player.getEntityWorld();
			
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				DHDTile dhdTile = ((DHDTile)world.getTileEntity(message.pos));
			
				if (dhdTile != null) {
					DHDRenderer renderer = dhdTile.getDHDRenderer();
					renderer.clearButtons();
				}
			});
						
			return null;
		}
		
	}
}
