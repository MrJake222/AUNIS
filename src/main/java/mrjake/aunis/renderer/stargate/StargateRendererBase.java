package mrjake.aunis.renderer.stargate;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.renderer.stargate.StargateRendererStatic.QuadStrip;
import mrjake.aunis.renderer.state.stargate.StargateRendererStateBase;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisPositionedSound;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class StargateRendererBase {
	
	protected World world;
	protected BlockPos pos;
	
	protected EnumFacing facing;	
	protected int horizontalRotation;
	
	public StargateRendererBase(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
				
		facing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
		
		if ( facing.getAxis().getName() == "x" )
			horizontalRotation = (int) facing.getOpposite().getHorizontalAngle();
		else
			horizontalRotation = (int) facing.getHorizontalAngle();
	}
	
	public int getHorizontalRotation() {
		return horizontalRotation;
	}
	
	// ---------------------------------------------------------------------------------------
	// Render
	
	public final void render(double x, double y, double z, double partialTicks) {
		
		if (shouldRender()) {			
			applyLightMap(partialTicks);
			
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x, y, z);
			renderRing(partialTicks);
			
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			renderGate();
			renderChevrons(partialTicks);
			
			if (getRendererState().doEventHorizonRender)
				renderKawoosh(partialTicks);
			
			GlStateManager.popMatrix();
		}
	}
	
	protected abstract boolean shouldRender();
	
	protected abstract StargateRendererStateBase getRendererState();
	public abstract void setRendererState(StargateRendererStateBase state);
	
	protected abstract void applyLightMap(double partialTicks);
	
	protected abstract void renderGate();
	protected abstract void renderRing(double partialTicks);
	protected abstract void renderChevrons(double partialTicks);
	public abstract void clearChevrons(Long stateChange);
	
	
	private long kawooshStart;
	private float vortexStart;
	
	private final float speedFactor = 6f;
	
	private QuadStrip backStrip;
	private boolean backStripClamp;
	
	private Float whiteOverlayAlpha;
	
	private float gateWaitStart = 0;
	
	private long gateWaitClose = 0;
	private boolean zeroAlphaSet;	
	
	private boolean horizonUnstable = false;
	
	public void setHorizonUnstable(boolean horizonUnstable) {
		this.horizonUnstable = horizonUnstable;
	}
	
	public void openGate() {
		Aunis.info("openGate");
		
		gateWaitStart = world.getTotalWorldTime();
		
		zeroAlphaSet = false;
		backStripClamp = true;
		whiteOverlayAlpha = 1.0f;
		
		getRendererState().vortexState = EnumVortexState.FORMING;
		
		kawooshStart = world.getTotalWorldTime();
		getRendererState().doEventHorizonRender = true;
	}
	
	public void closeGate() {
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.WORMHOLE, pos, false);
		
		AunisSoundHelper.playSoundEventClientSide((WorldClient) world, pos, EnumAunisSoundEvent.GATE_CLOSE, 0.3f);
		gateWaitClose = world.getTotalWorldTime();
		
		getRendererState().vortexState = EnumVortexState.CLOSING;
	}
	
	private void engageGate() {
		getRendererState().vortexState = EnumVortexState.STILL;
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.WORMHOLE, pos, true);
	}
	
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
	
	protected void renderKawoosh(double partialTicks) {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		float gateWait = world.getTotalWorldTime() - gateWaitStart;
		
		// Waiting for sound sync
		if ( gateWait < 44 ) {
			return;
		}
		
		kawooshStart = (long) (gateWaitStart + 44);
		
		GlStateManager.disableLighting();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 0.1);
		
		ModelLoader.bindTexture(ModelLoader.getTexture("stargate/event_horizon_by_mclatchyt_2.jpg"));
		
		float tick = (float) (world.getTotalWorldTime() - kawooshStart + partialTicks);
		float mul = 1;
		
		float inner = StargateRendererStatic.eventHorizonRadius - tick/3.957f;
		
		// Fading in the unstable vortex
		float tick2 = tick/4f;
		if ( tick2 <= Math.PI/2 )
			whiteOverlayAlpha = MathHelper.cos( tick2 );
		
		else {
			if (!zeroAlphaSet) {
				zeroAlphaSet = true;
				whiteOverlayAlpha = 0.0f;
			}
		}
		
		// Going center
		if (inner >= StargateRendererStatic.kawooshRadius) {
			backStrip = new QuadStrip(8, inner - 0.2f, StargateRendererStatic.eventHorizonRadius, tick);
		}
		
		else {
			if (backStripClamp) {
				// Clamping to the desired size
				backStripClamp = false;
				backStrip = new QuadStrip(8, StargateRendererStatic.kawooshRadius - 0.2f, StargateRendererStatic.eventHorizonRadius, null);
				
				vortexStart = 5.275f;
				
				float argState = (tick - vortexStart) / speedFactor;
								
				if (argState < 1.342f)
					getRendererState().vortexState = EnumVortexState.FORMING;
				else if (argState < 4.15f)
					getRendererState().vortexState = EnumVortexState.FULL;
				else if (argState < 5.898f)
					getRendererState().vortexState = EnumVortexState.DECREASING;
				else if ( getRendererState().vortexState != EnumVortexState.CLOSING )
					engageGate();
			}

			float prevZ = 0;
			float prevRad = 0;
			
			boolean first = true;
			
			if ( !(getRendererState().vortexState == EnumVortexState.STILL) ) {
				float arg = (tick - vortexStart) / speedFactor;
								
				if ( !(getRendererState().vortexState == EnumVortexState.CLOSING))  {
					if ( !(getRendererState().vortexState == EnumVortexState.SHRINKING) ) {
						if ( getRendererState().vortexState == EnumVortexState.FORMING && arg >= 1.342f ) {
							getRendererState().vortexState = EnumVortexState.FULL;
						}
						
						// Offset of the end of the function domain used to generate vortex 
						float end = 0.75f;
						
						if ( getRendererState().vortexState == (EnumVortexState.DECREASING) && arg >= 5.398+end ) {
							engageGate();
						}
						
						if ( getRendererState().vortexState == (EnumVortexState.FULL) ) {				
							if ( arg >= 3.65f+end ) {
								getRendererState().vortexState = EnumVortexState.DECREASING;
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
							if ( getRendererState().vortexState == (EnumVortexState.FORMING) )
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
								
								// Aunis.getRendererInit().new QuadStrip(8, rad, prevRad, tick).render(tick, zOffset*mul, prevZ*mul);
								new QuadStrip(8, rad, prevRad, tick).render(tick, zOffset*mul, prevZ*mul, false, 1.0f - whiteOverlayAlpha, 1);
								
								prevZ = zOffset;
								prevRad = rad;
							}
						} // for end
					} // not shrinking if
					
					else {
						// Going outwards, closing the gate 29
						long stateChange = gateWaitClose + 35;
						float arg2 = (float) ((world.getTotalWorldTime() - stateChange + partialTicks) / 3f) - 1.0f;
												
						if (arg2 < StargateRendererStatic.eventHorizonRadius+0.1f) {
							backStrip = new QuadStrip(8, arg2, StargateRendererStatic.eventHorizonRadius, tick);
						}
						
						else {
							whiteOverlayAlpha = null;							
							
							if (world.getTotalWorldTime() - stateChange - 9 > 7) {
								getRendererState().doEventHorizonRender = false;							
								clearChevrons(stateChange + 9 + 7);
							}
							
							// return;
						}
					}
				} // not closing if
				
				else {					
					// Fading out the event horizon, closing the gate
					if ( (world.getTotalWorldTime() - gateWaitClose) > 35 ) {
						float arg2 = (float) ((world.getTotalWorldTime() - (gateWaitClose+35) + partialTicks) / speedFactor / 2f);
												
						if ( arg2 <= Math.PI/6 )
							whiteOverlayAlpha = MathHelper.sin( arg2 );
						else {
							if (backStrip == null)
								backStrip = new QuadStrip(8, arg2, StargateRendererStatic.eventHorizonRadius, tick);
							
							getRendererState().vortexState = EnumVortexState.SHRINKING;
						}
					}
				}
			} // not still if
		}
						
		// Rendering proper event horizon or the <backStrip> for vortex
		if (getRendererState().vortexState != null) {
			if ( getRendererState().vortexState == (EnumVortexState.STILL) || getRendererState().vortexState == EnumVortexState.CLOSING ) {
				
				if (horizonUnstable)
					ModelLoader.bindTexture(ModelLoader.getTexture("stargate/event_horizon_by_mclatchyt_2_unstable.jpg"));

//				if ( getRendererState().vortexState == (EnumVortexState.CLOSING) || getRendererState().vortexState == EnumVortexState.SHRINKING )
//					renderEventHorizon(x, y, z, partialTicks, true, whiteOverlayAlpha, true, 1);
//				else				
//					renderEventHorizon(x, y, z, partialTicks, false, 0.0f, false, horizonUnstable ? 2f : 1);
				
				if ( getRendererState().vortexState == EnumVortexState.CLOSING )
					renderEventHorizon(partialTicks, true, whiteOverlayAlpha, false, 1.7f);
				else
					renderEventHorizon(partialTicks, false, null, false, horizonUnstable ? 1.2f : 1);
					
				GlStateManager.popMatrix();
				GlStateManager.enableLighting();
				
				return;
			}
		}
				
		if (whiteOverlayAlpha != null) {
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableBlend();
			
			if (backStrip != null)
				backStrip.render(tick, 0f, null, false, 1.0f - whiteOverlayAlpha, 1);
			
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
		float tick = (float) (world.getTotalWorldTime() + partialTicks) * mul;	
		
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
}
