package mrjake.aunis.packet.dhd.renderingUpdate;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DHDIncomingWormholePacketToClient extends PositionedPacket {
	public DHDIncomingWormholePacketToClient() {}
	
	private long gateAddress;
	private int lastSymbolId;
	private boolean include7thSymbol;
	
	public DHDIncomingWormholePacketToClient(BlockPos pos, List<EnumSymbol> gateAddress, boolean include7thSymbol) {	
		super(pos);
		
		this.gateAddress = EnumSymbol.toLong(gateAddress);
		this.include7thSymbol = include7thSymbol;
		this.lastSymbolId = gateAddress.get(gateAddress.size()-1).id;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeLong(gateAddress);
		buf.writeBoolean(include7thSymbol);
		
		if (include7thSymbol)
			buf.writeInt(lastSymbolId);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		gateAddress = buf.readLong();
		include7thSymbol = buf.readBoolean();
		
		if (include7thSymbol)
			lastSymbolId = buf.readInt();
	}

	public static class DHDIncomingWormholePacketToClientHandler implements IMessageHandler<DHDIncomingWormholePacketToClient, IMessage> {

		@Override
		public IMessage onMessage(DHDIncomingWormholePacketToClient message, MessageContext ctx) {
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			World world = player.getEntityWorld();
						
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				DHDTile te = (DHDTile) world.getTileEntity( message.pos );
				
				List<Integer> address = EnumSymbol.fromLong(message.gateAddress);
				
				if (message.include7thSymbol)
					address.add(message.lastSymbolId);
				
				address.add(EnumSymbol.ORIGIN.id);
				
				te.getDHDRenderer().smoothlyActivateButtons(address);
			});
			
			return null;
		}
		
	}
	
}
