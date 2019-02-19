package mrjake.aunis.packet.dhd.renderingUpdate;

import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.client.Minecraft;
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
		// TODO Cleanup
		@Override
		public IMessage onMessage(ClearLinkedDHDButtons message, MessageContext ctx) {
			
			/*if (ctx.side == Side.SERVER) {
				WorldServer world = ctx.getServerHandler().player.getServerWorld();
				
				if ( world.getBlockState(message.pos).getBlock() instanceof StargateBaseBlock ) {
					StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.pos);
					
					if ( gateTile.isLinked() ) {
						BlockPos dhd = gateTile.getLinkedDHD();
						TargetPoint point = new TargetPoint(world.provider.getDimension(), dhd.getX(), dhd.getY(), dhd.getZ(), 512);
						
						AunisPacketHandler.INSTANCE.sendToAllAround(new ClearLinkedDHDButtons(dhd), point);
					}
				}
			}
			
			else {*/
			World world = Minecraft.getMinecraft().player.getEntityWorld();
			DHDTile dhdTile = ((DHDTile)world.getTileEntity(message.pos));
			
			if (dhdTile != null) {
				DHDRenderer renderer = dhdTile.getDHDRenderer();
				renderer.clearButtons();
			}
						
			return null;
		}
		
	}
}
