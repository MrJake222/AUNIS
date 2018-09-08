package mrjake.aunis.packet.dhd;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.gui.StargateGUI;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenStargateAddressGuiToClient implements IMessage {
	public OpenStargateAddressGuiToClient() {}
	
	private BlockPos gatePos;
	private int symbolsCount;
	
	public OpenStargateAddressGuiToClient(BlockPos gatePos, int symbolsCount) {
		this.gatePos = gatePos;
		this.symbolsCount = symbolsCount;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( gatePos.toLong() );
		buf.writeInt(symbolsCount);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		gatePos = BlockPos.fromLong( buf.readLong() );
		symbolsCount = buf.readInt();
	}

	
	public static class OpenStargateAddressGuiClientHandler implements IMessageHandler<OpenStargateAddressGuiToClient, IMessage> {

		@Override
		public IMessage onMessage(OpenStargateAddressGuiToClient message, MessageContext ctx) {
			World world = Minecraft.getMinecraft().player.world;
			StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.gatePos);
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				Minecraft.getMinecraft().displayGuiScreen( new StargateGUI(gateTile, message.symbolsCount) );
			});
			
			return null;
		}
		
	}
}
