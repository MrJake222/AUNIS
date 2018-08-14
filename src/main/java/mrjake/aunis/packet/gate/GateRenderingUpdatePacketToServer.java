package mrjake.aunis.packet.gate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisSoundEvents;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.dhd.DHDTile;
import mrjake.aunis.stargate.sgbase.StargateBaseTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// Send from client to server
public class GateRenderingUpdatePacketToServer implements IMessage {
	public GateRenderingUpdatePacketToServer() {}
	
	private int packetID;
	private int objectID;
	private BlockPos blockPos; 
	
	public GateRenderingUpdatePacketToServer(EnumPacket packet, int objectID, BlockPos pos) {
		this.packetID = packet.packetID;
		this.objectID = objectID;
		this.blockPos = pos;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(packetID);
		buf.writeInt(objectID);
		buf.writeInt(blockPos.getX());
		buf.writeInt(blockPos.getY());
		buf.writeInt(blockPos.getZ());
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		packetID = buf.readInt();
		objectID = buf.readInt();
		blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
	}

	
	public static class DHDButtonClickedHandler implements IMessageHandler<GateRenderingUpdatePacketToServer, IMessage>{
		
		@Override
		public IMessage onMessage(GateRenderingUpdatePacketToServer message, MessageContext ctx) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
			
				EntityPlayer player = ctx.getServerHandler().player;
				World world = player.getEntityWorld();
				BlockPos pos = message.blockPos;
				
				if ( world.isBlockLoaded(pos) ) {
					if ( message.packetID == EnumPacket.ENGAGE_GATE.packetID ) {
						StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
						
						gateTile.engageGate();
						world.playSound(null, pos, AunisSoundEvents.gateOpen, SoundCategory.BLOCKS, 1.0f, 1.0f);
					}
					
					else if ( message.packetID == EnumPacket.CLOSE_GATE.packetID ) {
						StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
						
						gateTile.disconnectGate();
						gateTile.clearAddress();
						
						TargetPoint point = new TargetPoint(ctx.getServerHandler().player.getEntityWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64);
						AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHDButton.packetID, -1, gateTile.getLinkedDHD(world).getPos()), point );
						
						world.playSound(null, pos, AunisSoundEvents.gateClose, SoundCategory.BLOCKS, 1.0f, 1.0f);
					}
					
					else {
						if ( world.getTileEntity(pos) instanceof DHDTile ) {		
							DHDTile te = (DHDTile) world.getTileEntity(pos);
							BlockPos gate = te.getLinkedGate();
							
							if (gate != null) {
								StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(gate);
								
								EnumSymbol symbol = EnumSymbol.values()[message.objectID];
								
								TargetPoint point = new TargetPoint(ctx.getServerHandler().player.getEntityWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64);
								
								if ( symbol == EnumSymbol.BRB ) {						
									if (gateTile.isEngaged()/* ||  gateTile.getDialedChevrons() != gateTile.getMaxChevrons()*/) {
										//if ( gateTile.isEngaged() ) {
											//gateTile.disconnectGate();
										//}
										
										Aunis.info("Gate is engaged, closing...");
										
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.Chevron.packetID, EnumGateAction.CLOSE_GATE.actionID, gate),  point );
									}
									
									else {
										world.playSound(null, pos, AunisSoundEvents.dhdPressBRB, SoundCategory.BLOCKS, 1.0f, 1.0f);
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHDButton.packetID, message.objectID, pos), point );
										
										
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.Chevron.packetID, EnumGateAction.OPEN_GATE.actionID, gate), point );
									}
								}
								
								else {
									if ( gateTile.addSymbolToAddress(symbol) ) {
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHDButton.packetID, message.objectID, pos), point );
										
										if (gateTile.getMaxChevrons() > gateTile.getDialedChevrons()) {
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.Chevron.packetID, EnumGateAction.ACTIVATE_NEXT.actionID, gate),  point );
										}
										
										else if (gateTile.getMaxChevrons() == gateTile.getDialedChevrons()) {
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.Chevron.packetID, EnumGateAction.ACTIVATE_FINAL.actionID, gate),  point );
										}
										
										world.playSound(null, pos, AunisSoundEvents.dhdPress, SoundCategory.BLOCKS, 1.0f, 1.0f);
									}
								}
							}				
						}
					}
				}
			});

			return null;
		}
	}
}