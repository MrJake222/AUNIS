package mrjake.aunis.packet;

import mrjake.aunis.packet.gate.GateRenderingUpdatePacketToClient;
import mrjake.aunis.packet.gate.GateRenderingUpdatePacketToServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AunisPacketHandler {
	public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("aunis");
	
	public static void registerPackets() {
		int id = 0;
		
		INSTANCE.registerMessage(GateRenderingUpdatePacketToServer.DHDButtonClickedHandler.class, GateRenderingUpdatePacketToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(GateRenderingUpdatePacketToClient.DHDActivateButtonHandler.class, GateRenderingUpdatePacketToClient.class, id, Side.CLIENT); id++;
	}
}
