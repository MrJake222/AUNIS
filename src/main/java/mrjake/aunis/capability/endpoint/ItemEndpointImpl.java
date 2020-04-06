package mrjake.aunis.capability.endpoint;

import mrjake.aunis.Aunis;

public class ItemEndpointImpl implements ItemEndpointInterface {

	private Object endpoint;
	private long endpointCreated;
	
	@Override
	public boolean hasEndpoint() {
		return endpoint != null;
	}

	@Override
	public Object getEndpoint() {
		return endpoint;
	}
	
	@Override
	public void setEndpoint(Object endpoint, long endpointCreated) {
		this.endpoint = endpoint;
		this.endpointCreated = endpointCreated;
	}
	
	@Override
	public void removeEndpoint() {
		if (hasEndpoint()) {
			Aunis.ocWrapper.leaveWirelessNetwork(endpoint);
			endpoint = null;
		
//			Aunis.info("removed endpoint");
		}
	}
	
	@Override
	public void updateEndpoint() {
		if (hasEndpoint()) {
			Aunis.ocWrapper.updateWirelessNetwork(endpoint);
//			Aunis.info("updated endpoint");
		}
	}
	
	@Override
	public void checkAndUpdateEndpoint(long totalWorldTime) {
		if (hasEndpoint()) {
			if (totalWorldTime - endpointCreated > 20)
				removeEndpoint();
			else
				updateEndpoint();
		}
	}
	
	@Override
	public void resetEndpointCounter(long totalWorldTime) {
		endpointCreated = totalWorldTime;
	}
}
