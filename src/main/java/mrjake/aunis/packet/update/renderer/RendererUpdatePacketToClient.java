package mrjake.aunis.packet.update.renderer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.UpgradeRendererState;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RendererUpdatePacketToClient extends PositionedPacket {
	public RendererUpdatePacketToClient() {}
	
	private RendererState rendererState;
	
	private boolean supportsUpgrade;
	private UpgradeRendererState upgradeRendererState;
	private ByteBuf stateBuf;
	
	public RendererUpdatePacketToClient(BlockPos pos, RendererState rendererState) {
		super(pos);
		
		this.rendererState = rendererState;
		this.supportsUpgrade = false;
	}
	
	public RendererUpdatePacketToClient(BlockPos pos, RendererState rendererState, UpgradeRendererState upgradeRendererState) {
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
		
		stateBuf = buf.copy();
	}
	
	public static class TileUpdateClientHandler implements IMessageHandler<RendererUpdatePacketToClient, IMessage> {

		@SuppressWarnings("unchecked")
		@Override
		public IMessage onMessage(RendererUpdatePacketToClient message, MessageContext ctx) {			
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			World world = player.getEntityWorld();
			
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				ITileEntityRendered te = (ITileEntityRendered) world.getTileEntity(message.pos);
				te.getRenderer().setState(te.createRendererState(message.stateBuf));
				
				// supportsUpgrade
				if (message.stateBuf.readBoolean()) {
					((ITileEntityUpgradeable) te).getUpgradeRenderer().setState((UpgradeRendererState) new UpgradeRendererState().fromBytes(message.stateBuf));	
				}
			});
			
			return null;
		}
		
	}
}
