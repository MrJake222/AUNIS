package mrjake.aunis.packet.gate.renderingUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
		
		buf.writeLong( blockPos.toLong() );
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		packetID = buf.readInt();
		objectID = buf.readInt();

		blockPos = BlockPos.fromLong( buf.readLong() );
	}

	
	public static class GateRenderingUpdatePacketToServerHandler implements IMessageHandler<GateRenderingUpdatePacketToServer, IMessage> {
		@Override
		public IMessage onMessage(GateRenderingUpdatePacketToServer message, MessageContext ctx) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
			
				EntityPlayer player = ctx.getServerHandler().player;
				World world = player.getEntityWorld();
				BlockPos pos = message.blockPos;
				
				if ( world.isBlockLoaded(pos) ) {
					TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64);
					
					TileEntity te = world.getTileEntity(pos);
					StargateBaseTile gateTile;
					DHDTile dhdTile;
					
					if ( te instanceof StargateBaseTile ) {
						gateTile = (StargateBaseTile) te;
						dhdTile = gateTile.getLinkedDHD(world);
					}
					
					else if  ( te instanceof DHDTile ) {
						dhdTile = (DHDTile) te;
						gateTile = dhdTile.getLinkedGate(world);
					}
					
					else {
						// Bad BlockPos given
						
						return;
					}
				
					switch ( EnumPacket.valueOf(message.packetID) ) {
						case ENGAGE_GATE:
							gateTile.engageGate();
							
							break;
						
						case CLEAR_DHD_BUTTONS:							
							// Clear DHD buttons, Chevrons are cleared in StargateRenderer
							if (dhdTile != null)
								AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, -1, dhdTile), point );
							
							break;
							
						default:							
							if (dhdTile != null && gateTile != null) {								
								EnumSymbol symbol = EnumSymbol.valueOf(message.objectID);
								int symbolCount = gateTile.getEnteredSymbolsCount();
								
								if ( symbol == EnumSymbol.BRB ) {						
									if ( gateTile.isEngaged() ) {
										Aunis.log("Gate is engaged, closing...");
										
										// clear connection and address, start animation 
										BlockPos targetPos = StargateNetwork.get(world).getStargate( gateTile.dialedAddress );
										TargetPoint targetPoint = new TargetPoint(world.provider.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 64);
										
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, gateTile), point );
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, targetPos), targetPoint );
										
										gateTile.disconnectGate();
										gateTile.clearAddress();
									}
									
									else {
										// Gate is closed, BRB pressed
										
										// Check if symbols entered match the range, last is ORIGIN, target gate exists, and if not dialing self
										
										Aunis.info("Dialing address: " + gateTile.dialedAddress.toString());
										
										if ( symbolCount >= 7 && symbolCount <= gateTile.getMaxSymbols() && gateTile.checkForPointOfOrigin() && StargateNetwork.get(world).checkForStargate(gateTile.dialedAddress) 
												&& !gateTile.dialedAddress.equals(gateTile.gateAddress) ) {
											// All check, light it up and start gate animation
											
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE.packetID, message.objectID, dhdTile.getPos()), point );
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, gateTile), point );
											
											// Open target gate
											BlockPos targetPos = StargateNetwork.get(world).getStargate( gateTile.dialedAddress );
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, targetPos), point );
										}
										
										else {
											// Address malformed, dialing failed
											// Clear address, clear DHD buttons and chevrons
											// stop ring spin
											
											gateTile.clearAddress();
											// AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, -1, dhdTile), point );
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.GATE_DIAL_FAILED, gateTile), point );
										}
									}	
									
								}
								
								// Not BRB, some glyph pressed
								else {
									if ( gateTile.addSymbolToAddress(symbol) ) {
										// We can still add glyphs(no limit reached)
										symbolCount++;										
										
										// Update the DHD's renderer
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, message.objectID, dhdTile), point );
										
										// Limit not reached, activating in order
										if ( gateTile.getMaxSymbols() > symbolCount ) {
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.ACTIVATE_NEXT, gateTile), point );
										}
										
										// Limit reached, activate the top one
										else if ( gateTile.getMaxSymbols() == symbolCount ) {
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.ACTIVATE_FINAL, gateTile), point );
										}
										
										// Light up target gate, if exists
										if ( symbol == EnumSymbol.ORIGIN && StargateNetwork.get(world).checkForStargate(gateTile.dialedAddress) ) {
											Aunis.info("Origin dialed, target gate present, light it up!");
											
											BlockPos targetPos = StargateNetwork.get(world).getStargate( gateTile.dialedAddress );
											StargateBaseTile targetTile = (StargateBaseTile) world.getTileEntity(targetPos);
											
											TargetPoint targetPoint = new TargetPoint(world.provider.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 64);								
											
											// To renderer: light up chevrons and target dhd glyphs
											if (targetTile.getLinkedDHD(world) != null)
												AunisPacketHandler.INSTANCE.sendToAllAround( new DHDIncomingWormholePacketToClient(targetTile.getLinkedDHD(world), gateTile.dialedAddress), targetPoint );
											
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.LIGHT_UP_ALL_CHEVRONS, targetPos), targetPoint );
										}
										
									} // add symbol if
								} // not brb else
							} // gateTile not null if
							
							break;
					} // switch
				} // block loaded if
			}); // runnable

			return null;
		} // IMessage onMessage end
	} // Handler end
}