package mrjake.aunis.renderer.transportrings;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.state.TransportRingsRendererState;
import mrjake.aunis.tesr.RendererInterface;
import mrjake.aunis.util.AunisAxisAlignedBB;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.world.World;

public class TransportRingsRenderer implements RendererInterface {

	public static final int RING_COUNT = 5; 
	public static final int INTERVAL_UPRISING = 5; 
	public static final int INTERVAL_FALLING = 5;
	
	public static final double ANIMATION_SPEED_DIVISOR = 2.7; 
	
	private World world;
	private AunisAxisAlignedBB localTeleportBox;
	private List<Ring> rings;
		
	public TransportRingsRenderer(World world, AunisAxisAlignedBB localTeleportBox) {
		this.world = world;
		this.localTeleportBox = localTeleportBox;
		
		rings = new ArrayList<>();
		for (int i=0; i<RING_COUNT; i++) {
			rings.add(new Ring(world, i));
		}
	}
	
	
	// --------------------------------------------------------------------------
	private int currentRing;
	private int lastRingAnimated;	
	private long lastTick;	
		
	@Override
	public void render(double x, double y, double z, float partialTicks) {		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		if (AunisConfig.debugConfig.renderBoundingBoxes)
			localTeleportBox.render();
		
		GlStateManager.translate(0.50, 0.63271/2 + 1.35, 0.50);
		GlStateManager.scale(0.5, 0.5, 0.5);
		
		for (Ring ring : rings)
			ring.render(partialTicks);
		
		GlStateManager.popMatrix();
		
		// ---------------------------------------------------------------------------
		long tick = world.getTotalWorldTime() - state.animationStart;
		
		if (state.isAnimationActive) {
			/**
			 * If the rings are going up(initial state), wait 30 ticks(1.5s) for the animation to begin
			 */
			if (state.ringsUprising) {
				if (tick > 30) {
					tick -= 30;
					
					/**
					 * Spawn rings in intervals of 7 ticks(not repeated in a single tick)
					 */
					if (tick % INTERVAL_UPRISING == 0 && tick != lastTick) {	
						currentRing = (int)(tick/INTERVAL_UPRISING) - 1;
						
//						Aunis.info("[uprising][currentRing="+currentRing+"]: tick: "+tick);
						
						// Handles correction when rings were not rendered
						for (int ring=lastRingAnimated+1; ring<Math.min(currentRing, RING_COUNT); ring++) {
//							Aunis.info("[uprising][ring="+ring+"]: setTop()");
							
							rings.get(ring).setTop();
						}
						
						if (currentRing < RING_COUNT) {
							rings.get(currentRing).animate(state.ringsUprising);
						
							lastRingAnimated = currentRing;
							lastTick = tick;
						}
						
						if (currentRing >= RING_COUNT-1) {
							state.ringsUprising = false;
							
							lastRingAnimated = RING_COUNT;
							lastTick = -1;
						}
					}
				}
			}
			
			/**
			 * If going down wait 100 tick (5s, transport sound played)
			 */
			else {
				if (tick > 100) {
					tick -= 100;
					
					/**
					 * Start lowering them in interval of 5 ticks
					 */
					if (tick % INTERVAL_FALLING == 0 && tick != lastTick) {
						currentRing = RING_COUNT - (int)(tick/INTERVAL_FALLING);
						
//						Aunis.info("[falling ][currentRing="+currentRing+"]: lastRingAnimated: "+lastRingAnimated);
						
						// Correction for skipped frames(when not looking at)
						for (int ring=lastRingAnimated-1; ring>Math.max(currentRing, -1); ring--) {
//							Aunis.info("[falling ][ring="+ring+"]: setDown()");
							
							rings.get(ring).setDown();
						}
						
						
						if (currentRing >= 0) {
							rings.get(currentRing).animate(state.ringsUprising);
						
							lastRingAnimated = currentRing;
							lastTick = tick;
						}
						
						else {
							state.isAnimationActive = false;
						}
						
						lastTick = tick;
					}
				}
			}
		}
	}
	
	public void animationStart(long animationStart) {	
		lastTick = -1;
		currentRing = 0;
		lastRingAnimated = -1;
		
		state.animationStart = animationStart;
		state.ringsUprising = true;
		state.isAnimationActive = true;
	}

	TransportRingsRendererState state = new TransportRingsRendererState();
	
	public void setState(TransportRingsRendererState rendererState) {
		lastTick = -1;
		this.state = rendererState;
	}
}
