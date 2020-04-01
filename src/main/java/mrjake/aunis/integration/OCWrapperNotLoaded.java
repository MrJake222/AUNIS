package mrjake.aunis.integration;

import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Node;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class OCWrapperNotLoaded implements OCWrapperInterface {

	@Override
	public void sendSignalToReachable(Node node, Context invoker, String name, Object... params) {}

	@Override
	public Node createNode(TileEntity tileEntity, String componentName) {
		return null;
	}
	
	@Override
	public void joinOrCreateNetwork(TileEntity tileEntity) {}
	
	@Override
	public boolean isModLoaded() {
		return false;
	}
	
	@Override
	public void sendWirelessPacketPlayer(EntityPlayer player, String address, short port, Object[] data) {}
}
