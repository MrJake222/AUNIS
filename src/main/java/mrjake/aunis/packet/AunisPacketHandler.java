package mrjake.aunis.packet;

import mrjake.aunis.item.dialer.UniverseDialerActionPacketToServer;
import mrjake.aunis.item.dialer.UniverseDialerActionPacketToServer.UniverseDialerActionPacketServerHandler;
import mrjake.aunis.item.dialer.UniverseDialerOCProgramToServer.UniverseDialerOCProgramServerHandler;
import mrjake.aunis.item.notebook.NotebookActionPacketToServer;
import mrjake.aunis.item.notebook.NotebookActionPacketToServer.NotebookActionPacketServerHandler;
import mrjake.aunis.item.notebook.PageNotebookSetNameToServer.PageNotebookSetNameServerHandler;
import mrjake.aunis.item.notebook.PageNotebookSetNameToServer;
import mrjake.aunis.item.dialer.UniverseDialerOCProgramToServer;
import mrjake.aunis.packet.BeamerChangeRoleToServer.BeamerChangeRoleServerHandler;
import mrjake.aunis.packet.BeamerChangedInactivityToServer.BeamerChangedInactivityServerHandler;
import mrjake.aunis.packet.BeamerChangedLevelsToServer.BeamerChangedLevelsServerHandler;
import mrjake.aunis.packet.ChangeRedstoneModeToServer.ChangeRedstoneModeServerHandler;
import mrjake.aunis.packet.SetOpenTabToServer.SetOpenTabServerHandler;
import mrjake.aunis.packet.SoundPositionedPlayToClient.PlayPositionedSoundClientHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient.StateUpdateClientHandler;
import mrjake.aunis.packet.StateUpdateRequestToServer.StateUpdateServerHandler;
import mrjake.aunis.packet.gui.address.AddressActionToServer;
import mrjake.aunis.packet.gui.address.AddressActionToServer.AddressActionServerHandler;
import mrjake.aunis.packet.stargate.DHDButtonClickedToServer;
import mrjake.aunis.packet.stargate.DHDButtonClickedToServer.DHDButtonClickedServerHandler;
import mrjake.aunis.packet.stargate.StargateMotionToClient;
import mrjake.aunis.packet.stargate.StargateMotionToServer;
import mrjake.aunis.packet.stargate.StargateMotionToClient.RetrieveMotionClientHandler;
import mrjake.aunis.packet.stargate.StargateMotionToServer.MotionServerHandler;
import mrjake.aunis.packet.transportrings.SaveRingsParametersToServer;
import mrjake.aunis.packet.transportrings.SaveRingsParametersToServer.SaveRingsParametersServerHandler;
import mrjake.aunis.packet.transportrings.StartPlayerFadeOutToClient;
import mrjake.aunis.packet.transportrings.StartPlayerFadeOutToClient.StartPlayerFadeOutToClientHandler;
import mrjake.aunis.packet.transportrings.TRControllerActivatedToServer;
import mrjake.aunis.packet.transportrings.TRControllerActivatedToServer.TRControllerActivatedServerHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AunisPacketHandler {
	public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("aunis");
	
	private static int id = 0;
	
	public static void registerPackets() {		
		INSTANCE.registerMessage(DHDButtonClickedServerHandler.class, DHDButtonClickedToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(MotionServerHandler.class, StargateMotionToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(StateUpdateServerHandler.class, StateUpdateRequestToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(SaveRingsParametersServerHandler.class, SaveRingsParametersToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(TRControllerActivatedServerHandler.class, TRControllerActivatedToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(SetOpenTabServerHandler.class, SetOpenTabToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(UniverseDialerActionPacketServerHandler.class, UniverseDialerActionPacketToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(UniverseDialerOCProgramServerHandler.class, UniverseDialerOCProgramToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(BeamerChangeRoleServerHandler.class, BeamerChangeRoleToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(ChangeRedstoneModeServerHandler.class, ChangeRedstoneModeToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(BeamerChangedLevelsServerHandler.class, BeamerChangedLevelsToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(BeamerChangedInactivityServerHandler.class, BeamerChangedInactivityToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(NotebookActionPacketServerHandler.class, NotebookActionPacketToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(PageNotebookSetNameServerHandler.class, PageNotebookSetNameToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(AddressActionServerHandler.class, AddressActionToServer.class, id, Side.SERVER); id++;

		
		INSTANCE.registerMessage(RetrieveMotionClientHandler.class, StargateMotionToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(StartPlayerFadeOutToClientHandler.class, StartPlayerFadeOutToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(StateUpdateClientHandler.class, StateUpdatePacketToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(PlayPositionedSoundClientHandler.class, SoundPositionedPlayToClient.class, id, Side.CLIENT); id++;
	}
}
