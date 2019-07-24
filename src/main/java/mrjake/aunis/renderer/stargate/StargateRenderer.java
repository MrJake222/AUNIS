package mrjake.aunis.renderer.stargate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.StargateActivation;
import mrjake.aunis.renderer.stargate.StargateRendererStatic.QuadStrip;
import mrjake.aunis.renderer.state.StargateRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisPositionedSound;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.MergeHelper;
import mrjake.aunis.state.SpinStateRequest;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class StargateRenderer implements ISpecialRenderer<StargateRendererState> {
	// private StargateBaseTile te;
	private World world;
	private BlockPos pos;
	
	private static final Vec3d ringLoc = new Vec3d(0.0, -0.122333, -0.000597);
	private EnumFacing facing;	
	private int horizontalRotation;	
		
	public StargateRenderer(StargateBaseTile te) {
		// this.te = te;		
		this.world = te.getWorld();
		this.pos = te.getPos();
		
//		this.state.ringAngularRotation = 0;
		
		facing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
		
		if ( facing.getAxis().getName() == "x" )
			horizontalRotation = (int) facing.getOpposite().getHorizontalAngle();
		else
			horizontalRotation = (int) facing.getHorizontalAngle();
		
		for (int i=0; i<9; i++)
			chevronTextureList.add(CHEVRON_TEXTURE_BASE + "0.png");
		
		// Load chevron textures
		for (int i=0; i<=10; i++)
			ModelLoader.getTexture("stargate/chevron/chevron" + i + ".png");
		
		}
	
	@Override
	public float getHorizontalRotation() {
		return horizontalRotation;
	}
	
	private StargateRendererState state = new StargateRendererState();
	
	public StargateRendererState getState() {
		return state;
	}
	
	@Override
	public void setState(StargateRendererState state) {		
		this.state = state;
		
		ringAngularRotation = state.ringCurrentSymbol.angle;
		
		setActiveChevrons(state.getActiveChevrons(), state.isFinalActive());
		
		ringSpinHelper = new StargateRingSpinHelper(world, pos, this, state.spinState);
		
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.WORMHOLE, pos, state.doEventHorizonRender);
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.RING_ROLL_START, pos, false);
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.RING_ROLL_LOOP, pos, state.spinState.isSpinning);
	}
	
	private int skyLight;
	private int blockLight;
	
	public static List<BlockPos> chevronBlocks = Arrays.asList(
			new BlockPos(-4, 2, 0), 
			new BlockPos(-5, 5, 0), 
			new BlockPos(5, 5, 0), 
			new BlockPos(4, 2, 0) ); 
	
	private void calculateLightMap(float partialTicks) {
//		int subt = world.calculateSkylightSubtracted(partialTicks);
		skyLight = 0;
		blockLight = 0;
		
		for (int i=0; i<chevronBlocks.size(); i++) {
			BlockPos blockPos = MergeHelper.rotateAndGlobal(chevronBlocks.get(i), world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL), pos); //((int) world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL).getHorizontalAngle(), pos);
			
			skyLight += world.getLightFor(EnumSkyBlock.SKY, blockPos);// - subt;
			blockLight += world.getLightFor(EnumSkyBlock.BLOCK, blockPos);
		}
		
		skyLight /= 4;
		blockLight /= 4;
	}
	
	@Override
	public void render(double x, double y, double z, double partialTicks) {
		IBlockState state = world.getBlockState(pos);
		
		if (state.getBlock() instanceof StargateBaseBlock) {
			if (state.getValue(AunisProps.RENDER_BLOCK))
				return;
			
			calculateLightMap((float) partialTicks);
			
			int clamped = MathHelper.clamp(skyLight+blockLight, 0, 15);
			
//			int clamped = (skyLight+blockLight) / 2;
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight * 16, clamped * 16);
			
			renderGate(x, y, z);
			renderRing(x, y, z, partialTicks);
			renderChevrons(x, y, z, partialTicks);
			
			if (this.state.doEventHorizonRender)
				renderKawoosh(x, y, z, partialTicks);
		}
	}
	
	private void renderGate(double x, double y, double z) {		
		Model gateModel = ModelLoader.getModel( EnumModel.GATE_MODEL );
		
		if ( gateModel != null ) {
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			EnumModel.GATE_MODEL.bindTexture();
			
			gateModel.render();
			
			GlStateManager.popMatrix();
		}
	}

	private StargateRingSpinHelper ringSpinHelper;
	
	private StargateRingSpinHelper getRingSpinHelper() {
		if (ringSpinHelper == null)
			ringSpinHelper = new StargateRingSpinHelper(world, pos, this, state.spinState);
		
		return ringSpinHelper;
	}
	
	private long lastFailSoundPlayed = 0;
	
	public void setRingSpin(boolean spin, boolean dialingComplete, SpinStateRequest stateRequest) {
		this.state.dialingComplete = dialingComplete;
				
		if (!state.dialingComplete && (world.getTotalWorldTime() - lastFailSoundPlayed) > 54) {
			lastFailSoundPlayed = world.getTotalWorldTime();
			
			AunisSoundHelper.playSoundEventClientSide((WorldClient) world, pos, EnumAunisSoundEvent.GATE_DIAL_FAILED, 0.3f);
		}
		
		if (spin) {
			AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.RING_ROLL_START, pos, true);
			
			if (state.spinState.isSpinning)
				ringAngularRotation = getRingSpinHelper().spin(0);
			
			state.spinState.direction = stateRequest.direction;
			state.spinState.targetSymbol = stateRequest.targetSymbol;
			state.spinState.finalChevron = stateRequest.lock;
			
			getRingSpinHelper().requestStart(ringAngularRotation, state.spinState.direction, stateRequest.targetSymbol, stateRequest.lock, false);		
		}
		
		else {
			stopRingSounds();
						
			getRingSpinHelper().requestStop();
		}
	}
	
	public void setRingSpin(boolean spin, boolean dialingComplete) {
		setRingSpin(spin, dialingComplete, new SpinStateRequest());
	}
	
	public void requestStopByComputer(long worldTicks, boolean moveOnly) {
		getRingSpinHelper().requestStopByComputer(worldTicks, moveOnly);
	}
	
	public void stopRingSounds() {		
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.RING_ROLL_START, pos, false);
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.RING_ROLL_LOOP, pos, false);
	}
	
	private boolean waitForFinalMove;
	private long waitForFinalMoveStart;

	public void requestFinalMove(long finalMoveStart, boolean finalChevron) {
		waitForFinalMoveStart = finalMoveStart;
		
		waitForFinalMove = true;
		state.spinState.finalChevron = finalChevron;
	}
	
	public void addComputerActivation(long finalMoveStart, boolean finalChevron) {
		long start = finalMoveStart + (finalChevron ? 20 : 15);		
		Activation dimActivation = new StargateActivation(8, start + 19 + 3, true).inactive();
		
		activationList.add(new StargateActivation(8, start) {
			@Override
			protected void onActivated() {				
				if (!finalChevron) {
					activateNextChevron(false);
					dimActivation.active();
				}
				
				else {
					activeChevrons++;
				}
			}
		});
		
		if (!finalChevron)
			activationList.add(dimActivation);
	}
	
	private double ringAngularRotation;
	
	private void renderRing(double x, double y, double z, double partialTicks) {
//		ModelLoader.loadModel(EnumModel.RING_MODEL);
		
		Model ringModel = ModelLoader.getModel(EnumModel.RING_MODEL);
				
		if (ringModel != null) {
			
			if (state.spinState.isSpinning) {				
				ringAngularRotation = (float) (getRingSpinHelper().spin(partialTicks) % 360);
			}
			
			if (waitForFinalMove && (world.getTotalWorldTime() - waitForFinalMoveStart) >= (state.spinState.finalChevron ? 20 : 15)) {
				waitForFinalMove = false;
								
				long start = waitForFinalMoveStart + (state.spinState.finalChevron ? 20 : 15);
				moveFinalChevron(start, true);
			}
			
			GlStateManager.pushMatrix();
			
//			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			float angularRotation = (float) ringAngularRotation;
			
			if (horizontalRotation == 90 || horizontalRotation == 0)
				angularRotation *= -1;

			
			if (horizontalRotation == 90 || horizontalRotation == 270) {
				GlStateManager.translate(x+ringLoc.y, y+ringLoc.z, z+ringLoc.x);
				GlStateManager.rotate((float) angularRotation, 1, 0, 0);
				GlStateManager.translate(-ringLoc.y, -ringLoc.z, -ringLoc.x);
			}
			
			else {
				GlStateManager.translate(x+ringLoc.x, y+ringLoc.z, z+ringLoc.y);
				GlStateManager.rotate((float) angularRotation, 0, 0, 1);
				GlStateManager.translate(-ringLoc.x, -ringLoc.z, -ringLoc.y);
			}
			
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			
			EnumModel.RING_MODEL.bindTexture();
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
	
	private List<String> chevronTextureList = new ArrayList<String>(9);
	private static final String CHEVRON_TEXTURE_BASE = "stargate/chevron/chevron";
	
	private List<Activation> activationList = new ArrayList<>();
	
	private int activeChevrons = 0;
	
	private float finalChevronStart;
	private boolean finalChevronMove;
	
	public void activateFinalChevron(boolean setRingSpin) {
		activationList.add(new StargateActivation(8, world.getTotalWorldTime()));
		activeChevrons++;
		
		if (setRingSpin)
			setRingSpin( false, true );
	}
	
	public void activateFinalChevron() {
		activateFinalChevron(true);
	}
	
	public void activateNextChevron(boolean setRingSpin) {		
		activationList.add(new StargateActivation(activeChevrons, world.getTotalWorldTime()));
					
		if (activeChevrons == 0 && setRingSpin) {
			setRingSpin( true, true );
		}
		
		if (activeChevrons < 8)
			activeChevrons++;
	}
	
	public void activateNextChevron() {
		activateNextChevron(true);
	}
	
	public void clearChevrons(Long stateChange) {
		changeChevrons(true, stateChange, activeChevrons, isLastChevronActive());
	}
	
	public void clearChevrons() {
		clearChevrons(null);
	}
	
	public void lightUpChevrons(int chevronsToLightUp) {
		AunisSoundHelper.playSoundEventClientSide((WorldClient) world, pos, EnumAunisSoundEvent.CHEVRON_INCOMING, 0.5f);
		
		this.state.dialingComplete = true;
		
		changeChevrons(false, null, chevronsToLightUp, true);
	}
	
	private void changeChevrons(boolean clear, Long stateChange, int chevronsToChange, boolean changeFinal) {			
		long activationStateChange;

		if (stateChange != null)
			activationStateChange = stateChange;
		else
			activationStateChange = world.getTotalWorldTime();
		
		if (clear) {
			if (state.dialingComplete)
				activationStateChange += 10;
			else
				activationStateChange += 40;
		}

		activeChevrons = clear ? 0 : chevronsToChange;
		
		if (isLastChevronActive() || changeFinal)
			chevronsToChange--;
		
		for (int i=0; i<chevronsToChange; i++) {
			activationList.add(new StargateActivation(i, activationStateChange, clear));
		}
		
		if (changeFinal)
			activationList.add(new StargateActivation(8, activationStateChange, clear));
	}
	
	public void moveFinalChevron(long finalChevronStart, boolean computer) {
		this.finalChevronStart = finalChevronStart;
		finalChevronMove = true;
	}
	
	public void moveFinalChevron(long finalChevronStart) {
		moveFinalChevron(finalChevronStart, false);
	}
	
	private void renderChevron(double x, double y, double z, int index, double partialTicks) {
//		ModelLoader.loadModel(EnumModel.GATE_MODEL);
		
		Model ChevronLight = ModelLoader.getModel( EnumModel.ChevronLight );
		Model ChevronFrame = ModelLoader.getModel( EnumModel.ChevronFrame );
		Model ChevronMoving = ModelLoader.getModel( EnumModel.ChevronMoving );
		Model ChevronBack = ModelLoader.getModel( EnumModel.ChevronBack );
		
		if ( ChevronLight != null && ChevronFrame != null && ChevronMoving != null && ChevronBack != null ) {
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			int angularPosition = EnumChevron.getRotation(index);
			GlStateManager.rotate(angularPosition, 0, 0, 1);
			
			ModelLoader.bindTexture( chevronTextureList.get(index) );
						
			if (index == 8 && finalChevronMove) {
								
				float tick = (float) (world.getTotalWorldTime() - finalChevronStart + partialTicks);
				float arg = tick / 6.0f;
				
				float finalChevronOffset = 0;
				
				if (arg < 0)
					finalChevronOffset = 0;
				
				else if (arg <= Math.PI/2)
					finalChevronOffset = MathHelper.sin( arg ) / 12f;
				
				else if (arg <= Math.PI)
					finalChevronOffset = 0.08333f; // 1 / 12
				
				else if (arg <= 3*Math.PI/2)
					finalChevronOffset = -MathHelper.cos( arg ) / 12f;
				
				else {
					finalChevronOffset = 0;
					finalChevronMove = false;
				}
								
				GlStateManager.pushMatrix();
				
				GlStateManager.translate(0, finalChevronOffset, 0);
				ChevronLight.render();
			
				GlStateManager.translate(0, -2*finalChevronOffset, 0);
				ChevronMoving.render();
				
				GlStateManager.popMatrix();
			}
			
			else {
				ChevronLight.render();	
				ChevronMoving.render();
			}
			
			EnumModel.ChevronFrame.bindTexture();
			ChevronFrame.render();
			
			EnumModel.ChevronBack.bindTexture();
			ChevronBack.render();
			
			GlStateManager.popMatrix();
		}
	}
		
	private void renderChevrons(double x, double y, double z, double partialTicks) {
		for (int i=0; i<9; i++)
			renderChevron(x, y, z, i, partialTicks);
		
		Activation.iterate(activationList, world.getTotalWorldTime(), partialTicks, (index, stage) -> {
			chevronTextureList.set(index, CHEVRON_TEXTURE_BASE + stage + ".png");
		});
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
			String tex = CHEVRON_TEXTURE_BASE;
			
			if ( i < activeChevrons || (i == 8 && lastChevronActive) )
				tex += "10.png";
			else
				tex += "0.png";
						
			chevronTextureList.add(tex);
		}
	}
	
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
		gateWaitStart = world.getTotalWorldTime();
		
		zeroAlphaSet = false;
		backStripClamp = true;
		whiteOverlayAlpha = 1.0f;
		
		state.vortexState = EnumVortexState.FORMING;
		
		kawooshStart = world.getTotalWorldTime();
		state.doEventHorizonRender = true;
	}
	
	public void closeGate() {
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.WORMHOLE, pos, false);
		
		AunisSoundHelper.playSoundEventClientSide((WorldClient) world, pos, EnumAunisSoundEvent.GATE_CLOSE, 0.3f);
		gateWaitClose = world.getTotalWorldTime();
		
		state.vortexState = EnumVortexState.CLOSING;
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
		
	private void engageGate() {
		state.vortexState = EnumVortexState.STILL;
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.WORMHOLE, pos, true);
	}
		
	private void renderKawoosh(double x, double y, double z, double partialTicks) {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		float gateWait = world.getTotalWorldTime() - gateWaitStart;
		
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
					state.vortexState = EnumVortexState.FORMING;
				else if (argState < 4.15f)
					state.vortexState = EnumVortexState.FULL;
				else if (argState < 5.898f)
					state.vortexState = EnumVortexState.DECREASING;
				else if ( state.vortexState != EnumVortexState.CLOSING )
					engageGate();
			}

			float prevZ = 0;
			float prevRad = 0;
			
			boolean first = true;
			
			if ( !state.vortexState.equals(EnumVortexState.STILL) ) {
				float arg = (tick - vortexStart) / speedFactor;
								
				if ( !state.vortexState.equals(EnumVortexState.CLOSING) ) {
					if ( !state.vortexState.equals(EnumVortexState.SHRINKING) ) {
						if ( state.vortexState.equals(EnumVortexState.FORMING) && arg >= 1.342f ) {
							state.vortexState = EnumVortexState.FULL;
						}
						
						// Offset of the end of the function domain used to generate vortex 
						float end = 0.75f;
						
						if ( state.vortexState.equals(EnumVortexState.DECREASING) && arg >= 5.398+end ) {
							engageGate();
						}
						
						if ( state.vortexState.equals(EnumVortexState.FULL) ) {				
							if ( arg >= 3.65f+end ) {
								state.vortexState = EnumVortexState.DECREASING;
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
							if ( state.vortexState.equals(EnumVortexState.FORMING) )
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
								state.doEventHorizonRender = false;							
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
							
							state.vortexState = EnumVortexState.SHRINKING;
						}
					}
				}
			} // not still if
		}
						
		// Rendering proper event horizon or the <backStrip> for vortex
		if (state.vortexState != null) {
			if ( state.vortexState.equals(EnumVortexState.STILL) || state.vortexState.equals(EnumVortexState.CLOSING) ) {
				
				if (horizonUnstable)
					ModelLoader.bindTexture(ModelLoader.getTexture("stargate/event_horizon_by_mclatchyt_2_unstable.jpg"));

//				if ( state.vortexState.equals(EnumVortexState.CLOSING) || state.vortexState == EnumVortexState.SHRINKING )
//					renderEventHorizon(x, y, z, partialTicks, true, whiteOverlayAlpha, true, 1);
//				else				
//					renderEventHorizon(x, y, z, partialTicks, false, 0.0f, false, horizonUnstable ? 2f : 1);
				
				if ( state.vortexState.equals(EnumVortexState.CLOSING) )
					renderEventHorizon(x, y, z, partialTicks, true, whiteOverlayAlpha, false, 1.7f);
				else
					renderEventHorizon(x, y, z, partialTicks, false, null, false, horizonUnstable ? 1.2f : 1);
					
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
			
			renderEventHorizon(x, y, z, partialTicks, false, 0.0f, true, 1.0f);
			
			GlStateManager.disableBlend();
		}
		
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
	
	private void renderEventHorizon(double x, double y, double z, double partialTicks, boolean white, Float alpha, boolean backOnly, float mul) {			
		float tick = (float) (world.getTotalWorldTime() + partialTicks) * mul;	
		
	    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		
		for (int k=(backOnly ? 1 : 0); k<2; k++) {
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
