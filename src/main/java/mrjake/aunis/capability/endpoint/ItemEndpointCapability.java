package mrjake.aunis.capability.endpoint;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * This capability enables items to be used as OpenComputers
 * Wireless Endpoints.
 * 
 * @author MrJake222
 * 
 */
public class ItemEndpointCapability {
	
	@CapabilityInject(ItemEndpointInterface.class)
	public static Capability<ItemEndpointInterface> ENDPOINT_CAPABILITY;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(ItemEndpointInterface.class, new ItemEndpointStorage(), ItemEndpointImpl::new);
	}
}
