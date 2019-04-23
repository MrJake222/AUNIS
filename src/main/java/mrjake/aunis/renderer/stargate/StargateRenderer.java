package mrjake.aunis.renderer.stargate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.stargate.StargateRendererStatic.QuadStrip;
import mrjake.aunis.renderer.state.StargateRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.stargate.merge.MergeHelper;
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
			chevronTextureList.add(textureTemplate + "0.png");
		
		// Load chevron textures
		for (int i=0; i<=10; i++)
			ModelLoader.getTexture("stargate/chevron/chevron" + i + ".png");
		
		}
	
	@Override
	public float getHorizontalRotation() {
		return horizontalRotation;
	}
	
	StargateRendererState state = new StargateRendererState();
	
	@Override
	public void setState(StargateRendererState state) {
		this.state = state;
		
		setActiveChevrons(state.getActiveChevrons(), state.isFinalActive());
		
		this.ringRollLoopPlayed = state.spinState.isSpinning;
		ringSpinHelper = new StargateRingSpinHelper(world, pos, this, state.spinState);
		
		
		AunisSoundHelper.playPositionedSound("wormhole", pos, state.doEventHorizonRender);
		AunisSoundHelper.playPositionedSound("ringRollStart", pos, false);
		AunisSoundHelper.playPositionedSound("ringRollLoop", pos, state.spinState.isSpinning);
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

//	private double state.ringAngularRotation;
	private boolean ringRollLoopPlayed;
//	
//	boolean state.dialingComplete;
	
	private StargateRingSpinHelper ringSpinHelper;
	
	private StargateRingSpinHelper getRingSpinHelper() {
		if (ringSpinHelper == null)
			ringSpinHelper = new StargateRingSpinHelper(world, pos, this, state.spinState);
		
		return ringSpinHelper;
	}
	
	private long lastFailSoundPlayed = 0;
	
	public void setRingSpin(boolean spin, boolean dialingComplete) {
		this.state.dialingComplete = dialingComplete;
				
		if (!state.dialingComplete && (world.getTotalWorldTime() - lastFailSoundPlayed) > 54) {
			lastFailSoundPlayed = world.getTotalWorldTime();
			
			AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.gateDialFail);
		}
		
		if (spin) {
			AunisSoundHelper.playPositionedSound("ringRollStart", pos, true);
			ringRollLoopPlayed = false;

			getRingSpinHelper().requestStart(state.ringAngularRotation);
			
//			ringSpinStart = world.getTotalWorldTime();
//			lastTick = -1;
//			
//			ringDecelerating = false;
//			ringAccelerating = true;
//			ringSpin = true;
			
		}
		
		else {
			ringRollLoopPlayed = true;
			
			AunisSoundHelper.playPositionedSound("ringRollStart", pos, false);
			AunisSoundHelper.playPositionedSound("ringRollLoop", pos, false);
						
			Aunis.info("requesting stop");
			getRingSpinHelper().requestStop();
			
//			lockSoundPlayed = false;
//			ringDecelFirst = true;
//			
//			ringAccelerating = false;
//			ringDecelerating = true;
		}
	}
	
	private void renderRing(double x, double y, double z, double partialTicks) {
//		ModelLoader.loadModel(EnumModel.RING_MODEL);
		
		Model ringModel = ModelLoader.getModel(EnumModel.RING_MODEL);
		
		if (ringModel != null) {
			
			if (state.spinState.isSpinning) {				
				// Play looped ring sound
				// ringRollStart duration in 4.891s
				// 4.891 * 20 = 98 ticks
				if (!ringRollLoopPlayed && (world.getTotalWorldTime() - state.spinState.tickStart) > 98) {
					ringRollLoopPlayed = true;
										
					AunisSoundHelper.playPositionedSound("ringRollLoop", pos, true);
				}
				
				state.ringAngularRotation = getRingSpinHelper().spin(partialTicks) % 360;
				
//				Aunis.info("state.ringAngularRotation: " + state.ringAngularRotation);
			}
			
			GlStateManager.pushMatrix();
			
//			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			float angularRotation = (float) state.ringAngularRotation;
			
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
		AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.chevronIncoming);
		
		this.chevronsToLightUp = chevronsToLightUp;
		this.state.dialingComplete = true;
		
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
			if (state.dialingComplete)
				activationStateChange += 10;
			else
				activationStateChange += 40;
		}
		
		activation = 0;
	}
	
	public void moveFinalChevron() {
		finalChevronStart = 0;
		finalChevronMove = true;
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
	
	// private List<Boolean> LastChevronActiveList;
	
	private void renderChevrons(double x, double y, double z, double partialTicks) {
		for (int i=0; i<9; i++)
			renderChevron(x, y, z, i, partialTicks);
		
		if (activation != -1) {
			int stage = (int) (((world.getTotalWorldTime() - activationStateChange) + partialTicks) * 3);

			if (stage < 0) return;
			
//			Aunis.info("Stage: " + stage);
			
			if (stage < 11) {
				if (changingChevrons) {
					for (int i=0; i<9; i++) {
						
						if (clearingChevrons) {
							if ( !chevronTextureList.get(i).contains("n0") ) {
								chevronTextureList.set(i, textureTemplate+(10-stage)+".png");
							}
						}
						
						else {
							if ( !chevronTextureList.get(i).contains("10") && (i < chevronsToLightUp-1 || i == 8) ) {
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
						activeChevrons = chevronsToLightUp;
					
					// This is needed because if the gate isn't rendered(player not looking at it)
					// render code isn't running and gate can't perform any animation(not necessary in this case)
					// So we need to check if chevron's states didn't change and perform correction
					
					if (clearingChevrons) {
						String tex = textureTemplate+"0.png";

						for (int i=0; i<9; i++) {
//							BlockPos chevPos = MergeHelper.rotateAndGlobal(MergeHelper.chevronBlocks.get(0), facing, pos);
//							StargateMemberTile memberTile = (StargateMemberTile) world.getTileEntity(chevPos);
//							
//							memberTile.setLitUpClient(false);
							
							chevronTextureList.set(i, tex);
						}
					}
					
					else {
						String tex = textureTemplate+"10.png";
						
						for (int i=0; i<chevronsToLightUp-1; i++)
							chevronTextureList.set(i, tex);
						
						chevronTextureList.set(8, tex);
					}
				}
				
				else {
					chevronTextureList.set(activation, textureTemplate+"10.png");
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
	
	private final float speedFactor = 6f;
	
	private QuadStrip backStrip;
	private boolean backStripClamp;
	
//	private boolean state.doEventHorizonRender = false;
	private Float whiteOverlayAlpha;
	
	private float gateWaitStart = 0;
//	private boolean state.openingSoundPlayed;
	
	private long gateWaitClose = 0;
	private boolean zeroAlphaSet;	
			
	public void openGate() {
		gateWaitStart = world.getTotalWorldTime();
		state.openingSoundPlayed = false;
		
		zeroAlphaSet = false;
		backStripClamp = true;
		whiteOverlayAlpha = 1.0f;
		
		state.vortexState = EnumVortexState.FORMING;
		
		kawooshStart = world.getTotalWorldTime();
		state.doEventHorizonRender = true;
	}
	
	public void closeGate() {
		AunisSoundHelper.playPositionedSound("wormhole", pos, false);
		
		AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.gateClose);
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
	
//	private EnumVortexState state.vortexState = EnumVortexState.FORMING;
	
	private void engageGate() {
		state.vortexState = EnumVortexState.STILL;
		AunisSoundHelper.playPositionedSound("wormhole", pos, true);
	}
	
	public void unstableHorizon(boolean unstable) {
		state.horizonUnstable = unstable;
	}
		
	private void renderKawoosh(double x, double y, double z, double partialTicks) {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		float gateWait = world.getTotalWorldTime() - gateWaitStart;
		
		if ( gateWait < 25 )
			return;
		
		// This must be done here, because we wait a little before playing any sound
		if ( !state.openingSoundPlayed && gateWait < 30 ) {
			state.openingSoundPlayed = true;
			
			AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.gateOpen);
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
				
				boolean horizonUnstable;
				
				
				if (state.horizonUnstable)
					horizonUnstable = getHorizonFlashing(partialTicks);
				else
					horizonUnstable = false;
				
				
				if (horizonUnstable)
					ModelLoader.bindTexture(ModelLoader.getTexture("stargate/event_horizon_by_mclatchyt_2_unstable.jpg"));

//				if ( state.vortexState.equals(EnumVortexState.CLOSING) || state.vortexState == EnumVortexState.SHRINKING )
//					renderEventHorizon(x, y, z, partialTicks, true, whiteOverlayAlpha, true, 1);
//				else				
//					renderEventHorizon(x, y, z, partialTicks, false, 0.0f, false, horizonUnstable ? 2f : 1);
				
				if ( state.vortexState.equals(EnumVortexState.CLOSING) )
					renderEventHorizon(x, y, z, partialTicks, true, whiteOverlayAlpha, false, 1.7f);
				else
					renderEventHorizon(x, y, z, partialTicks, false, null, false, horizonUnstable ? 1.5f : 1);
					
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
	
	private long lastFlashEnded = 0;
	private long nextFlashIn = 0;
	private long flashDuration1 = 0;
	private long flashDuration2 = 0;
	
	private boolean soundPlayed = false;
		
	private boolean getHorizonFlashing(double partialTicks) {
		float tick = (float) (world.getTotalWorldTime() + partialTicks);
		
		tick -= lastFlashEnded;
		
		if (tick > 100) {
			resetFlashing();
			
			Aunis.info("setka!");

			return false;
		}
		
		tick -= nextFlashIn;
		
//		Aunis.info("tick: " + tick + "\t\tflashDuration2*4:  " + (flashDuration2 * 4));
		
		if (tick > 0) {
			if (!soundPlayed) {
				soundPlayed = true;
				
				Aunis.info("sound");
				AunisSoundHelper.playSound((WorldClient) world, pos.up(), AunisSoundHelper.wormholeFlicker);
			}
			
			if (tick < flashDuration1) return true;
			if (tick < flashDuration1 * 2) return false;
			if (tick < flashDuration2 * 3) return true;
			if (tick < flashDuration2 * 4) return true;
			else {
				resetFlashing();
				
				Aunis.info("nextFlashIn: " + nextFlashIn + ", flashDuration1: " + flashDuration1 + ", flashDuration2: " + flashDuration2);
				
				return false;
			}
		}
		
		return false;
	}
	
	private void resetFlashing() {
		lastFlashEnded = world.getTotalWorldTime();
		nextFlashIn = (long)(Math.random() * 40) + 5;
		flashDuration1 = (long)(Math.random() * 4) + 1;
		flashDuration2 = (long)(Math.random() * 4) + 1;
		
		soundPlayed = false;
	}
}
