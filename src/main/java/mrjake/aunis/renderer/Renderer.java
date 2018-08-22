package mrjake.aunis.renderer;

public interface Renderer<STATE extends RendererState> {
	
	public void render(double x, double y, double z, double partialTicks);
	
	public STATE getState();
	
	public void setState(STATE rendererState);
	
}
