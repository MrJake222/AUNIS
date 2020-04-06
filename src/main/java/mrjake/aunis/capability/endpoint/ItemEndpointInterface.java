package mrjake.aunis.capability.endpoint;

public interface ItemEndpointInterface {

	public boolean hasEndpoint();
	public Object getEndpoint();
	public void setEndpoint(Object endpoint, long endpointCreated);
	public void removeEndpoint();
	public void updateEndpoint();
	public void checkAndUpdateEndpoint(long totalWorldTime);
	public void resetEndpointCounter(long totalWorldTime);
}
