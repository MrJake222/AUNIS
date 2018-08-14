package mrjake.aunis.packet.gate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.gate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.stargate.dhd.DHDTile;
import mrjake.aunis.stargate.sgbase.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
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
		
		@Override
		public IMessage onMessage(GateRenderingUpdatePacketToClient message, MessageContext ctx) {	
			EntityPlayer player = Minecraft.getMinecraft().player;
			World world = player.getEntityWorld();
			
			
			
			if ( message.packetID == EnumPacket.DHDButton.packetID ) {
				DHDTile te = (DHDTile) world.getTileEntity(message.blockPos);
				
				Minecraft.getMinecraft().addScheduledTask(() -> {
					
					if (message.objectID == -1) {
						te.getRenderer().clearButtons();
					}
					
					else {
						te.getRenderer().activateButton(message.objectID);
					}
				});
			}
			
			else if ( message.packetID == EnumPacket.Chevron.packetID ) {
				StargateBaseTile te = (StargateBaseTile) world.getTileEntity(message.blockPos);
				
				if (te != null) {
					Minecraft.getMinecraft().addScheduledTask(() -> {
						
						if ( message.objectID == EnumGateAction.ACTIVATE_NEXT.actionID) {
							te.getRenderer().activateNextChevron();
						}
						
						else if ( message.objectID == EnumGateAction.ACTIVATE_FINAL.actionID) {
							te.getRenderer().activateFinalChevron();
						}
						
						/*else if (message.objectID == EnumGateAction.CLEAR.actionID) {
							te.getRenderer().clearChevrons();
						}*/
						
						else if (message.objectID == EnumGateAction.OPEN_GATE.actionID) {
							te.getRenderer().openGate();
						}
						
						else if (message.objectID == EnumGateAction.CLOSE_GATE.actionID) {
							te.getRenderer().closeGate();
						}
					});
				}
			}
			
			return null;
		}
	}
}