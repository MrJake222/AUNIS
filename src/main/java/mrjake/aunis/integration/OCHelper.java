package mrjake.aunis.integration;

import li.cil.oc.api.machine.Context;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.network.Node;

public class OCHelper {
	
	/**
	 * Sends signal to all computers in the network
	 * 
	 * @param node - sender node
	 * @param context 
	 * @param name - name of the signal
	 * @param params - params of the signal
	 */
	public static void sendSignalToReachable(Node node, Context invoker, String name, Object... params) {
		for (Node targetNode : node.reachableNodes()) {
			
			if (targetNode.host() instanceof Machine) {
				Machine machine = (Machine) targetNode.host();
				
				// If the receiving machine was a sender 
				boolean caller = machine == invoker;
				
				Object[] array = new Object[params.length + 2];
				array[0] = node.address();
				array[1] = caller;
				
				for (int i=0; i<params.length; i++)
					array[i+2] = params[i];
				
				machine.signal(name, array);
			}
		}
	}
}
