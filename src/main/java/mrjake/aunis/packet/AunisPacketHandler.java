package mrjake.aunis.packet;

import mrjake.aunis.packet.dhd.renderingUpdate.ClearLinkedDHDButtons;
import mrjake.aunis.packet.dhd.renderingUpdate.ClearLinkedDHDButtons.ClearLinkedDHDButtonsHandler;
import mrjake.aunis.packet.dhd.renderingUpdate.DHDIncomingWormholePacketToClient;
import mrjake.aunis.packet.dhd.renderingUpdate.DHDIncomingWormholePacketToClient.DHDIncomingWormholePacketToClientHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToClient;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToClient.GateRenderingUpdatePacketToClientHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer.GateRenderingUpdatePacketToServerHandler;
import mrjake.aunis.packet.gate.teleportPlayer.MotionToServer;
import mrjake.aunis.packet.gate.teleportPlayer.MotionToServer.MotionServerHandler;
import mrjake.aunis.packet.gate.teleportPlayer.RetrieveMotionToClient;
import mrjake.aunis.packet.gate.teleportPlayer.RetrieveMotionToClient.RetrieveMotionClientHandler;
import mrjake.aunis.packet.infuser.EnergyStoredToClient;
import mrjake.aunis.packet.infuser.EnergyStoredToClient.EnergyStorageToClientHandler;
import mrjake.aunis.packet.infuser.ShouldRenderWavesToClient;
import mrjake.aunis.packet.infuser.ShouldRenderWavesToClient.ShouldRenderWavesToClientHandler;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.packet.state.StateUpdatePacketToClient.StateUpdateClientHandler;
import mrjake.aunis.packet.state.StateUpdateRequestToServer;
import mrjake.aunis.packet.state.StateUpdateRequestToServer.StateUpdateServerHandler;
import mrjake.aunis.packet.transportrings.SaveRingsParametersToServer;
import mrjake.aunis.packet.transportrings.SaveRingsParametersToServer.SaveRingsParametersServerHandler;
import mrjake.aunis.packet.transportrings.StartPlayerFadeOutToClient;
import mrjake.aunis.packet.transportrings.StartPlayerFadeOutToClient.StartPlayerFadeOutToClientHandler;
import mrjake.aunis.packet.transportrings.StartRingsAnimationToClient;
import mrjake.aunis.packet.transportrings.StartRingsAnimationToClient.StartRingsAnimationToClientHandler;
import mrjake.aunis.packet.transportrings.TRControllerActivatedToServer;
import mrjake.aunis.packet.transportrings.TRControllerActivatedToServer.TRControllerActivatedServerHandler;
import mrjake.aunis.packet.update.renderer.RendererUpdatePacketToClient;
import mrjake.aunis.packet.update.renderer.RendererUpdatePacketToClient.TileUpdateClientHandler;
import mrjake.aunis.packet.update.renderer.RendererUpdateRequestToServer;
import mrjake.aunis.packet.update.renderer.RendererUpdateRequestToServer.TileUpdateServerHandler;
import mrjake.aunis.packet.upgrade.UpgradeSlotInteractToClient;
import mrjake.aunis.packet.upgrade.UpgradeSlotInteractToClient.UpgradeSlotInteractHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AunisPacketHandler {
	public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("aunis");
	
	public static void registerPackets() {
		int id = 0;
		
		INSTANCE.registerMessage(GateRenderingUpdatePacketToServerHandler.class, GateRenderingUpdatePacketToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(GateRenderingUpdatePacketToClientHandler.class, GateRenderingUpdatePacketToClient.class, id, Side.CLIENT); id++;
		
		/*INSTANCE.registerMessage(OnLoadUpdateRequest.onLoadUpdateRequestToClientHandler.class, OnLoadUpdateRequest.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(OnLoadUpdateRequest.onLoadUpdateRequestToClientHandler.class, OnLoadUpdateRequest.class, id, Side.SERVER); id++;*/
		
		INSTANCE.registerMessage(DHDIncomingWormholePacketToClientHandler.class, DHDIncomingWormholePacketToClient.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(RetrieveMotionClientHandler.class, RetrieveMotionToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(MotionServerHandler.class, MotionToServer.class, id, Side.SERVER); id++;
		
//		INSTANCE.registerMessage(StateUpdateToServer.StateUpdateServerHandler.class, StateUpdateToServer.class, id, Side.SERVER); id++;
		
		INSTANCE.registerMessage(TileUpdateServerHandler.class, RendererUpdateRequestToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(TileUpdateClientHandler.class, RendererUpdatePacketToClient.class, id, Side.CLIENT); id++;
		
//		INSTANCE.registerMessage(ClearLinkedDHDButtons.ClearLinkedDHDButtonsHandler.class, ClearLinkedDHDButtons.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(ClearLinkedDHDButtonsHandler.class, ClearLinkedDHDButtons.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(UpgradeSlotInteractHandler.class, UpgradeSlotInteractToClient.class, id, Side.CLIENT); id++;
//		INSTANCE.registerMessage(UpgradeTileUpdateToServer.UpgradeTileUpdateHandler.class, UpgradeTileUpdateToServer.class, id, Side.SERVER); id++;
				
		INSTANCE.registerMessage(EnergyStorageToClientHandler.class, EnergyStoredToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(ShouldRenderWavesToClientHandler.class, ShouldRenderWavesToClient.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(StartRingsAnimationToClientHandler.class, StartRingsAnimationToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(StartPlayerFadeOutToClientHandler.class, StartPlayerFadeOutToClient.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(StateUpdateServerHandler.class, StateUpdateRequestToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(StateUpdateClientHandler.class, StateUpdatePacketToClient.class, id, Side.CLIENT); id++;
		
		INSTANCE.registerMessage(SaveRingsParametersServerHandler.class, SaveRingsParametersToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(TRControllerActivatedServerHandler.class, TRControllerActivatedToServer.class, id, Side.SERVER); id++;
	}
}
