package mrjake.aunis.renderer.stargate;

import net.minecraft.util.ResourceLocation;

public class StargateUniverseRendererState extends StargateClassicRendererState {
	public StargateUniverseRendererState() {}
	
	
	
	public StargateUniverseRendererState(StargateUniverseRendererStateBuilder builder) {
		super(builder);
	}

	@Override
	public ResourceLocation getEventHorizonTexture() {
		return HORIZON_UNSTABLE_TEXTURE;
	}
	
	@Override
	protected String getChevronTextureBase() {
		return "textures/tesr/universe/universe_chevron";
	}

	
	// ------------------------------------------------------------------------
	// Builder
	
	public static StargateUniverseRendererStateBuilder builder() {
		return new StargateUniverseRendererStateBuilder();
	}
	
	public static class StargateUniverseRendererStateBuilder extends StargateClassicRendererStateBuilder {
		public StargateUniverseRendererStateBuilder() {}
		
		public StargateUniverseRendererStateBuilder(StargateClassicRendererStateBuilder superBuilder) {
			super(superBuilder);
			setActiveChevrons(superBuilder.activeChevrons);
			setFinalActive(superBuilder.isFinalActive);
		}
		
		@Override
		public StargateAbstractRendererState build() {
			return new StargateUniverseRendererState(this);
		}
	}
}
