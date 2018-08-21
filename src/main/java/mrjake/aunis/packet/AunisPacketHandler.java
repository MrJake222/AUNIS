package mrjake.aunis.packet;

import mrjake.aunis.packet.gate.addressUpdate.GateAddressPacketToClient;
import mrjake.aunis.packet.gate.addressUpdate.GateAddressRequestToServer;
import mrjake.aunis.packet.gate.onLoadUpdate.OnLoadUpdateRequest;
import mrjake.aunis.packet.gate.renderingUpdate.DHDIncomingWormholePacketToClient;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToClient;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.gate.teleportPlayer.MotionToServer;
import mrjake.aunis.packet.gate.teleportPlayer.PlayWormholeSoundPacketToClient;
import mrjake.aunis.packet.gate.teleportPlayer.RetrieveMotionToClient;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdatePacketToClient;
import mrjake.aunis.packet.gate.tileUpdate.TileUpdateRequestToServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AunisPacketHandler {
	public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("aunis");
	
	public static void registerPackets() {
		int id = 0;
		
		INSTANCE.registerMessage(GateRenderingUpdatePacketToServer.GateRenderingUpdatePacketToServerHandler.class, GateRenderingUpdatePacketToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(GateRenderingUpdatePacketToClient.GateRenderingUpdatePacketToClientHandler.class, GateRenderingUpdatePacketToClient.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(OnLoadUpdateRequest.onLoadUpdateRequestToClientHandler.class, OnLoadUpdateRequest.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(OnLoadUpdateRequest.onLoadUpdateRequestToClientHandler.class, OnLoadUpdateRequest.class, id, Side.SERVER); id++;
		
		INSTANCE.registerMessage(GateAddressRequestToServer.GateAddressRequestToServerHandler.class, GateAddressRequestToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(GateAddressPacketToClient.GateAddressPacketToClientHandler.class, GateAddressPacketToClient.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(DHDIncomingWormholePacketToClient.DHDIncomingWormholePacketToClientHandler.class, DHDIncomingWormholePacketToClient.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(RetrieveMotionToClient.RetrieveMotionClientHandler.class, RetrieveMotionToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(MotionToServer.MotionServerHandler.class, MotionToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(PlayWormholeSoundPacketToClient.PlayWormholeSoundClientHandler.class, PlayWormholeSoundPacketToClient.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(TileUpdateRequestToServer.TileUpdateServerHandler.class, TileUpdateRequestToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(TileUpdatePacketToClient.TileUpdateClientHandler.class, TileUpdatePacketToClient.class, id, Side.CLIENT); id++;
	}
}
