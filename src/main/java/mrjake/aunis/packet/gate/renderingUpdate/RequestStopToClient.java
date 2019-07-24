package mrjake.aunis.packet.gate.renderingUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.renderer.stargate.StargateRendererSG1;
import mrjake.aunis.renderer.state.SpinState;
import mrjake.aunis.tileentity.stargate.StargateBaseTileSG1;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * When the gate is not rendered, it's not aware of {@link SpinState#tickStopRequested}.
 * 
 * @author MrJake222
 *
 */
public class RequestStopToClient extends PositionedPacket {
	public RequestStopToClient() {}
	
	public long worldTicks;
	public boolean moveOnly;
	
	public RequestStopToClient(BlockPos pos, long worldTicks, boolean moveOnly) {
		super(pos);
		
		this.worldTicks = worldTicks;
		this.moveOnly = moveOnly;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeLong(worldTicks);
		buf.writeBoolean(moveOnly);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		worldTicks = buf.readLong();
		moveOnly = buf.readBoolean();
	}
	
	
	public static class RequestStopClientHandler implements IMessageHandler<RequestStopToClient, IMessage> {

		@Override
		public IMessage onMessage(RequestStopToClient message, MessageContext ctx) {
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			World world = player.world;
			
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				StargateBaseTileSG1 gateTile = (StargateBaseTileSG1) world.getTileEntity(message.pos);
				
				message.worldTicks-=1;
//				Aunis.info("worldTicks delta: " + (world.getTotalWorldTime()-message.worldTicks));
				
				((StargateRendererSG1) gateTile.getRenderer()).requestStopByComputer(message.worldTicks, message.moveOnly);
			});
			
			return null;
		}
		
	}
}
