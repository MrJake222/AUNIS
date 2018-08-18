package mrjake.aunis.packet.gate.addressUpdate;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GateAddressPacketToClient implements IMessage {
	public GateAddressPacketToClient() {}
	
	public GateAddressPacketToClient(BlockPos gatePos, List<EnumSymbol> gateAddress) {
		this.gatePos = gatePos;
		this.gateAddress = gateAddress;
	}
	
	private BlockPos gatePos;
	private List<EnumSymbol> gateAddress;
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( gatePos.toLong() );
		buf.writeInt(gateAddress.size());
		
		for (EnumSymbol symbol : gateAddress) {
			buf.writeInt(symbol.id);
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		gatePos = BlockPos.fromLong( buf.readLong() );
		
		int len = buf.readInt();
		gateAddress = new ArrayList<EnumSymbol>();
		
		for (int i=0; i<len; i++) {
			gateAddress.add( EnumSymbol.valueOf(buf.readInt()) );
		}
	}

	public static class GateAddressPacketToClientHandler implements IMessageHandler<GateAddressPacketToClient, IMessage> {

		@Override
		public IMessage onMessage(GateAddressPacketToClient message, MessageContext ctx) {
			World world = Minecraft.getMinecraft().world;
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				StargateBaseTile te = (StargateBaseTile) world.getTileEntity( message.gatePos );
				
				te.gateAddress = message.gateAddress;
			});
			
			return null;
		}
		
	}
	
}
