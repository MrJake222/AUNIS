package mrjake.aunis.packet.gate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.AunisSoundEvents;
import mrjake.aunis.packet.gate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.stargate.dhd.DHDTile;
import mrjake.aunis.stargate.sgbase.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// Send from server to clients nearby
public class GateRenderingUpdatePacketToClient implements IMessage {
	public GateRenderingUpdatePacketToClient() {}
	
	private int packetID;
	private int objectID;
	private BlockPos blockPos; 
	
	public GateRenderingUpdatePacketToClient(EnumPacket packet, int objectID, TileEntity dhdTile) {
		this(packet.packetID, objectID, dhdTile.getPos());
	}
	
	public GateRenderingUpdatePacketToClient(EnumPacket packet, EnumGateAction action, TileEntity te) {
		this(packet, action, te.getPos());
	}
	
	public GateRenderingUpdatePacketToClient(EnumPacket packet, EnumGateAction action, BlockPos pos) {
		this(packet.packetID, action.actionID, pos);
	}
	
	public GateRenderingUpdatePacketToClient(int packetID, int objectID, BlockPos pos) {
		this.packetID = packetID;
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

	
	public static class DHDActivateButtonHandler implements IMessageHandler<GateRenderingUpdatePacketToClient, IMessage>{
		
		@SuppressWarnings("incomplete-switch")
		@Override
		public IMessage onMessage(GateRenderingUpdatePacketToClient message, MessageContext ctx) {	
			EntityPlayer player = Minecraft.getMinecraft().player;
			World world = player.getEntityWorld();
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				TileEntity te = world.getTileEntity( message.blockPos );
				StargateBaseTile gateTile = null;
				DHDTile dhdTile = null;
				
				if ( te instanceof StargateBaseTile ) {
					gateTile = (StargateBaseTile) te;
				}
				
				else if  ( te instanceof DHDTile ) {
					dhdTile = (DHDTile) te;
				}
				
				else {
					// Invalid BlockPos
					return;
				}
				
				PositionedSoundRecord sound = AunisSoundEvents.ringRollSoundMap.get( message.blockPos );
				
				switch ( EnumPacket.valueOf(message.packetID) ) {
					case DHD_RENDERER_UPDATE:						
						if (message.objectID == -1) 
							dhdTile.getRenderer().clearButtons();
						
						else 
							dhdTile.getRenderer().activateButton(message.objectID);
						
						break;
						
					case GATE_RENDERER_UPDATE:
						gateRendererUpdate(EnumGateAction.valueOf(message.objectID), gateTile);
						
						break;
						
					case PLAY_ROLL_SOUND:						
						if ( sound == null ) {
							sound = new PositionedSoundRecord(AunisSoundEvents.ringRoll, SoundCategory.BLOCKS, 1.0f, 1.0f, message.blockPos);
							AunisSoundEvents.ringRollSoundMap.put(message.blockPos, sound);
						}
						
						Minecraft.getMinecraft().getSoundHandler().playSound( sound );
						
						break;
						
					case STOP_ROLL_SOUND:						
						if ( sound != null ) {
							Minecraft.getMinecraft().getSoundHandler().stopSound( sound );
						}
						
						break;
				}
			});
			
			return null;
		}
		
		@SuppressWarnings("incomplete-switch")
		private void gateRendererUpdate(EnumGateAction action, StargateBaseTile gateTile) {
			switch ( action ) {
				case ACTIVATE_NEXT:
					gateTile.getRenderer().activateNextChevron();
					break;
				
				case ACTIVATE_FINAL:
					gateTile.getRenderer().activateFinalChevron();
					break;
					
				case OPEN_GATE:
					gateTile.getRenderer().openGate();
					break;
					
				case CLOSE_GATE:
					gateTile.getRenderer().closeGate();
					break;
			}
		}
	}
}