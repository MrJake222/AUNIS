package mrjake.aunis.integration;

import javax.annotation.Nullable;

import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Network;
import li.cil.oc.api.network.Node;
import net.minecraft.tileentity.TileEntity;

public interface OCWrapperInterface {
	
	/**
	 * Sends signal to all computers in the network
	 * 
	 * @param node - sender node
	 * @param context 
	 * @param name - name of the signal
	 * @param params - params of the signal
	 */
	public void sendSignalToReachable(Node node, Context invoker, String name, Object... params);
	
	/**
	 * Creates a new Node when OpenComputers is loaded.
	 * Otherwise returns null.
	 * 
	 * @param tileEntity {@link TileEntity} instance of the parent block.
	 * @param componentName {@link String} representing the component.
	 * @return {@link Node} or null.
	 */
	@Nullable
	public Node createNode(TileEntity tileEntity, String componentName);

	/**
	 * Joins the {@link TileEntity} to the {@link Network}.
	 * 
	 * @param tileEntity {@link TileEntity} to be linked.
	 */
	public void joinOrCreateNetwork(TileEntity tileEntity);
}
