package mrjake.aunis.packet.gate.renderingUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.stargate.StargateRenderer;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// Send from server to clients nearby
public class GateRenderingUpdatePacketToClient extends PositionedPacket {
	public GateRenderingUpdatePacketToClient() {}
	
	private int packetID;
	private int objectID;
	
	public GateRenderingUpdatePacketToClient(EnumPacket packet, EnumSymbol symbol, BlockPos pos) {
		this(packet.packetID, symbol.id, pos);
	}
	
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
		super(pos);
		
		this.packetID = packetID;
		this.objectID = objectID;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(packetID);
		buf.writeInt(objectID);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		packetID = buf.readInt();
		objectID = buf.readInt();
	}

	
	public static class GateRenderingUpdatePacketToClientHandler implements IMessageHandler<GateRenderingUpdatePacketToClient, IMessage>{
		
		@Override
		public IMessage onMessage(GateRenderingUpdatePacketToClient message, MessageContext ctx) {	
			EntityPlayer player = Minecraft.getMinecraft().player;
			World world = player.getEntityWorld();
						
			Minecraft.getMinecraft().addScheduledTask(() -> {
				TileEntity te = world.getTileEntity( message.pos );
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
				
				switch ( EnumPacket.valueOf(message.packetID) ) {
					case DHD_RENDERER_UPDATE:
						DHDRenderer dhdRenderer = dhdTile.getDHDRenderer();
						
						if (message.objectID == -1) 
							dhdRenderer.clearButtons();
						
						else {
							if (message.objectID == EnumSymbol.BRB.id)
								dhdRenderer.brbToActivate = true;
							
							dhdRenderer.activateButton(message.objectID);
						}
						
						break;
						
					case GATE_RENDERER_UPDATE:
						gateRendererUpdate(EnumGateAction.valueOf(message.objectID), gateTile);
						
						break;
				}
			});
			
			return null;
		}
		
		@SuppressWarnings("incomplete-switch")
		private void gateRendererUpdate(EnumGateAction action, StargateBaseTile gateTile) {
			StargateRenderer renderer = (StargateRenderer) gateTile.getRenderer();
			
			switch ( action ) {
				case ACTIVATE_NEXT:
					renderer.activateNextChevron();
					break;
				
				case ACTIVATE_FINAL:
					renderer.activateFinalChevron();
					break;
					
				case OPEN_GATE:
					renderer.openGate();
					break;
					
				case CLOSE_GATE:
					renderer.closeGate();
					break;
					
				case GATE_DIAL_FAILED:
					renderer.setRingSpin(false, false);
					renderer.clearChevrons();
					break;
					
				case LIGHT_UP_7_CHEVRONS:
					renderer.lightUpChevrons( 7 );
					break;
					
				case LIGHT_UP_8_CHEVRONS:
					renderer.lightUpChevrons( 8 );
					break;
					
				case UNSTABLE_HORIZON:
					renderer.unstableHorizon(true);
					break;
					
				case STABLE_HORIZON:
					renderer.unstableHorizon(false);
					break;
			}
		}
	}
}