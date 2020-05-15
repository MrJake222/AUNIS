package mrjake.aunis.renderer.stargate;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.loader.texture.Texture;
import mrjake.aunis.loader.texture.TextureLoader;
import mrjake.aunis.renderer.BlockRenderer;
import mrjake.aunis.renderer.stargate.StargateRendererStatic.QuadStrip;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public abstract class StargateAbstractRenderer<S extends StargateAbstractRendererState> extends TileEntitySpecialRenderer<StargateAbstractBaseTile> {
	
	// ---------------------------------------------------------------------------------------
	// Render
	
	@Override
	public void render(StargateAbstractBaseTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		@SuppressWarnings("unchecked")
		S rendererState = (S) te.getRendererStateClient();
		
		if (rendererState != null) {		
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			
			if (shouldRender(rendererState)) {				
				if (AunisConfig.debugConfig.renderBoundingBoxes || AunisConfig.debugConfig.renderWholeKawooshBoundingBox) {
					te.getEventHorizonLocalBox().render();
					
					int segments = AunisConfig.debugConfig.renderWholeKawooshBoundingBox ? te.getLocalKillingBoxes().size() : rendererState.horizonSegments;
	
					for (int i=0; i<segments; i++) {
						te.getLocalKillingBoxes().get(i).render();
					}
								
					for (AunisAxisAlignedBB b : te.getLocalInnerBlockBoxes())
						b.render();
					
					te.getRenderBoundingBoxForDisplay().render();
				}
								
	            applyTransformations(rendererState);
	            GlStateManager.disableRescaleNormal();
				applyLightMap(rendererState, partialTicks);
				
				renderGate(rendererState, partialTicks);
				
				if (rendererState.doEventHorizonRender)
					renderKawoosh(rendererState, partialTicks);
			}
			
			else {
				bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				
				GlStateManager.enableBlend();
	            GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
	            GL14.glBlendColor(0, 0, 0, 0.7f);
				
				Minecraft.getMinecraft().entityRenderer.disableLightmap();
				
				for (Map.Entry<BlockPos, IBlockState> entry : getMemberBlockStates(te.getMergeHelper(), rendererState.facing).entrySet()) {				
					BlockPos pos = entry.getKey().rotate(FacingToRotation.get(rendererState.facing));
					
					if (getWorld().isAirBlock(pos.add(rendererState.pos)))
						BlockRenderer.render(getWorld(), pos, entry.getValue());
				}
				
				Minecraft.getMinecraft().entityRenderer.enableLightmap();
				GlStateManager.disableBlend();
			}
			
			GlStateManager.popMatrix();	
		}
	}
	
	protected boolean shouldRender(S rendererState) {
		IBlockState state = getWorld().getBlockState(rendererState.pos);
		return state.getPropertyKeys().contains(AunisProps.RENDER_BLOCK) && !state.getValue(AunisProps.RENDER_BLOCK);
	}
	
	/**
	 * @param mergeHelper Merge helper instance.
	 * @return {@link Map} of {@link BlockPos} to {@link IBlockState} for rendering of the ghost blocks.
	 */
	protected abstract Map<BlockPos, IBlockState> getMemberBlockStates(StargateAbstractMergeHelper mergeHelper, EnumFacing facing);
		
	protected abstract void applyLightMap(S rendererState, double partialTicks);
	protected abstract void applyTransformations(S rendererState);
	protected abstract void renderGate(S rendererState, double partialTicks);
	
	private static final float VORTEX_START = 5.275f;
	private static final float SPEED_FACTOR = 6f;
	
	public enum EnumVortexState {
		FORMING(0),
		FULL(1),
		DECREASING(2),
		STILL(3),
		CLOSING(4),
		SHRINKING(5);
		
		public int index;
		private static Map<Integer, EnumVortexState> map = new HashMap<Integer, EnumVortexState>();
		
		EnumVortexState(int index) {
			this.index = index;
		}
		
		public boolean equals(EnumVortexState state) {
			return this.index == state.index;
		}
		
		static {
			for (EnumVortexState packet : EnumVortexState.values()) {
				map.put(packet.index, packet);
			}
		}
		
		public static EnumVortexState valueOf(int index) {
			return map.get(index);
		}
	}
	
	protected static final ResourceLocation EV_HORIZON_NORMAL_TEXTURE_ANIMATED = new ResourceLocation(Aunis.ModID, "textures/tesr/event_horizon_animated.jpg");
	protected static final ResourceLocation EV_HORIZON_DESATURATED_TEXTURE_ANIMATED = new ResourceLocation(Aunis.ModID, "textures/tesr/event_horizon_animated.jpg_desaturated");
	
	protected static final ResourceLocation EV_HORIZON_NORMAL_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/tesr/event_horizon.jpg");
	protected static final ResourceLocation EV_HORIZON_DESATURATED_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/tesr/event_horizon_unstable.jpg");
	
	protected ResourceLocation getEventHorizonTextureResource(StargateAbstractRendererState rendererState) {
		if (AunisConfig.stargateConfig.disableAnimatedEventHorizon)
			return rendererState.horizonUnstable ? EV_HORIZON_DESATURATED_TEXTURE : EV_HORIZON_NORMAL_TEXTURE;
		
		return rendererState.horizonUnstable ? EV_HORIZON_DESATURATED_TEXTURE_ANIMATED : EV_HORIZON_NORMAL_TEXTURE_ANIMATED;
	}
	
	protected void renderKawoosh(StargateAbstractRendererState rendererState, double partialTicks) {
//		rendererState.vortexState = EnumVortexState.FULL;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		float gateWait = getWorld().getTotalWorldTime() - rendererState.gateWaitStart;
		
		// Waiting for sound sync
		if ( gateWait < 44 ) {
			return;
		}
		
		GlStateManager.disableLighting();
        GlStateManager.enableCull();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 0.1);
		
		Texture ehTexture = TextureLoader.getTexture(getEventHorizonTextureResource(rendererState));
		if (ehTexture != null)
			ehTexture.bindTexture();
		
		long kawooshStart = rendererState.gateWaitStart + 44;
		float tick = (float) (getWorld().getTotalWorldTime() - kawooshStart + partialTicks);
		float mul = 1;
		
		float inner = StargateRendererStatic.eventHorizonRadius - tick/3.957f;
		
		// Fading in the unstable vortex
		float tick2 = tick/4f;
		if ( tick2 <= Math.PI/2 )
			rendererState.whiteOverlayAlpha = MathHelper.cos( tick2 );
		
		else {
			if (!rendererState.zeroAlphaSet) {
				rendererState.zeroAlphaSet = true;
				rendererState.whiteOverlayAlpha = 0.0f;
			}
		}
		
		// Going center
		if (inner >= StargateRendererStatic.kawooshRadius) {
			rendererState.backStrip = new QuadStrip(8, inner - 0.2f, StargateRendererStatic.eventHorizonRadius, tick);
		}
		
		else {
			if (rendererState.backStripClamp) {
				// Clamping to the desired size
				rendererState.backStripClamp = false;
				rendererState.backStrip = new QuadStrip(8, StargateRendererStatic.kawooshRadius - 0.2f, StargateRendererStatic.eventHorizonRadius, null);
				
				float argState = (tick - VORTEX_START) / SPEED_FACTOR;
								
				if (argState < 1.342f)
					rendererState.vortexState = EnumVortexState.FORMING;
				else if (argState < 4.15f)
					rendererState.vortexState = EnumVortexState.FULL;
				else if (argState < 5.898f)
					rendererState.vortexState = EnumVortexState.DECREASING;
				else if ( rendererState.vortexState != EnumVortexState.CLOSING )
					rendererState.vortexState = EnumVortexState.STILL;
			}

			float prevZ = 0;
			float prevRad = 0;
			
			boolean first = true;
			
			if ( !(rendererState.vortexState == EnumVortexState.STILL) ) {
				float arg = (tick - VORTEX_START) / SPEED_FACTOR;
								
				if ( !(rendererState.vortexState == EnumVortexState.CLOSING))  {
					if ( !(rendererState.vortexState == EnumVortexState.SHRINKING) ) {
						if ( rendererState.vortexState == EnumVortexState.FORMING && arg >= 1.342f ) {
							rendererState.vortexState = EnumVortexState.FULL;
						}
						
						// Offset of the end of the function domain used to generate vortex 
						float end = 0.75f;
						
						if ( rendererState.vortexState == (EnumVortexState.DECREASING) && arg >= 5.398+end ) {
							rendererState.vortexState = EnumVortexState.STILL;
						}
						
						if ( rendererState.vortexState == (EnumVortexState.FULL) ) {				
							if ( arg >= 3.65f+end ) {
								rendererState.vortexState = EnumVortexState.DECREASING;
							}
							
							// Flattening the vortex and keeping it still for a moment
							if (arg < 2)
								mul = (arg-1.5f)*(arg-2.5f)/-10f + 0.91f;
							else if (arg > 3+end)
								mul = (arg-2.5f-end)*(arg-3.5f-end)/-10f + 0.91f;
							else
								mul = 0.935f;
						}
						
						else {
							if ( rendererState.vortexState == (EnumVortexState.FORMING) )
								mul = ( arg * (arg-4) ) / -4.0f;
							
							else
								mul = ( (arg-1-end) * (arg-5-end) ) / -5.968f + 0.29333f;
						}
						
						// Rendering the vortex
						for ( Map.Entry<Float, Float> e : StargateRendererStatic.Z_RadiusMap.entrySet() ) {
							if (first) {
								first = false;
								prevZ = e.getKey();
								prevRad = e.getValue();
							}
							
							else {
								float zOffset = e.getKey();
								float rad = e.getValue();
								
//								mul = 0.945f;
								// Aunis.getRendererInit().new QuadStrip(8, rad, prevRad, tick).render(tick, zOffset*mul, prevZ*mul);
								new QuadStrip(8, rad, prevRad, tick).render(tick, zOffset*mul, prevZ*mul, false, 1.0f - rendererState.whiteOverlayAlpha, 1);
								
								prevZ = zOffset;
								prevRad = rad;
							}
						} // for end
					} // not shrinking if
					
					else {
						// Going outwards, closing the gate 29
						long stateChange = rendererState.gateWaitClose + 35;
						float arg2 = (float) ((getWorld().getTotalWorldTime() - stateChange + partialTicks) / 3f) - 1.0f;
												
						if (arg2 < StargateRendererStatic.eventHorizonRadius+0.1f) {
							rendererState.backStrip = new QuadStrip(8, arg2, StargateRendererStatic.eventHorizonRadius, tick);
						}
						
						else {
							rendererState.whiteOverlayAlpha = null;							
							
							if (getWorld().getTotalWorldTime() - stateChange - 9 > 7) {
								rendererState.doEventHorizonRender = false;							
//								clearChevrons(stateChange + 9 + 7);
							}
							
							// return;
						}
					}
				} // not closing if
				
				else {					
					// Fading out the event horizon, closing the gate
					if ( (getWorld().getTotalWorldTime() - rendererState.gateWaitClose) > 35 ) {
						float arg2 = (float) ((getWorld().getTotalWorldTime() - (rendererState.gateWaitClose+35) + partialTicks) / SPEED_FACTOR / 2f);
												
						if ( arg2 <= Math.PI/6 )
							rendererState.whiteOverlayAlpha = MathHelper.sin( arg2 );
						else {
							if (rendererState.backStrip == null)
								rendererState.backStrip = new QuadStrip(8, arg2, StargateRendererStatic.eventHorizonRadius, tick);
							
							rendererState.vortexState = EnumVortexState.SHRINKING;
						}
					}
				}
			} // not still if
		}
						
		// Rendering proper event horizon or the <rendererState.backStrip> for vortex
		if (rendererState.vortexState != null) {
			if ( rendererState.vortexState == (EnumVortexState.STILL) || rendererState.vortexState == EnumVortexState.CLOSING ) {
				
//				if (rendererState.horizonUnstable)
//					ModelLoader.bindTexture(ModelLoader.getTexture("stargate/event_horizon_by_mclatchyt_2_unstable.jpg"));
				
				if ( rendererState.vortexState == EnumVortexState.CLOSING )
					renderEventHorizon(partialTicks, true, rendererState.whiteOverlayAlpha, false, 1.7f);
				else
					renderEventHorizon(partialTicks, false, null, false, rendererState.horizonUnstable ? 1.2f : 1);
					
				GlStateManager.popMatrix();
				GlStateManager.enableLighting();
				
				return;
			}
		}
				
		if (rendererState.whiteOverlayAlpha != null) {
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableBlend();
			
			if (rendererState.backStrip != null)
				rendererState.backStrip.render(tick, 0f, null, false, 1.0f - rendererState.whiteOverlayAlpha, 1);
			
			renderEventHorizon(partialTicks, false, 0.0f, true, 1.0f);
			
			GlStateManager.disableBlend();
		}
		
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
	
	/**
	 * Renders event horizon(white/blue flat thing)
	 * 
	 * @param white Are we rendering the white overlay?
	 * @param alpha Alpha channel of the white overlay
	 * @param backOnly Render only the back face?(Used in kawoosh)
	 * @param mul Multiplier of the horizon waving speed
	 */
	protected void renderEventHorizon(double partialTicks, boolean white, Float alpha, boolean backOnly, float mul) {			
		float tick = (float) (getWorld().getTotalWorldTime() + partialTicks);	
		
	    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		
		for (int k=(backOnly ? 1 : 0); k<2; k++) {
			if (k == 1) {
//				GlStateManager.popMatrix();
//				
//				GlStateManager.pushMatrix();
//				GlStateManager.translate(x, y, z);
				GlStateManager.rotate(180, 0, 1, 0);
				//glColor4f(1,1,1,0.7f);
			}
			
			if (alpha == null)
				alpha = 0.0f;
			
			if (k == 1)
				alpha += 0.3f;
				
			
			if (white)
				StargateRendererStatic.innerCircle.render(tick, true, alpha, mul);
			
			StargateRendererStatic.innerCircle.render(tick, false, 1.0f-alpha, mul);
			
			
			for ( QuadStrip strip : StargateRendererStatic.quadStrips ) {
				if (white)
					strip.render(tick, true, alpha, mul);
				
				strip.render(tick, false, 1.0f-alpha, mul);
			}
		}
		
		GlStateManager.disableBlend();
	}
	
	@Override
	public boolean isGlobalRenderer(StargateAbstractBaseTile te) {
		return true;
	}
}
