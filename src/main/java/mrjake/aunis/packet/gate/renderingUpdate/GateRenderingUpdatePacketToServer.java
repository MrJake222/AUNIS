package mrjake.aunis.packet.gate.renderingUpdate;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.dhd.renderingUpdate.DHDIncomingWormholePacketToClient;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.block.Block;
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
				
				Block block = world.getBlockState(pos).getBlock();
				
				if ( block instanceof StargateBaseBlock || block instanceof DHDBlock ) {
					TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64);
					
					TileEntity te = world.getTileEntity(pos);
					StargateBaseTile gateTile;
					DHDTile dhdTile;
					
					if ( te instanceof StargateBaseTile ) {
						gateTile = (StargateBaseTile) te;
						dhdTile = gateTile.getLinkedDHD(world);
					}
					
					else if ( te instanceof DHDTile ) {
						dhdTile = (DHDTile) te;
						gateTile = dhdTile.getLinkedGate(world);
					}
					
					else {
						// Bad BlockPos given
						
						return;
					}
					
					if (dhdTile != null && gateTile != null) {						
						EnumSymbol symbol = EnumSymbol.valueOf(message.objectID);
						
						if ( symbol == EnumSymbol.BRB ) {						
							if ( gateTile.isEngaged() ) {
								Aunis.log("Gate is engaged, closing...");
								
								BlockPos targetPos = StargateNetwork.get(world).getStargate( gateTile.dialedAddress );
								StargateBaseTile targetTile = (StargateBaseTile) world.getTileEntity(targetPos);
								
								// clear connection and address, start animation 
								TargetPoint targetPoint = new TargetPoint(world.provider.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 64);
								
								AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, gateTile), point );
								AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, targetPos), targetPoint );
								
								DHDTile targetDhdTile = targetTile.getLinkedDHD(world);
								
								// Open target gate
								if (targetDhdTile != null) {
									targetDhdTile.setLinkedGateEngagement(false);
								}
								
								dhdTile.setLinkedGateEngagement(false);
								
								targetTile.closeGate();
								gateTile.closeGate();
								gateTile.clearAddress();
							}
							
							else {
								// Gate is closed, BRB pressed
								
								Aunis.log(gateTile.getPos()+": Dialing address: " + gateTile.dialedAddress.toString());
								
								int symbolCount = gateTile.getEnteredSymbolsCount();
								
								List<EnumSymbol> gateAddressWithOrigin = gateTile.gateAddress;
								gateAddressWithOrigin.add(EnumSymbol.ORIGIN);
								
								// Check if symbols entered match the range, last is ORIGIN, target gate exists, and if not dialing self
								if ( symbolCount >= 7 && symbolCount <= gateTile.getMaxSymbols() && gateTile.checkForPointOfOrigin() && StargateNetwork.get(world).checkForStargate(gateTile.dialedAddress) 
										&& !gateTile.dialedAddress.equals(gateAddressWithOrigin) ) {
									// All check, light it up and start gate animation
																
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, message.objectID, dhdTile), point );
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, gateTile), point );
									dhdTile.setLinkedGateEngagement(true);
									dhdTile.setRendererState( new DHDRendererState(dhdTile.getPos(), EnumSymbol.toIntegerList(gateTile.dialedAddress, EnumSymbol.BRB)) );
									
									gateTile.openGate(true);
									
									BlockPos targetPos = StargateNetwork.get(world).getStargate( gateTile.dialedAddress );
									StargateBaseTile targetTile = (StargateBaseTile) world.getTileEntity(targetPos);
									
									DHDTile targetDhdTile = targetTile.getLinkedDHD(world);
									TargetPoint targetPoint = new TargetPoint(world.provider.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 64);
									
									// Open target gate
									if (targetDhdTile != null) {
										targetDhdTile.setLinkedGateEngagement(true);
										
										targetDhdTile.setRendererState( new DHDRendererState(targetDhdTile.getPos(), EnumSymbol.toIntegerList(gateAddressWithOrigin, EnumSymbol.BRB)) );
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, message.objectID, targetDhdTile), targetPoint );
									}
									
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, targetPos), targetPoint );
									targetTile.openGate(false);
								}
								
								else {
									// Address malformed, dialing failed
									// Execute GATE_DIAL_FAILED
									
									gateTile.clearAddress();
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.GATE_DIAL_FAILED, gateTile), point );
								}
							}	
							
						}
						
						// Not BRB, some glyph pressed
						else {
							if ( gateTile.addSymbolToAddress(symbol) ) {
								// We can still add glyphs(no limit reached)
								int symbolCount = gateTile.getEnteredSymbolsCount();				
																
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
								
								StargateNetwork network = StargateNetwork.get(world);
																
								// Light up target gate, if exists
								if ( symbol == EnumSymbol.ORIGIN && network.checkForStargate(gateTile.dialedAddress) ) {
									BlockPos targetPos = network.getStargate( gateTile.dialedAddress );
									StargateBaseTile targetTile = (StargateBaseTile) world.getTileEntity(targetPos);
									DHDTile targetDhdTile = targetTile.getLinkedDHD(world);
									
									TargetPoint targetPoint = new TargetPoint(world.provider.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 64);
									
									// To renderer: light up chevrons and target dhd glyphs																
									if (targetDhdTile != null) {
										targetDhdTile.setRendererState( new DHDRendererState(targetDhdTile.getPos(), EnumSymbol.toIntegerList(gateTile.dialedAddress)) );
										AunisPacketHandler.INSTANCE.sendToAllAround( new DHDIncomingWormholePacketToClient(targetDhdTile.getPos(), gateTile.gateAddress), targetPoint );	
									}
										
									targetTile.getStargateRendererState().activeChevrons = targetTile.getMaxSymbols();
									targetTile.getStargateRendererState().isFinalActive = true;
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.LIGHT_UP_ALL_CHEVRONS, targetPos), targetPoint );
								}
							} // add symbol if
						} // not brb else
					} // gateTile not null if
				} // block loaded if
			}); // runnable

			return null;
		} // IMessage onMessage end
	} // Handler end
}