package mrjake.aunis.packet.infuser;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.renderer.crystalinfuser.CrystalInfuserRenderer;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ShouldRenderWavesToClient extends PositionedPacket {
	public ShouldRenderWavesToClient() {}
	
	private boolean renderWaves;
	
	public ShouldRenderWavesToClient(BlockPos pos, boolean renderWaves) {
		super(pos);
		
		this.renderWaves = renderWaves;
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeBoolean(renderWaves);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		renderWaves = buf.readBoolean();
	}
	
	public static class ShouldRenderWavesToClientHandler implements IMessageHandler<ShouldRenderWavesToClient, IMessage> {
		
		@Override
		public IMessage onMessage(ShouldRenderWavesToClient message, MessageContext ctx) {
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			World world = player.getEntityWorld();
				
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				CrystalInfuserTile infuserTile = (CrystalInfuserTile) world.getTileEntity(message.pos);
				CrystalInfuserRenderer infuserRenderer = (CrystalInfuserRenderer) infuserTile.getRenderer();
				
				infuserRenderer.shouldRenderWaves(message.renderWaves);
			});
			
			return null;
		}
	}
}
