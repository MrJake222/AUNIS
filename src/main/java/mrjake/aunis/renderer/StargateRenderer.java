package mrjake.aunis.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisSoundEvents;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.block.BlockFaced;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.dhd.renderingUpdate.ClearLinkedDHDButtons;
import mrjake.aunis.packet.gate.stateUpdate.StateUpdateToServer;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class StargateRenderer implements Renderer<StargateRendererState> {
	// private StargateBaseTile te;
	private World world;
	private BlockPos pos;
	
	private static final Vec3d ringLoc = new Vec3d(0.0, -0.122333, -0.000597);
	private int horizontalRotation;	

	Random rand = new Random();
	
	private float getRandomFloat() {
		return rand.nextFloat()*2-1;
	}
	
	public StargateRenderer(StargateBaseTile te) {
		// this.te = te;
		this.world = te.getWorld();
		this.pos = te.getPos();
		
		this.ringAngularRotation = 0;
		
		EnumFacing facing = world.getBlockState(pos).getValue(BlockFaced.FACING);
		
		if ( facing.getAxis().getName() == "x" )
			horizontalRotation = (int) facing.getOpposite().getHorizontalAngle();
		else
			horizontalRotation = (int) facing.getHorizontalAngle();
		
		// Load chevron textures
		for (int i=0; i<=10; i++) {
			ResourceLocation resource = new ResourceLocation( "aunis:textures/tesr/stargate/chevron/chevron"+i+".png" );
			
			ITextureObject itextureobject = new SimpleTexture(resource);
			Minecraft.getMinecraft().getTextureManager().loadTexture(resource, itextureobject);
		}
		
		for (int i=0; i<9; i++) {
			chevronTextureList.add(textureTemplate + "0.png");
		}
				
		initEventHorizon();
		initKawoosh();
	}
	
	@Override
	public void setState(StargateRendererState state) {
		setActiveChevrons(state.activeChevrons, state.isFinalActive);
		
		ringAngularRotation = state.ringAngularRotation;
		ringSpin = state.ringSpin;
		ringSpinStart = state.ringSpinStart;
		
		vortexState = state.vortexState;
		soundPlayed = state.soundPlayed;
		doEventHorizonRender = state.doEventHorizonRender;
		dialingComplete = state.dialingComplete;
		
		wormholeSound(vortexState == EnumVortexState.STILL);
		rollSound(ringSpin);
	}
	
	@Override
	public StargateRendererState getState() {
		return new StargateRendererState(pos, getActiveChevrons(), isLastChevronActive(), ringAngularRotation, ringSpin, ringSpinStart, doEventHorizonRender, vortexState, lockSoundPlayed, dialingComplete);
	}
	
	@Override
	public void render(double x, double y, double z, double partialTicks) {
		renderGate(x, y, z);
		renderRing(x, y, z, partialTicks);
		renderChevrons(x, y, z, partialTicks);
		
		if (doEventHorizonRender) {
			renderKawoosh(x, y, z, partialTicks);
		}
	}
	
	private void renderGate(double x, double y, double z) {
		Model gateModel = Aunis.modelLoader.getModel( EnumModel.GATE_MODEL );
		
		if ( gateModel != null ) {
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			ModelLoader.bindTexture( EnumModel.GATE_MODEL );
			gateModel.render();
			
			GlStateManager.popMatrix();
		}
	}
	
	private final float targetAnglePerTick = 2.0f;
	
	private final float accelerationMul = 1.0f / 16.0f;
	private final float maxTick = (float) (Math.PI / accelerationMul / 2);

	private float ringAngularRotation;
	private boolean ringSpin;
	private long ringSpinStart;
	
	private float lastTick;
	
	private boolean ringAccelerating;
	
	private boolean ringDecelerating;
	private boolean ringDecelFirst;
	private float ringDecelStart;
	
	private boolean lockSoundPlayed;
	private boolean dialingComplete;
	
	private void rollSound(boolean play) {
		PositionedSoundRecord sound = AunisSoundEvents.ringRollSoundMap.get( pos );
			
		if (play) {			
			if ( sound == null ) {
				sound = new PositionedSoundRecord(AunisSoundEvents.ringRoll, SoundCategory.BLOCKS, 1.0f, 1.0f, pos);
								
				AunisSoundEvents.ringRollSoundMap.put(pos, sound);
			}
			
			Minecraft.getMinecraft().getSoundHandler().playSound( sound );
		}
		
		else {					
			if ( sound != null ) {
				Minecraft.getMinecraft().getSoundHandler().stopSound( sound );
			}
		}
	}
	
	public void setRingSpin(boolean spin, boolean dialingComplete) {
		this.dialingComplete = dialingComplete;
		
		if (!dialingComplete)
			AunisSoundEvents.playSound(world, pos, AunisSoundEvents.gateDialFail);
		
		if (spin) {
			rollSound(true);
			
			ringSpinStart = world.getTotalWorldTime();
			lastTick = -1;
			
			ringDecelerating = false;
			ringAccelerating = true;
			ringSpin = true;
		}
		
		else {
			rollSound(false);
			
			lockSoundPlayed = false;
			ringDecelFirst = true;
			
			ringAccelerating = false;
			ringDecelerating = true;
		}
	}
	
	private void renderRing(double x, double y, double z, double partialTicks) {
		Model ringModel = Aunis.modelLoader.getModel(EnumModel.RING_MODEL);
		
		if (ringModel != null) {
			
			if (ringSpin) {
				float tick = (float) (world.getTotalWorldTime() - ringSpinStart + partialTicks);
				float anglePerTick = targetAnglePerTick;
				
				if (ringAccelerating) {
					if ( tick < maxTick ) {
						anglePerTick *= MathHelper.sin( tick * accelerationMul );
					}
					
					else {
						ringAccelerating = false;
					}
				}
				
				if (ringDecelerating) {
					if (ringDecelFirst) {
						ringDecelFirst = false;
						ringDecelStart = tick;
					}
					
					else {
						float tickDecel = tick - ringDecelStart;
						
						if ( tickDecel > maxTick/2.0f && !lockSoundPlayed ) {
							lockSoundPlayed = true;
							
							// Play final chevron lock sound
							if (dialingComplete) {
								moveFinalChevron();
								AunisSoundEvents.playSound(world, pos, AunisSoundEvents.chevronLockDHD);
							}
						}
						
						if ( tickDecel <= maxTick ) {
							anglePerTick *= MathHelper.cos( tickDecel * accelerationMul );
						}
						
						else {
							ringDecelerating = false;
							ringSpin = false;
							lastTick = -1;
						}
					}
				}
								
				if (lastTick != -1) {
					float time = tick - lastTick;
					
					ringAngularRotation += time * anglePerTick;
				}
								
				lastTick = tick;
			}
			
			GlStateManager.pushMatrix();
			
			if (horizontalRotation == 90 || horizontalRotation == 270) {
				GlStateManager.translate(x+ringLoc.y, y+ringLoc.z, z+ringLoc.x);
				GlStateManager.rotate(ringAngularRotation, 1, 0, 0);
				GlStateManager.translate(-ringLoc.y, -ringLoc.z, -ringLoc.x);
			}
			else {
				GlStateManager.translate(x+ringLoc.x, y+ringLoc.z, z+ringLoc.y);
				GlStateManager.rotate(ringAngularRotation, 0, 0, 1);
				GlStateManager.translate(-ringLoc.x, -ringLoc.z, -ringLoc.y);
			}
			
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			ModelLoader.bindTexture( EnumModel.RING_MODEL );
			ringModel.render();
			
			GlStateManager.popMatrix();
		}
	}
	
	public enum EnumChevron {
		C1(1),
		C2(2),
		C3(3),
		
		C4(6),
		C5(7),
		C6(8),
		
		C7(4),
		C8(5),
		
		C9(0);
		
		public int index;
		public int rotation;
				
		EnumChevron(int index) {
			this.index = index;
			this.rotation = -40*index;
		}		
		
		public static int toGlobal(int index) {
			return values()[index].index;
		}
		
		public static int getRotation(int index) {
			return values()[index].rotation;
		}
	}
	
	private List<String> chevronTextureList = new ArrayList<String>();
	private static final String textureTemplate = "stargate/chevron/chevron";
	
	private int activation = -1;
	private long activationStateChange = 0;
	
	private int activeChevrons = 0;
	private boolean changingChevrons = false;
	private boolean clearingChevrons;
	private int chevronsToLightUp;
	
	private float finalChevronStart;
	private boolean finalChevronMove;
	
	public void activateFinalChevron() {
		activationStateChange = world.getTotalWorldTime();
		
		// chevronActiveList.set(8, true);
		activation = 8;
		activeChevrons++;
		
		setRingSpin( false, true );
	}
	
	public void activateNextChevron() { 
		if (activation == -1) {			
			activationStateChange = world.getTotalWorldTime();
			
			// chevronActiveList.set(activeChevrons, true);
			activation = activeChevrons;
						
			if (activeChevrons == 0) {
				setRingSpin( true, true );
			}
			
			if (activeChevrons < 8)
				activeChevrons++;
		}
	}
	
	public void clearChevrons(Long stateChange) {
		changeChevrons(true, stateChange);
	}
	
	public void clearChevrons() {
		clearChevrons(null);
	}
	
	public void lightUpChevrons(int chevronsToLightUp) {
		AunisSoundEvents.playSound(world, pos, AunisSoundEvents.chevronIncoming);
		
		this.chevronsToLightUp = chevronsToLightUp;
		this.dialingComplete = true;
		
		changeChevrons(false, null);
	}
	
	public void changeChevrons(boolean clear, Long stateChange) {	
		changingChevrons = true;
		
		clearingChevrons = clear;
		
		if (stateChange != null)
			activationStateChange = stateChange;
		else
			activationStateChange = world.getTotalWorldTime();
		
		if (clearingChevrons) {
			if (dialingComplete)
				activationStateChange += 10;
			else
				activationStateChange += 30;
		}
		
		activation = 0;
	}
	
	public void moveFinalChevron() {
		finalChevronStart = 0;
		finalChevronMove = true;
	}
	
	private void renderChevron(double x, double y, double z, int index, double partialTicks) {
		Model ChevronLight = Aunis.modelLoader.getModel( EnumModel.ChevronLight );
		Model ChevronFrame = Aunis.modelLoader.getModel( EnumModel.ChevronFrame );
		Model ChevronMoving = Aunis.modelLoader.getModel( EnumModel.ChevronMoving );
		
		if ( ChevronLight != null && ChevronFrame != null && ChevronMoving != null ) {
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			int angularPosition = EnumChevron.getRotation(index);
			GlStateManager.rotate(angularPosition, 0, 0, 1);
			
			ModelLoader.bindTexture( chevronTextureList.get(index) );
			
			if (index == 8 && finalChevronMove) {
				if (finalChevronStart == 0)
					finalChevronStart = world.getTotalWorldTime();
				
				float tick = (float) (world.getTotalWorldTime() - finalChevronStart + partialTicks);
				float arg = tick / 6.0f;
				
				float finalChevronOffset = 0;
				
				if (arg <= Math.PI/2)
					finalChevronOffset = MathHelper.sin( arg ) / 12f;
				
				else if (arg <= Math.PI)
					finalChevronOffset = 0.08333f; // 1 / 12
				
				else if (arg <= 3*Math.PI/2)
					finalChevronOffset = -MathHelper.cos( arg ) / 12f;
				
				else {
					finalChevronOffset = 0;
					finalChevronMove = false;
				}
											
				ChevronFrame.render();
				
				GlStateManager.translate(0, finalChevronOffset, 0);
				ChevronLight.render();
			
				GlStateManager.translate(0, -2*finalChevronOffset, 0);
				ChevronMoving.render();
			}
			
			else {
				ChevronLight.render();	
				ChevronFrame.render();
				ChevronMoving.render();
			}
			
			GlStateManager.popMatrix();
		}
	}
	
	// private List<Boolean> LastChevronActiveList;
	
	private void renderChevrons(double x, double y, double z, double partialTicks) {
		for (int i=0; i<9; i++)
			renderChevron(x, y, z, i, partialTicks);
		
		if (activation != -1) {
			int stage = (int) (((world.getTotalWorldTime() - activationStateChange) + partialTicks) * 3);

			if (stage < 0) return;
			else {
				if (!dhdButtonsCleared && clearingChevrons) {
					dhdButtonsCleared = true;
					AunisPacketHandler.INSTANCE.sendToServer( new ClearLinkedDHDButtons(pos) );
				}
			}
			
			if (stage < 11) {
				if (changingChevrons) {
					for (int i=0; i<9; i++) {
						
						if (clearingChevrons) {
							if ( !chevronTextureList.get(i).contains("n0") ) {
								chevronTextureList.set(i, textureTemplate+(10-stage)+".png");
							}
						}
						
						else {
							if ( !chevronTextureList.get(i).contains("10") && (i < chevronsToLightUp || i == 8) ) {
								chevronTextureList.set(i, textureTemplate+stage+".png");
							}
						}
					}
				}
				
				else {
					chevronTextureList.set(activation, textureTemplate+stage+".png");
				}
			}
				
			else {
				if (changingChevrons) {
					changingChevrons = false;
					
					if (clearingChevrons)
						activeChevrons = 0;
					else
						activeChevrons = chevronsToLightUp+1;
					
					// Sync state to server after chevrons fade out
					AunisPacketHandler.INSTANCE.sendToServer( new StateUpdateToServer(getState()) );
					
					// This is needed because if the gate isn't rendered(player not looking at it)
					// render code isn't running and gate can't perform any animation(not necessary in this case)
					// So we need to check if chevron's states didn't change and perform correction
					
					for (int i=0; i<9; i++) {
						String tex = textureTemplate;
					
						if (clearingChevrons)
							tex += "0.png";
						else
							tex += "10.png";
						
						if ( !chevronTextureList.get(i).equals(tex) ) {
							chevronTextureList.set(i, tex);
						}
					}
				}
				
				activation = -1;
			}
		}
	}
	
	public boolean isLastChevronActive() {
		return chevronTextureList.get(8).contains("10");
	}
	
	public int getActiveChevrons() {
		if ( isLastChevronActive() )
			return activeChevrons - 1;
		else
			return activeChevrons;
	}
	
	public void setActiveChevrons(int activeChevrons, boolean lastChevronActive) {
		chevronTextureList.clear();
		this.activeChevrons = activeChevrons;
		
		for (int i=0; i<9; i++) {	
			String tex = textureTemplate;
			
			if ( i < activeChevrons || (i == 8 && lastChevronActive) )
				tex += "10.png";
			else
				tex += "0.png";
						
			chevronTextureList.add(tex);
		}
	}
	
	private long kawooshStart;
	private float vortexStart;
	
	private final float kawooshRadius = 2.5f;
	private final float kawooshSize = 7f;
	private final float kawooshSections = 128; 
	
	private final float speedFactor = 6f;
	
	private QuadStrip backStrip;
	private boolean backStripClamp;
	
	private boolean doEventHorizonRender = false;
	private Float whiteOverlayAlpha;
	
	private float gateWaitStart = 0;
	private boolean soundPlayed;
	
	private long gateWaitClose = 0;
	private boolean zeroAlphaSet;	
	
	private boolean dhdButtonsCleared;
		
	public void openGate() {
		dhdButtonsCleared = false;
		gateWaitStart = world.getTotalWorldTime();
		soundPlayed = false;
		
		zeroAlphaSet = false;
		backStripClamp = true;
		whiteOverlayAlpha = 1.0f;
		
		vortexState = EnumVortexState.FORMING;
		
		kawooshStart = world.getTotalWorldTime();
		doEventHorizonRender = true;
	}
	
	private PositionedSoundRecord wormholeSound;
	
	private void wormholeSound(boolean active) {
		if (wormholeSound == null)
			wormholeSound = new PositionedSoundRecord(new ResourceLocation("aunis", "wormhole_loop"), SoundCategory.BLOCKS, 1.0f, 1.0f, true, 0, AttenuationType.LINEAR, pos.getX(), pos.getY(), pos.getZ());
		
		try {
			if (active)
				Minecraft.getMinecraft().getSoundHandler().playSound(wormholeSound);
			else
				Minecraft.getMinecraft().getSoundHandler().stopSound(wormholeSound);
		}
		
		catch (IllegalArgumentException exception) {
			Aunis.info("IllegalArgumentException when playing wormhole loop");
		}
	}
	
	public void closeGate() {
		wormholeSound(false);
		
		AunisSoundEvents.playSound(world, pos, AunisSoundEvents.gateClose);
		gateWaitClose = world.getTotalWorldTime();
		// closingSoundPlayed = false;
		
		vortexState = EnumVortexState.CLOSING;
	}
	
	private Map<Float, Float> Z_RadiusMap = new LinkedHashMap<Float, Float>();
	
	// Generate kawoosh shape using 4 functions
	private void initKawoosh() {
		float begin = 0;
		float end   = 0.545f;
		
		float rng = end - begin;

		float step = rng / kawooshSections;
		boolean first = true;
		
		float scaleX = kawooshSize / rng;
		float scaleY = 1;
		
		for (int i=0; i<=kawooshSections; i++) {
			float x = begin + step*i;
			float y = 0;
			
			float border1 = 0.2575f;
			float border2 = 0.4241f;
			float border3 = 0.4577f;
			
			
			if ( x >= 0 && x <= border1 ) {
				float a = 2f;
				float b = -4.7f;
				float c = 2.1f;
				
				float p = x + (b/20f);
				y = (a/2f) * (p*p) + (c/30f);
				
				if (first) {
					first = false;
					scaleY = kawooshRadius / y;
					// Aunis.info("radius: " + kawooshRadius + "  y: " + y + "  scale: "+kawooshScaleFactor);
				}
			}
			
			else if ( x > border1 && x <= border2 ) {
				float a = 1.4f;
				float b = -4.3f;
				float c = 1.4f;
				
				float p = x + (b/20f);
				y = (a/5f) * (p*p) + (c/20f);
			}
			
			else if ( x > border2 && x <= border3 ) {
				float a = -7.4f;
				float b = -8.6f;
				float c = 3.3f;
				
				float p = x + (b/20f);
				y = a * (p*p) + (c/40f);
			}
			
			else if ( x > border3 && x <= 0.545f ) {
				float a = 5.2f;
				
				y = (float) (a/20f * Math.sqrt( 0.545f - x ));
			}
			
			Z_RadiusMap.put(x*scaleX, y*scaleY);
		}
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
	
	private EnumVortexState vortexState = EnumVortexState.FORMING;
	
	private void engageGate() {
		vortexState = EnumVortexState.STILL;
		wormholeSound(true);
		
		// Gate engaged, sync state to server
		AunisPacketHandler.INSTANCE.sendToServer( new StateUpdateToServer(getState()) );
	}
	
	private void renderKawoosh(double x, double y, double z, double partialTicks) {
		float gateWait = world.getTotalWorldTime() - gateWaitStart;
		
		if ( gateWait < 25 )
			return;
		
		// This must be done here, because we wait a little before playing any sound
		if ( !soundPlayed && gateWait < 30 ) {
			soundPlayed = true;
			
			AunisSoundEvents.playSound(world, pos, AunisSoundEvents.gateOpen);
		}
		
		// Waiting for sound sync
		if ( gateWait < 44 ) {
			return;
		}
		
		kawooshStart = (long) (gateWaitStart + 44);
		
		GlStateManager.disableLighting();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(horizontalRotation, 0, 1, 0);
		GlStateManager.translate(0, 0, 0.1);
		
		ModelLoader.bindTexture( "stargate/event_horizon_by_mclatchyt_2.jpg" );
			
		float tick = (float) (world.getTotalWorldTime() - kawooshStart + partialTicks);
		float mul = 1;
		
		float inner = eventHorizonRadius - tick/3.957f;
		
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
		if (inner >= kawooshRadius) {
			backStrip = new QuadStrip(0, inner - 0.2f, eventHorizonRadius, tick);
		}
		
		else {
			if (backStripClamp) {
				// Clamping to the desired size
				backStripClamp = false;
				backStrip = new QuadStrip(0, kawooshRadius - 0.2f, eventHorizonRadius, null);
				
				vortexStart = 5.275f;
				
				float argState = (tick - vortexStart) / speedFactor;
								
				if (argState < 1.342f)
					vortexState = EnumVortexState.FORMING;
				else if (argState < 4.15f)
					vortexState = EnumVortexState.FULL;
				else if (argState < 5.898f)
					vortexState = EnumVortexState.DECREASING;
				else if ( vortexState != EnumVortexState.CLOSING )
					engageGate();
			}

			float prevZ = 0;
			float prevRad = 0;
			
			boolean first = true;
			
			if ( !vortexState.equals(EnumVortexState.STILL) ) {
				float arg = (tick - vortexStart) / speedFactor;
								
				if ( !vortexState.equals(EnumVortexState.CLOSING) ) {
					if ( !vortexState.equals(EnumVortexState.SHRINKING) ) {
						if ( vortexState.equals(EnumVortexState.FORMING) && arg >= 1.342f ) {
							vortexState = EnumVortexState.FULL;
						}
						
						// Offset of the end of the function domain used to generate vortex 
						float end = 0.75f;
						
						if ( vortexState.equals(EnumVortexState.DECREASING) && arg >= 5.398+end ) {
							engageGate();
						}
						
						if ( vortexState.equals(EnumVortexState.FULL) ) {				
							if ( arg >= 3.65f+end ) {
								vortexState = EnumVortexState.DECREASING;
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
							if ( vortexState.equals(EnumVortexState.FORMING) )
								mul = ( arg * (arg-4) ) / -4.0f;
							
							else
								mul = ( (arg-1-end) * (arg-5-end) ) / -5.968f + 0.29333f;
						}
						
						// Rendering the vortex
						for ( Map.Entry<Float, Float> e : Z_RadiusMap.entrySet() ) {
							if (first) {
								first = false;
								prevZ = e.getKey();
								prevRad = e.getValue();
							}
							
							else {
								float zOffset = e.getKey();
								float rad = e.getValue();
								
								// new QuadStrip(0, rad, prevRad, tick).render(tick, zOffset*mul, prevZ*mul);
								new QuadStrip(0, rad, prevRad, tick).render(tick, zOffset*mul, prevZ*mul, false, 1.0f - whiteOverlayAlpha);
								
								prevZ = zOffset;
								prevRad = rad;
							}
						} // for end
					} // not shrinking if
					
					else {
						// Going outwards, closing the gate
						long stateChange = gateWaitClose + 29;
						float arg2 = (float) ((world.getTotalWorldTime() - stateChange + partialTicks) / 3f) - 1.0f;
						
						if (arg2 < eventHorizonRadius+0.1f) {
							backStrip = new QuadStrip(0, arg2, eventHorizonRadius, tick);
						}
						
						else {
							doEventHorizonRender = false;							
							clearChevrons(stateChange + 14);
						}
					}
				} // not closing if
				
				else {					
					// Fading out the event horizon, closing the gate
					if ( (world.getTotalWorldTime() - gateWaitClose) > 29 ) {
						float arg2 = (float) ((world.getTotalWorldTime() - (gateWaitClose+29) + partialTicks) / speedFactor / 2f);
												
						if ( arg2 <= Math.PI/6 )
							whiteOverlayAlpha = MathHelper.sin( arg2 );
						else {
							if (backStrip == null)
								backStrip = new QuadStrip(0, arg2, eventHorizonRadius, tick);
							
							vortexState = EnumVortexState.SHRINKING;
						}
					}
				}
			} // not still if
		}
				
		// Rendering proper event horizon or the <backStrip> for vortex
		if (vortexState != null) {
			if ( vortexState.equals(EnumVortexState.STILL) || vortexState.equals(EnumVortexState.CLOSING) ) {
				
				if ( vortexState.equals(EnumVortexState.CLOSING) )
					renderEventHorizon(x, y, z, partialTicks, true, whiteOverlayAlpha, false);
				else
					renderEventHorizon(x, y, z, partialTicks, false, null, false);
				
				GlStateManager.popMatrix();
				GlStateManager.enableLighting();
				
				
				
				return;
			}
		}
				
		if (whiteOverlayAlpha != null) {
			GlStateManager.enableBlend();
			
			if (backStrip != null)
				backStrip.render(tick, 0f, null, false, 1.0f - whiteOverlayAlpha);
			renderEventHorizon(x, y, z, partialTicks, false, 0f, true);
			
			GlStateManager.disableBlend();
		}
		
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
	
	private final float eventHorizonRadius = 3.790975f;
	
	private final int quads = 16;
	private final int sections = 36 * 2;
	private final float sectionAngle = (float) (2*Math.PI/sections);
	
	private final float innerCircleRadius = 0.25f;
	private final float quadStep = (eventHorizonRadius - innerCircleRadius) / quads;
	
	private List<Float> offsetList = new ArrayList<Float>();
	private long horizonStateChange = 0;
	
	private List<Float> sin = new ArrayList<Float>();
	private List<Float> cos = new ArrayList<Float>();
	
	private List<Float> quadRadius = new ArrayList<Float>();
	
	private InnerCircle innerCircle;
	private List<QuadStrip> quadStrips = new ArrayList<QuadStrip>();
		
	private void initEventHorizon() {
		for (int i=0; i<sections*(quads+1); i++) {
			offsetList.add( getRandomFloat() * 3 );
		}
		
		for (int i=0; i<=sections; i++) {
			sin.add( MathHelper.sin(sectionAngle * i) );
			cos.add( MathHelper.cos(sectionAngle * i) );
		}
		
		innerCircle = new InnerCircle();
		
		for (int i=0; i<=quads; i++) {
			quadRadius.add( innerCircleRadius + quadStep*i );
		}
		
		for (int i=0; i<quads; i++) {
			quadStrips.add( new QuadStrip(i) );
		}

		horizonStateChange = world.getTotalWorldTime();
	}
	
	
	private float getOffset(int index, float tick) {
		return MathHelper.sin( tick/4f + offsetList.get(index) ) / 24f;
	}
	
	private void renderEventHorizon(double x, double y, double z, double partialTicks, boolean white, Float alpha, boolean backOnly) {		
		float tick = (float) (world.getTotalWorldTime() - horizonStateChange + partialTicks);	
		
		GlStateManager.enableBlend();
		
		int k;
		
		if (backOnly)
			k = 1;
		else
			k = 0;
		
		for (; k<2; k++) {
			if (k == 1) {
				GlStateManager.popMatrix();
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y, z);
				GlStateManager.rotate(horizontalRotation-180, 0, 1, 0);
				//glColor4f(1,1,1,0.7f);
			}
			
			if (alpha == null)
				alpha = 0.0f;
			
			if (k == 1)
				alpha += 0.3f;
			
			
			if (white)
				innerCircle.render(tick, true, alpha);
			
			innerCircle.render(tick, false, 1.0f-alpha);
			
			for ( QuadStrip strip : quadStrips ) {
				if (white)
					strip.render(tick, true, alpha);
				
				strip.render(tick, false, 1.0f-alpha);
			}
		}
		
		GlStateManager.disableBlend();
	}
	
	private float toUV(float coord) {
		return (coord + 1) / 2f;
	}
	
	class InnerCircle {
		private List<Float> x = new ArrayList<Float>();
		private List<Float> y = new ArrayList<Float>();
		
		private List<Float> tx = new ArrayList<Float>();
		private List<Float> ty = new ArrayList<Float>();
		
		public InnerCircle() {
			float texMul = (innerCircleRadius / eventHorizonRadius);
			
			for (int i=0; i<sections; i++) {			
				x.add( sin.get(i) * innerCircleRadius );
				y.add( cos.get(i) * innerCircleRadius );
				
				tx.add( toUV( sin.get(i) * texMul ) );
				ty.add( toUV( cos.get(i) * texMul ) );
			}
		}
		
		public void render(float tick) {
			render(tick, false, null);
		}
		
		public void render(float tick, boolean white, Float alpha) {
			if (white) {
				GlStateManager.disableTexture2D();
				if (alpha > 0.5f)
					alpha = 1.0f - alpha;
			}
			
			glBegin(GL_TRIANGLE_FAN);
			
			if (alpha != null) glColor4f(1.0f, 1.0f, 1.0f, alpha.floatValue());
			if (!white) glTexCoord2f(0.5f, 0.5f);
			
			glVertex3f(0, 0, 0);
			
			int index = 0;
			for (int i=sections; i>=0; i--) {
				if (i == sections)
					index = 0;
				else
					index = i;
				
				if (!white) glTexCoord2f( tx.get(index), ty.get(index) );
				glVertex3f( x.get(index), y.get(index), getOffset(index, tick) );
			}

			glEnd();
			
			if (alpha != null) glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			if (white) GlStateManager.enableTexture2D();
		}
	}
	
	class QuadStrip {
		private List<Float> x = new ArrayList<Float>();
		private List<Float> y = new ArrayList<Float>();
		
		private List<Float> tx = new ArrayList<Float>();
		private List<Float> ty = new ArrayList<Float>();
		
		private int quadStripIndex;
		
		public QuadStrip(int quadStripIndex) {
			// this(quadStripIndex, quadRadius.get(quadStripIndex), quadRadius.get(quadStripIndex+1), false, 0);
			this( quadStripIndex, quadRadius.get(quadStripIndex), quadRadius.get(quadStripIndex+1), null );
		}
		
		public QuadStrip(int quadStripIndex, float innerRadius, float outerRadius/*, boolean randomizeRadius*/, Float tick) {
			this.quadStripIndex = quadStripIndex; 
			recalculate(innerRadius, outerRadius, tick);
		}
		
		public void recalculate(float innerRadius, float outerRadius, Float tick) {
			//this.quadStripIndex = quadStripIndex; 
			
			List<Float> radius = new ArrayList<Float>();
			List<Float> texMul = new ArrayList<Float>();
			
			/*radius.add( quadRadius.get( quadStripIndex   ) ); // Inner
			  radius.add( quadRadius.get( quadStripIndex+1 ) ); // Outer */
			
			radius.add( innerRadius );
			radius.add( outerRadius );
			
			for (int i=0; i<2; i++)
				texMul.add( radius.get(i) / eventHorizonRadius );
			
			for (int k=0; k<2; k++) {
				for (int i=0; i<sections; i++) {
					float rad = radius.get(k);
					
					if (tick != null) {
						rad += getOffset(i, tick) * 2;
					}
					
					x.add( rad * sin.get(i) );
					y.add( rad * cos.get(i) );
					
					tx.add( toUV( sin.get(i) * texMul.get(k) ) );
					ty.add( toUV( cos.get(i) * texMul.get(k) ) );
				}
			}
		}
		
		public void render(float tick) {
			render(tick, false, null);
		}
		
		public void render(float tick, boolean white, Float alpha) {
			render(tick, null, null, white, alpha);
		}
		
		public void render(float tick, Float outerZ, Float innerZ) {
			render(tick, outerZ, innerZ, false, null);
		}
		
		public void render(float tick, Float outerZ, Float innerZ, boolean white, Float alpha) {
			if (white) {
				GlStateManager.disableTexture2D();
				if (alpha > 0.5f)
					alpha = 1.0f - alpha;
			}
			
			if (alpha != null) glColor4f(1.0f, 1.0f, 1.0f, alpha.floatValue());
			
			glBegin(GL_QUAD_STRIP);
			
			int index = 0;
			
			for (int i=sections; i>=0; i--) {
				if (i == sections)
					index = 0;
				else
					index = i;
				
				float z;
				
				if (outerZ != null) z = outerZ.floatValue();
				else z = getOffset(index + sections*quadStripIndex, tick);
				
				if (!white) glTexCoord2f( tx.get(index), ty.get(index) );
				glVertex3f( x.get(index), y.get(index),  z );
				
				index = index + sections;
				
				if (innerZ != null) z = innerZ.floatValue();
				else z = getOffset(index + sections*quadStripIndex, tick);
				
				if (!white) glTexCoord2f( tx.get(index), ty.get(index) );
				glVertex3f( x.get(index), y.get(index), z );
			}
			
			glEnd();
			
			if (white) GlStateManager.enableTexture2D();
			if (alpha != null) glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
}
