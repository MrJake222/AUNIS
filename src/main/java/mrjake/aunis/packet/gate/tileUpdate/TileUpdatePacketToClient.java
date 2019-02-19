package mrjake.aunis.packet.gate.tileUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.UpgradeRendererState;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TileUpdatePacketToClient extends PositionedPacket {
	public TileUpdatePacketToClient() {}
	
	private RendererState rendererState;
	
	private boolean supportsUpgrade;
	private UpgradeRendererState upgradeRendererState;
	
	public TileUpdatePacketToClient(BlockPos pos, RendererState rendererState) {
		super(pos);
		
		this.rendererState = rendererState;
		this.supportsUpgrade = false;
	}
	
	public TileUpdatePacketToClient(BlockPos pos, RendererState rendererState, UpgradeRendererState upgradeRendererState) {
		this(pos, rendererState);
		
		this.supportsUpgrade = true;
		this.upgradeRendererState = upgradeRendererState;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {		
		super.toBytes(buf);
		
		rendererState.toBytes(buf);	
		
		buf.writeBoolean(supportsUpgrade);
		if (supportsUpgrade)
			upgradeRendererState.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		ITileEntityRendered te = (ITileEntityRendered) Minecraft.getMinecraft().world.getTileEntity(pos);
		rendererState = te.createRendererState(buf);
		
		supportsUpgrade = buf.readBoolean();
		if (supportsUpgrade)
			this.upgradeRendererState = new UpgradeRendererState(buf);
	}
	
	public static class TileUpdateClientHandler implements IMessageHandler<TileUpdatePacketToClient, IMessage> {

		@SuppressWarnings("unchecked")
		@Override
		public IMessage onMessage(TileUpdatePacketToClient message, MessageContext ctx) {			
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			World world = player.getEntityWorld();
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
								
				ITileEntityRendered te = (ITileEntityRendered) world.getTileEntity(message.pos);
				
				te.getRenderer().setState(message.rendererState);
				
				if (message.supportsUpgrade)
					((ITileEntityUpgradeable) te).getUpgradeRenderer().setState(message.upgradeRendererState);
				
			});
			
			return null;
		}
		
	}
}
