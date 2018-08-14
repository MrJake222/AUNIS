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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
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

	
	public static class DHDButtonClickedHandler implements IMessageHandler<GateRenderingUpdatePacketToServer, IMessage> {
		@Override
		public IMessage onMessage(GateRenderingUpdatePacketToServer message, MessageContext ctx) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
			
				EntityPlayer player = ctx.getServerHandler().player;
				World world = player.getEntityWorld();
				BlockPos pos = message.blockPos;
				
				if ( world.isBlockLoaded(pos) ) {
					TargetPoint point = new TargetPoint(ctx.getServerHandler().player.getEntityWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64);
					
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
						case PLAY_ENGAGE_SOUND:							
							world.playSound(null, pos, AunisSoundEvents.gateOpen, SoundCategory.BLOCKS, 1.0f, 1.0f);
							
							break;
						
						case ENGAGE_GATE:
							gateTile.engageGate();
							
							break;
						
						case CLEAR_DHD_BUTTONS:							
							// Clear DHD buttons, Chevrons are cleared in StargateRenderer
							AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, -1, dhdTile), point );
							
							break;
							
						case PLAY_LOCK_SOUND:
							world.playSound(null, pos, AunisSoundEvents.chevronLockDHD, SoundCategory.BLOCKS, 1.0f, 1.0f);
							
							break;
							
						case PLAY_ROLL_SOUND:
							AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.PLAY_ROLL_SOUND, 0, gateTile), point );
							
							break;
							
						case STOP_ROLL_SOUND:
							AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.STOP_ROLL_SOUND, 0, gateTile), point );
							
							break;
							
						default:							
							if (dhdTile != null && gateTile != null) {								
								EnumSymbol symbol = EnumSymbol.valueOf(message.objectID);
								int symbolCount = gateTile.getEnteredSymbolsCount();
								
								if ( symbol == EnumSymbol.BRB ) {						
									if ( gateTile.isEngaged() ) {
										Aunis.log("Gate is engaged, closing...");
										
										// clear connection and address, play closing sound, start animation 
										gateTile.disconnectGate();
										gateTile.clearAddress();
										
										world.playSound(null, pos, AunisSoundEvents.gateClose, SoundCategory.BLOCKS, 1.0f, 1.0f);
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, gateTile), point );
									}
									
									else {
										// Gate is closed, BRB pressed
										
										// Check if symbols entered match the range, and last is ORIGIN
										// TODO: Check if target gate exists
										
										if ( symbolCount >= 7 && symbolCount <= gateTile.getMaxSymbols() && gateTile.checkForPointOfOrigin() ) {
											// All check, play BRB sound, light it up, start gate animation
											world.playSound(null, pos, AunisSoundEvents.dhdPressBRB, SoundCategory.BLOCKS, 1.0f, 1.0f);
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE.packetID, message.objectID, dhdTile.getPos()), point );
											
											// We can't play sound here(like while closing) because renderer must wait some time (SG canon)
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, gateTile), point );
										}
										
										else {
											// Address malformed, dialing failed
											// Clear address, clear DHD buttons and chevrons
											// Stop roll sound, stop ring spin, play fail sound
											gateTile.clearAddress();
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.STOP_ROLL_SOUND, 0, gateTile), point );
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, -1, dhdTile), point );
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.STOP_RING_SPIN, gateTile), point );
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLEAR_CHEVRONS, gateTile), point );
											
											world.playSound(null, pos, AunisSoundEvents.gateDialFail, SoundCategory.BLOCKS, 1.0f, 1.0f);
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
										
										// Play sound of glyph pressed
										world.playSound(null, pos, AunisSoundEvents.dhdPress, SoundCategory.BLOCKS, 1.0f, 1.0f);
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