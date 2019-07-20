package mrjake.aunis.packet.gate.renderingUpdate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.stargate.EnumGateState;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.stargate.TeleportHelper;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.ScheduledTask;
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
import net.minecraftforge.energy.IEnergyStorage;
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

	/**
	 * Closes gate server side
	 * Handles animation packets both sides(initiating and receiving)
	 * Calls closeGate()
	 * 
	 * @param sourceTile - Source StargateBaseTile instance
	 */
	public static void closeGatePacket(StargateBaseTile sourceTile, boolean targetOnly) {
		StargatePos targetGate = StargateNetwork.get(sourceTile.getWorld()).getStargate( sourceTile.dialedAddress );
		World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
		BlockPos targetPos = targetGate.getPos();
		StargateBaseTile targetTile = (StargateBaseTile) targetWorld.getTileEntity(targetPos);
		
		if (!targetOnly) {
			BlockPos sourcePos = sourceTile.getPos();
			TargetPoint point = new TargetPoint(sourceTile.getWorld().provider.getDimension(), sourcePos.getX(), sourcePos.getY(), sourcePos.getZ(), 512);
			
			AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, sourceTile), point );
			sourceTile.closeGate(false, true);
		}

		TargetPoint targetPoint = new TargetPoint(targetGate.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 512);
		AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.CLOSE_GATE, targetPos), targetPoint );
		targetTile.closeGate(false, true);
	}
	
	public static void attemptLightUp(World world, StargateBaseTile sourceGateTile) {
		if (StargateNetwork.get(world).stargateInWorld(world, sourceGateTile.dialedAddress) && !sourceGateTile.dialedAddress.subList(0, 6).equals(sourceGateTile.gateAddress.subList(0, 6))) {
			StargatePos targetGate = StargateNetwork.get(world).getStargate(sourceGateTile.dialedAddress);
			World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
			
			BlockPos targetPos = targetGate.getPos();
			StargateBaseTile targetTile = (StargateBaseTile) targetWorld.getTileEntity(targetPos);
			DHDTile targetDhdTile = targetTile.getLinkedDHD(targetWorld);
			
			TargetPoint targetPoint = new TargetPoint(targetGate.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 512);
			
			boolean eightChevronDial = sourceGateTile.dialedAddress.size() == 8;
			
			targetTile.incomingWormhole(sourceGateTile.gateAddress, sourceGateTile.dialedAddress.size());
			
			// To renderer: light up chevrons and target dhd glyphs																
			if (targetDhdTile != null)
				targetDhdTile.activateSymbols(EnumSymbol.toIntegerList(eightChevronDial ? sourceGateTile.gateAddress : sourceGateTile.gateAddress.subList(0, 6), EnumSymbol.ORIGIN));
			
			EnumGateAction gateAction;
			if (eightChevronDial)
				gateAction = EnumGateAction.LIGHT_UP_8_CHEVRONS;
			else
				gateAction = EnumGateAction.LIGHT_UP_7_CHEVRONS;
			
			AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, gateAction, targetPos), targetPoint );
		 }
	}
	
	public static EnumGateState attemptOpen(World world, StargateBaseTile sourceGateTile, @Nullable DHDTile sourceDhdTile, boolean stopRing) {
		BlockPos sourcePos = sourceGateTile.getPos();
		TargetPoint point = new TargetPoint(world.provider.getDimension(), sourcePos.getX(), sourcePos.getY(), sourcePos.getZ(), 512);
		
		// Check if symbols entered match the range, last is ORIGIN, target gate exists, and if not dialing self
		if (StargateNetwork.get(world).stargateInWorld(world, sourceGateTile.dialedAddress) && !sourceGateTile.dialedAddress.subList(0, 6).equals(sourceGateTile.gateAddress.subList(0, 6))) {
			
			StargatePos targetGate = StargateNetwork.get(world).getStargate( sourceGateTile.dialedAddress );
			BlockPos targetPos = targetGate.getPos();
			World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
			
//			BlockPos sourceTranslated
			
			int distance = (int) sourcePos.getDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ());
			double multiplier = 1;
			
			// It the dimensions are the same, no multiplier
			if (targetWorld.provider.getDimensionType() != world.provider.getDimensionType()) { 
				if (targetWorld.provider.getDimensionType() == DimensionType.NETHER || world.provider.getDimensionType() == DimensionType.NETHER) {
					distance /= 8;
				}
				
				multiplier = AunisConfig.powerConfig.crossDimensionMul;
			}
			
			if (sourceGateTile.hasEnergyToDial(distance, multiplier)) {
				StargateBaseTile targetTile = (StargateBaseTile) targetWorld.getTileEntity(targetPos);
				
				if (sourceDhdTile != null) 
					sourceDhdTile.activateSymbol(EnumSymbol.BRB.id);
				
				AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, sourceGateTile), point );
				
				sourceGateTile.openGate(true, 0, null);
				
				DHDTile targetDhdTile = targetTile.getLinkedDHD(targetWorld);
				TargetPoint targetPoint = new TargetPoint(targetGate.getDimension(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), 512);
				
				targetTile.openGate(false, sourceGateTile.dialedAddress.size(), sourceGateTile.gateAddress);
				
				if (targetDhdTile != null) 
					targetDhdTile.activateSymbol(EnumSymbol.BRB.id);
				
				AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.OPEN_GATE, targetPos), targetPoint );
			}
			
			else {
				sourceGateTile.closeGate(true, stopRing);
				
				AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.GATE_DIAL_FAILED, sourceGateTile), point );
				return EnumGateState.NOT_ENOUGH_POWER;
			}
		}
		
		else {
			// Address malformed, dialing failed
			// Execute GATE_DIAL_FAILED
			
			sourceGateTile.closeGate(true, stopRing);
			
			AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.GATE_DIAL_FAILED, sourceGateTile), point );
			return EnumGateState.ADDRESS_MALFORMED;
		}
		
		return EnumGateState.OK;
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
						if ((gateTile.getStargateState() != EnumStargateState.COMPUTER_DIALING && gateTile.getStargateState() != EnumStargateState.FAILING)) {
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
	//							EnergyStorage energyStorage = (EnergyStorage) powerCrystal.getCapability(CapabilityEnergy.ENERGY, null);
	//							EnergyStorage stargateEnergyStorage = (EnergyStorage) gateTile.getCapability(CapabilityEnergy.ENERGY, null);
	//							
	////							Aunis.info("Crystal: " + energyStorage.getEnergyStored() + "uI / " + energyStorage.getMaxEnergyStored() + "uI");
	//							
	//							int energy = Math.max(energyStorage.getEnergyStored(), stargateEnergyStorage.getEnergyStored());
								
								IEnergyStorage energyStorage = gateTile.getEnergyStorage(AunisConfig.powerConfig.dhdMinimalEnergy);
								
								if (energyStorage == null) {
									player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.dhd_block.no_enough_power")), true);
									
									return;
								}
							}
							
							if ( symbol == EnumSymbol.BRB ) {						
								if ( gateTile.isEngaged() ) {
									if ( gateTile.isInitiating() ) {									
										closeGatePacket(gateTile, false);
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
									EnumGateState gateState = attemptOpen(world, gateTile, dhdTile, true);
									
									if (gateState != EnumGateState.OK) {
										gateTile.setRollPlayed();
										
										if (gateState == EnumGateState.NOT_ENOUGH_POWER)
											player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.stargatebase_block.not_enough_power")), true);
									}
								}	
								
							}
							
							// Not BRB, some glyph pressed
							else {								
								if ( gateTile.addSymbolToAddress(symbol, dhdTile, false) ) {
									// We can still add glyphs(no limit reached)
									int symbolCount = gateTile.getEnteredSymbolsCount();				
																	
									// Update the DHD's renderer
									dhdTile.activateSymbol(message.objectID);
									
									// Limit not reached, activating in order
									//if ( gateTile.getMaxSymbols() > symbolCount ) {
									if ( (dhdTile.hasUpgrade() && symbolCount == 8) || (!dhdTile.hasUpgrade() && symbolCount == 7) || (symbolCount == 7 && symbol == EnumSymbol.ORIGIN) ) {
										AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.ACTIVATE_FINAL, gateTile), point );
										
										gateTile.addTask(new ScheduledTask(gateTile, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_CHEVRON_LOCK_DHD_SOUND));										
										gateTile.setRollPlayed();
									}
									
									else {
										AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.ACTIVATE_NEXT, gateTile), point );
									}
															
									boolean lock = symbol == EnumSymbol.ORIGIN;
									
									gateTile.sendSignal(null, "stargate_dhd_chevron_engaged", new Object[] { symbolCount, lock, symbol.name });
									
									// Light up target gate, if exists
									if (lock) {	
										attemptLightUp(world, gateTile);
									}
								} // add symbol if
							} // not brb else
						} // Not busy if
						
						else { 
							switch (gateTile.getStargateState()) {
								case COMPUTER_DIALING:
									player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.dhd_block.computer_dial")), true);
									break;
									
//								case FAILING:
//									player.sendStatusMessage(new TextComponentString(Aunis.proxy.localize("tile.aunis.dhd_block.failing_dial")), true);
//									break;
									
								default:
									break;
							}
						}
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
