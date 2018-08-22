package mrjake.aunis.packet.dhd.renderingUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClearLinkedDHDButtons implements IMessage {
	public ClearLinkedDHDButtons() {}
	
	private BlockPos gatePos;
	
	public ClearLinkedDHDButtons(BlockPos pos) {
		this.gatePos = pos;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(gatePos.toLong());
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		gatePos = BlockPos.fromLong(buf.readLong());
	}
	
	
	public static class ClearLinkedDHDButtonsHandler implements IMessageHandler<ClearLinkedDHDButtons, IMessage> {

		@Override
		public IMessage onMessage(ClearLinkedDHDButtons message, MessageContext ctx) {
			
			if (ctx.side == Side.SERVER) {
				WorldServer world = ctx.getServerHandler().player.getServerWorld();
				
				if ( world.getBlockState(message.gatePos).getBlock() instanceof StargateBaseBlock ) {
					StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.gatePos);
					
					if ( gateTile.isLinked() ) {
						BlockPos dhd = gateTile.getLinkedDHD();
						TargetPoint point = new TargetPoint(world.provider.getDimension(), dhd.getX(), dhd.getY(), dhd.getZ(), 64);
						
						AunisPacketHandler.INSTANCE.sendToAllAround(new ClearLinkedDHDButtons(dhd), point);
					}
				}
			}
			
			else {
				World world = Minecraft.getMinecraft().player.getEntityWorld();
				DHDRenderer renderer = (DHDRenderer) ((DHDTile)world.getTileEntity(message.gatePos)).getRenderer();
				
				renderer.clearButtons();				
			}
						
			return null;
		}
		
	}
}
