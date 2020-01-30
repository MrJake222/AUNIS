package mrjake.aunis.packet;

import mrjake.aunis.packet.SoundPositionedPlayToClient.PlayPositionedSoundClientHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient.StateUpdateClientHandler;
import mrjake.aunis.packet.StateUpdateRequestToServer.StateUpdateServerHandler;
import mrjake.aunis.packet.stargate.StargateMotionToClient;
import mrjake.aunis.packet.stargate.StargateMotionToServer;
import mrjake.aunis.packet.stargate.StargateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.stargate.StargateMotionToClient.RetrieveMotionClientHandler;
import mrjake.aunis.packet.stargate.StargateMotionToServer.MotionServerHandler;
import mrjake.aunis.packet.stargate.StargateRenderingUpdatePacketToServer.GateRenderingUpdatePacketToServerHandler;
import mrjake.aunis.packet.transportrings.SaveRingsParametersToServer;
import mrjake.aunis.packet.transportrings.SaveRingsParametersToServer.SaveRingsParametersServerHandler;
import mrjake.aunis.packet.transportrings.StartPlayerFadeOutToClient;
import mrjake.aunis.packet.transportrings.StartPlayerFadeOutToClient.StartPlayerFadeOutToClientHandler;
import mrjake.aunis.packet.transportrings.TRControllerActivatedToServer;
import mrjake.aunis.packet.transportrings.TRControllerActivatedToServer.TRControllerActivatedServerHandler;
import mrjake.aunis.packet.upgrade.UpgradeSlotInteractToClient;
import mrjake.aunis.packet.upgrade.UpgradeSlotInteractToClient.UpgradeSlotInteractHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AunisPacketHandler {
	public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("aunis");
	
	private static int id = 0;
	
	public static void registerPackets() {		
		INSTANCE.registerMessage(GateRenderingUpdatePacketToServerHandler.class, StargateRenderingUpdatePacketToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(MotionServerHandler.class, StargateMotionToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(StateUpdateServerHandler.class, StateUpdateRequestToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(SaveRingsParametersServerHandler.class, SaveRingsParametersToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(TRControllerActivatedServerHandler.class, TRControllerActivatedToServer.class, id, Side.SERVER); id++;

		
		INSTANCE.registerMessage(RetrieveMotionClientHandler.class, StargateMotionToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(UpgradeSlotInteractHandler.class, UpgradeSlotInteractToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(StartPlayerFadeOutToClientHandler.class, StartPlayerFadeOutToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(StateUpdateClientHandler.class, StateUpdatePacketToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(PlayPositionedSoundClientHandler.class, SoundPositionedPlayToClient.class, id, Side.CLIENT); id++;
	}
}
