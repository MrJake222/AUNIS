package mrjake.aunis.packet.gate.onLoadUpdate;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.stargate.dhd.DHDTile;
import mrjake.aunis.stargate.sgbase.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class OnLoadUpdateRequest implements IMessage {
	public OnLoadUpdateRequest() {}
	
	private int senderID;
	private boolean isSender;
	private List<BlockPos> stargatesToUpdate;
	private List<BlockPos> dhdsToUpdate;
	
	private List<StargateUpdatePacket> stargateUpdatePackets;
	private List<DHDUpdatePacket> dhdUpdatePackets;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public OnLoadUpdateRequest(int senderID, boolean isSender, List sg, List dhd) {
		this.senderID = senderID;
		
		this.isSender = isSender;
		
		if (isSender) {
			this.stargatesToUpdate = sg;
			this.dhdsToUpdate = dhd;
		}
		
		else {
			this.stargateUpdatePackets = sg;
			this.dhdUpdatePackets = dhd;
		}
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(senderID);
		buf.writeBoolean( isSender );
		
		if (isSender) {
			buf.writeInt( stargatesToUpdate.size() );
			for (BlockPos sg : stargatesToUpdate)
				buf.writeLong( sg.toLong() );
			
			buf.writeInt( dhdsToUpdate.size() );
			for (BlockPos dhd : dhdsToUpdate)
				buf.writeLong( dhd.toLong() );
		}
		
		else {
			buf.writeInt( stargateUpdatePackets.size() );
			for (StargateUpdatePacket sg : stargateUpdatePackets)
				sg.toBytes(buf);
			
			buf.writeInt( dhdUpdatePackets.size() );
			for (DHDUpdatePacket dhd : dhdUpdatePackets)
				dhd.toBytes(buf);
		}
		
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		senderID = buf.readInt();
		isSender = buf.readBoolean();
		
		if (isSender) {
			stargatesToUpdate = new ArrayList<BlockPos>();
			dhdsToUpdate = new ArrayList<BlockPos>();
			
			int len = buf.readInt();
			for (int i=0; i<len; i++) 
				stargatesToUpdate.add( BlockPos.fromLong( buf.readLong() ) );
			
			len = buf.readInt();
			for (int i=0; i<len; i++) 
				dhdsToUpdate.add( BlockPos.fromLong( buf.readLong() ) );
		}
		
		else {
			stargateUpdatePackets = new ArrayList<StargateUpdatePacket>();
			dhdUpdatePackets = new ArrayList<DHDUpdatePacket>();
			
			int len = buf.readInt();
			for (int i=0; i<len; i++) 
				stargateUpdatePackets.add( new StargateUpdatePacket(buf) );
			
			len = buf.readInt();
			for (int i=0; i<len; i++) 
				dhdUpdatePackets.add( new DHDUpdatePacket(buf) );
		}
		
	}
	public static class onLoadUpdateRequestToClientHandler implements IMessageHandler<OnLoadUpdateRequest, IMessage> {

		@Override
		public IMessage onMessage(OnLoadUpdateRequest message, MessageContext ctx) {		
			if ( ctx.side == Side.CLIENT ) {
				EntityPlayer player = Minecraft.getMinecraft().player;
				World world = player.getEntityWorld();
				
				// Aunis.info("Received onLoadUpdateRequestToClient on client on player "+player.toString()+",  isSender: "+message.isSender+",  senderID: "+message.senderID);
			
				Minecraft.getMinecraft().addScheduledTask(() -> {
					if (message.isSender) {
						List<StargateUpdatePacket> stargateUpdatePackets = new ArrayList<StargateUpdatePacket>();
						List<DHDUpdatePacket> dhdUpdatePackets = new ArrayList<DHDUpdatePacket>();
						
						/*Aunis.info("stargatesToUpdate: " + message.stargatesToUpdate);
						Aunis.info("dhdsToUpdate: " + message.dhdsToUpdate);*/
						
						for ( BlockPos sg : message.stargatesToUpdate ) {
							if ( world.isBlockLoaded(sg) ) {
								
								TileEntity te = world.getTileEntity(sg);
								StargateBaseTile gateTile;
								
								if ( te instanceof StargateBaseTile )
									gateTile = (StargateBaseTile) te;
								else
									continue;
								
								stargateUpdatePackets.add( new StargateUpdatePacket( gateTile ) );
							}
						}
						
						for ( BlockPos dhd : message.dhdsToUpdate ) {
							if ( world.isBlockLoaded(dhd) ) {
								
								// Aunis.info("DHD at " + dhd);
								
								TileEntity te = world.getTileEntity(dhd);
								DHDTile dhdTile;
								
								if ( te instanceof DHDTile )
									dhdTile = (DHDTile) te;
								else
									continue;
								
								
								
								dhdUpdatePackets.add( new DHDUpdatePacket( dhdTile ) );
							}
						}
						
						// EntityOtherPlayerMP sender = (EntityOtherPlayerMP) world.getEntityByID(message.senderID);
						// AunisPacketHandler.INSTANCE.sendTo( new onLoadUpdateRequestToClient(0, false, stargateUpdatePackets, dhdUpdatePackets),  sender); 
						AunisPacketHandler.INSTANCE.sendToServer( new OnLoadUpdateRequest(message.senderID, false, stargateUpdatePackets, dhdUpdatePackets) ); 
						// return new onLoadUpdateRequestToClient(false, stargateUpdatePackets, dhdUpdatePackets);
					}
					
					else {
						// Aunis.info("message.stargateUpdatePackets: " + message.stargateUpdatePackets);
						// Aunis.info("message.dhdUpdatePackets: " + message.dhdUpdatePackets);
						
						for ( StargateUpdatePacket sgPacket : message.stargateUpdatePackets ) {
							BlockPos pos = sgPacket.pos;
							
							if ( world.isBlockLoaded(pos) ) {
								
								TileEntity te = world.getTileEntity(pos);
								StargateBaseTile gateTile;
								
								if ( te instanceof StargateBaseTile )
									gateTile = (StargateBaseTile) te;
								else
									continue;
							
								sgPacket.set( gateTile );
							}
						}
						
						for ( DHDUpdatePacket dhdPacket : message.dhdUpdatePackets ) {
							BlockPos pos = dhdPacket.pos;
							
							if ( world.isBlockLoaded(pos) ) {
								
								TileEntity te = world.getTileEntity(pos);
								DHDTile dhdTile;
								
								if ( te instanceof DHDTile )
									dhdTile = (DHDTile) te;
								else
									continue;
							
								dhdPacket.set( dhdTile );
							}
						}
					}
					
				});
			}
			
			// Server side
			else {
				EntityPlayer player = ctx.getServerHandler().player;
				World world = player.getEntityWorld();
				
				EntityPlayerMP target = (EntityPlayerMP) world.getEntityByID(message.senderID);
				
				// Aunis.info("Received message on server, passing to "+target.toString());
				
				AunisPacketHandler.INSTANCE.sendTo( new OnLoadUpdateRequest(0, false, message.stargateUpdatePackets, message.dhdUpdatePackets), target); 

			}
			
			return null;
		}
	}
}
