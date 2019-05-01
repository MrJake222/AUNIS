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

public class ClearLinkedDHDButtons extends PositionedPacket {
	public ClearLinkedDHDButtons() {}
		
	public ClearLinkedDHDButtons(BlockPos pos) {
		super(pos);
	}	
	
	public static class ClearLinkedDHDButtonsHandler implements IMessageHandler<ClearLinkedDHDButtons, IMessage> {

		@Override
		public IMessage onMessage(ClearLinkedDHDButtons message, MessageContext ctx) {
			EntityPlayer player = Aunis.proxy.getPlayerInMessageHandler(ctx);
			World world = player.getEntityWorld();
			
			DHDTile dhdTile = ((DHDTile)world.getTileEntity(message.pos));
			
			if (dhdTile != null) {
				DHDRenderer renderer = dhdTile.getDHDRenderer();
				renderer.clearButtons();
			}
						
			return null;
		}
		
	}
}
