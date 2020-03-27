package mrjake.aunis.renderer.stargate;

public abstract class StargateClassicRenderer<S extends StargateClassicRendererState> extends StargateAbstractRenderer<S> {
	
	// ----------------------------------------------------------------------------------------
	// Chevrons
	
	protected abstract void renderChevron(S rendererState, double partialTicks, ChevronEnum chevron);
	
	protected void renderChevrons(S rendererState, double partialTicks) {
		for (ChevronEnum chevron : ChevronEnum.values())
			renderChevron(rendererState, partialTicks, chevron);
		
		rendererState.chevronTextureList.iterate(getWorld(), partialTicks);
	}
}
