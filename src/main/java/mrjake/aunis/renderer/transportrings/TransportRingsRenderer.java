package mrjake.aunis.renderer.transportrings;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.TransportRingsRendererState;
import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.world.World;

public class TransportRingsRenderer implements ISpecialRenderer<TransportRingsRendererState>{

	public static final int ringCount = 5; 
	public static final int uprisingInterval = 5; 
	public static final int fallingInterval = 5;
	
	public static final double animationDiv = 2.7; 
	
	private World world;
	private List<Ring> rings;
		
	public TransportRingsRenderer(TransportRingsTile te) {
		this.world = te.getWorld();
		
		rings = new ArrayList<>();
		for (int i=0; i<ringCount; i++) {
			rings.add(new Ring(world, i));
		}
	}
	
	
	// --------------------------------------------------------------------------
	private int currentRing;
	private int lastRingAnimated;	
	private long lastTick;	
	
	@Override
	public void render(double x, double y, double z, double partialTicks) {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y+1.1, z);
		GlStateManager.scale(0.8, 0.8, 0.8);

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
					if (tick % uprisingInterval == 0 && tick != lastTick) {	
						currentRing = (int)(tick/uprisingInterval) - 1;
						
//						Aunis.info("[uprising][currentRing="+currentRing+"]: tick: "+tick);
						
						// Handles correction when rings were not rendered
						for (int ring=lastRingAnimated+1; ring<Math.min(currentRing, ringCount); ring++) {
//							Aunis.info("[uprising][ring="+ring+"]: setTop()");
							
							rings.get(ring).setTop();
						}
						
						if (currentRing < ringCount) {
							rings.get(currentRing).animate(state.ringsUprising);
						
							lastRingAnimated = currentRing;
							lastTick = tick;
						}
						
						if (currentRing >= ringCount-1) {
							state.ringsUprising = false;
							
							lastRingAnimated = ringCount;
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
					if (tick % fallingInterval == 0 && tick != lastTick) {
						currentRing = ringCount - (int)(tick/fallingInterval);
						
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
	
	@Override
	public void setState(TransportRingsRendererState rendererState) {
		lastTick = -1;
		this.state = rendererState;
	}

	@Override
	public float getHorizontalRotation() {
		return 0;
	}
}
