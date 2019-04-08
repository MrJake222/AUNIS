package mrjake.aunis.packet.gate.renderingUpdate;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.packet.dhd.renderingUpdate.DHDIncomingWormholePacketToClient;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.stargate.TeleportHelper;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class GateRenderingUpdatePacketToServer extends PositionedPacket {
	public GateRenderingUpdatePacketToServer() {}
	
	private int objectID;
	
	public GateRenderingUpdatePacketToServer(int objectID, BlockPos pos) {
		super(pos);
		
		this.objectID = objectID;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(objectID);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		objectID = buf.readInt();
	}

	
	public static class GateRenderingUpdatePacketToServerHandler implements IMessageHandler<GateRenderingUpdatePacketToServer, IMessage> {		
		@Override
		public IMessage onMessage(GateRenderingUpdatePacketToServer message, MessageContext ctx) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
			
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.getEntityWorld();
				BlockPos pos = message.pos;
				
				Block block = world.getBlockState(pos).getBlock();
				
				if ( block instanceof StargateBaseBlock || block instanceof DHDBlock ) {
					TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
					
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
						
						/*
						 * Check power requirements
						 * At least some power is required to begin dialing
						 * If no power or no crystal, DHD will appear dead
						 */
						
						ItemStackHandler itemStackHandler = (ItemStackHandler) dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
						ItemStack powerCrystal = itemStackHandler.getStackInSlot(0);
						
						if (powerCrystal.isEmpty()) {
							// No control crystal, display message
							player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.dhd_block.no_crystal_warn")), true);
							
							return;
						}
						
						else {
							EnergyStorage energyStorage = (EnergyStorage) powerCrystal.getCapability(CapabilityEnergy.ENERGY, null);
							EnergyStorage stargateEnergyStorage = (EnergyStorage) gateTile.getCapability(CapabilityEnergy.ENERGY, null);
							
//							Aunis.info("Crystal: " + energyStorage.getEnergyStored() + "uI / " + energyStorage.getMaxEnergyStored() + "uI");
							
							int energy = Math.max(energyStorage.getEnergyStored(), stargateEnergyStorage.getEnergyStored());
							
							if (energy < 10000) {
								player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.dhd_block.no_enough_power")), true);
								
								return;
							}
							
							/*
							 * From now, energy management is passed to the gate itself
							 */
						}
						
						if ( symbol == EnumSymbol.BRB ) {						
							if ( gateTile.isEngaged() ) {
								if ( gateTile.isInitiating() ) {
//									Aunis.info("isClosing: " + gateTile.isClosing());
									
									StargatePos targetGate = StargateNetwork.get(world).getStargate( gateTile.dialedAddress );
									World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
									BlockPos targetPos = targetGate.getPos();
									StargateBaseTile targetTile = (StargateBaseTile) targetWorld.getTileEntity(targetPos);
									
									// clear connection and address, start animation 
									TargetPoint targetPoint = new TargetPoint(targetGate.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 512);
									
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, gateTile), point );
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, targetPos), targetPoint );
									
									targetTile.closeGate(false);
									gateTile.closeGate(false);
								}
								
								else {
									player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.dhd_block.incoming_wormhole_warn")), true);
								}
							}
							
							else {
								// Gate is closed, BRB pressed
								
								if (gateTile.isOpening() || gateTile.isClosing())
									return;
								
								ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
								if (stack.getItem() == AunisItems.dialerFast) {
									NBTTagCompound compound = stack.getTagCompound();
									
									byte[] symbols = compound.getByteArray("address");
									List<EnumSymbol> dialedAddress = new ArrayList<EnumSymbol>();
									
									for (byte s : symbols)
										dialedAddress.add(EnumSymbol.valueOf(s));
									
									dialedAddress.add(EnumSymbol.ORIGIN);
									
									gateTile.dialedAddress = dialedAddress;	
									// gateTile.setRendererState();
									gateTile.fastDialer = true;
								}
								
								// Check if symbols entered match the range, last is ORIGIN, target gate exists, and if not dialing self
								if (StargateNetwork.get(world).stargateInWorld(world, gateTile.dialedAddress) && !gateTile.dialedAddress.subList(0, 6).equals(gateTile.gateAddress)) {
									
									StargatePos targetGate = StargateNetwork.get(world).getStargate( gateTile.dialedAddress );
									BlockPos targetPos = targetGate.getPos();
									World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
									
									boolean nether = targetWorld.provider.getDimensionType() == DimensionType.NETHER;
									int x = targetPos.getX() * (nether ? 8 : 1);
									int y = targetPos.getY() * (nether ? 8 : 1);
									int z = targetPos.getZ() * (nether ? 8 : 1);
									
									int distance = (int) gateTile.getPos().getDistance(x, y, z);

									if (gateTile.hasEnergyToDial(distance, targetWorld)) {
										StargateBaseTile targetTile = (StargateBaseTile) targetWorld.getTileEntity(targetPos);
										
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, message.objectID, dhdTile), point );
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, gateTile), point );
										
										gateTile.openGate(true, 0, null);
										
										DHDTile targetDhdTile = targetTile.getLinkedDHD(targetWorld);
										TargetPoint targetPoint = new TargetPoint(targetGate.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 512);
										
										targetTile.openGate(false, gateTile.dialedAddress.size(), gateTile.gateAddress);
										
										// Open target gate
										if (targetDhdTile != null)
											AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, message.objectID, targetDhdTile), targetPoint );
										
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, targetPos), targetPoint );
									}
									
									else {
										gateTile.closeGate(true);
										
										player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.stargatebase_block.not_enough_power")), true);
										AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.GATE_DIAL_FAILED, gateTile), point );
									}
								}
								
								else {
									// Address malformed, dialing failed
									// Execute GATE_DIAL_FAILED
									
									gateTile.closeGate(true);
									
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.GATE_DIAL_FAILED, gateTile), point );
								}
							}	
							
						}
						
						// Not BRB, some glyph pressed
						else {
							if ( gateTile.addSymbolToAddress(symbol, dhdTile) ) {
								// We can still add glyphs(no limit reached)
								int symbolCount = gateTile.getEnteredSymbolsCount();				
																
								// Update the DHD's renderer
								AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, message.objectID, dhdTile), point );
								
								// Limit not reached, activating in order
								//if ( gateTile.getMaxSymbols() > symbolCount ) {
								if ( (dhdTile.hasUpgrade() && symbolCount == 8) || (!dhdTile.hasUpgrade() && symbolCount == 7) || (symbolCount == 7 && symbol == EnumSymbol.ORIGIN) ) {
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.ACTIVATE_FINAL, gateTile), point );
								}
								
								else {
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.ACTIVATE_NEXT, gateTile), point );
								}
								
								StargateNetwork network = StargateNetwork.get(world);
																
								// Light up target gate, if exists
								if ( symbol == EnumSymbol.ORIGIN && network.stargateInWorld(world, gateTile.dialedAddress) ) {	
									
									StargatePos targetGate = StargateNetwork.get(world).getStargate( gateTile.dialedAddress );
									World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
									
									BlockPos targetPos = targetGate.getPos();
									StargateBaseTile targetTile = (StargateBaseTile) targetWorld.getTileEntity(targetPos);
									DHDTile targetDhdTile = targetTile.getLinkedDHD(targetWorld);
									
									TargetPoint targetPoint = new TargetPoint(targetGate.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 512);
									
									boolean eightChevronDial = gateTile.dialedAddress.size() == 8;
									
									targetTile.incomingWormhole(gateTile.gateAddress, gateTile.dialedAddress.size());
									
									// To renderer: light up chevrons and target dhd glyphs																
									if (targetDhdTile != null)
										AunisPacketHandler.INSTANCE.sendToAllAround( new DHDIncomingWormholePacketToClient(targetDhdTile.getPos(), gateTile.gateAddress, eightChevronDial), targetPoint );	
									
									EnumGateAction gateAction;
									if (eightChevronDial)
										gateAction = EnumGateAction.LIGHT_UP_8_CHEVRONS;
									else
										gateAction = EnumGateAction.LIGHT_UP_7_CHEVRONS;
									
									AunisPacketHandler.INSTANCE.sendToAllAround( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, gateAction, targetPos), targetPoint );
								}
							} // add symbol if
						} // not brb else
					} // gateTile and dhdTile not null if
					
					else {
						// DHD is not linked
						player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.dhd_block.not_linked_warn")), true);
					}
					
				} // block loaded if
			}); // runnable

			return null;
		} // IMessage onMessage end
	} // Handler end
}
