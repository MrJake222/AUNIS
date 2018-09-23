package mrjake.aunis.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.block.BlockFaced;
import mrjake.aunis.block.BlockTESRMember;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.stateUpdate.StateUpdateToServer;
import mrjake.aunis.packet.upgrade.UpgradeTileUpdateToServer;
import mrjake.aunis.renderer.RendererInit.QuadStrip;
import mrjake.aunis.renderer.state.LimitedStargateRendererState;
import mrjake.aunis.renderer.state.StargateRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.stargate.merge.BlockPosition;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public class StargateRenderer implements Renderer<StargateRendererState> {
	// private StargateBaseTile te;
	private World world;
	private BlockPos pos;
	
	private static final Vec3d ringLoc = new Vec3d(0.0, -0.122333, -0.000597);
	private int horizontalRotation;	
	
	public StargateRenderer(StargateBaseTile te) {
		// this.te = te;		
		this.world = te.getWorld();
		this.pos = te.getPos();
		
		this.ringAngularRotation = 0;
		
		EnumFacing facing = world.getBlockState(pos).getValue(BlockTESRMember.FACING);
		
		if ( facing.getAxis().getName() == "x" )
			horizontalRotation = (int) facing.getOpposite().getHorizontalAngle();
		else
			horizontalRotation = (int) facing.getHorizontalAngle();
		
		for (int i=0; i<9; i++) {
			chevronTextureList.add(textureTemplate + "0.png");
		}
	}
	
	@Override
	public void setState(StargateRendererState state) {
		setActiveChevrons(state.activeChevrons, state.isFinalActive);
		
		ringAngularRotation = state.ringAngularRotation;
		/*ringSpin = state.ringSpin;
		ringSpinStart = state.ringSpinStart;*/
		
		vortexState = state.vortexState;
		soundPlayed = state.soundPlayed;
		doEventHorizonRender = state.doEventHorizonRender;
		dialingComplete = state.dialingComplete;
		
		AunisSoundHelper.playPositionedSound("wormhole", pos, vortexState == EnumVortexState.STILL);
		AunisSoundHelper.playPositionedSound("ringRoll", pos, ringSpin);
	}
	
	private int skyLight;
	private int blockLight;
	
	public static List<BlockPosition> chevronBlocks = Arrays.asList(
			new BlockPosition(-4, 2, 0), 
			new BlockPosition(-5, 5, 0), 
			new BlockPosition(5, 5, 0), 
			new BlockPosition(4, 2, 0) ); 
	
	private void calculateLightMap(float partialTicks) {
		int subt = world.calculateSkylightSubtracted(partialTicks);
		skyLight = 0;
		blockLight = 0;
		
		for (int i=0; i<chevronBlocks.size(); i++) {
			BlockPos blockPos = chevronBlocks.get(i).rotateAndGlobal((int) world.getBlockState(pos).getValue(BlockFaced.FACING).getHorizontalAngle(), pos);
			
			skyLight += world.getLightFor(EnumSkyBlock.SKY, blockPos) - subt;
			blockLight += world.getLightFor(EnumSkyBlock.BLOCK, blockPos);
		}
		
		skyLight /= 4;
		blockLight /= 4;
	}
	
	@Override
	public void render(double x, double y, double z, double partialTicks) {
		IBlockState state = world.getBlockState(pos);
		
		if (state.getBlock() instanceof StargateBaseBlock) {
			if (state.getValue(BlockTESRMember.RENDER))
				return;
			
			calculateLightMap((float) partialTicks);
			
			int clamped = MathHelper.clamp(skyLight+blockLight, 0, 15);
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight * 16, clamped * 16);
			
			renderGate(x, y, z);
			renderRing(x, y, z, partialTicks);
			renderChevrons(x, y, z, partialTicks);
			
			if (doEventHorizonRender)
				renderKawoosh(x, y, z, partialTicks);
			
			if (doUpgradeRender)
				renderUpgrade(x, y, z, partialTicks);
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
	
	public void setRingSpin(boolean spin, boolean dialingComplete) {
		this.dialingComplete = dialingComplete;
		
		if (!dialingComplete)
			AunisSoundHelper.playSound(world, pos, AunisSoundHelper.gateDialFail);
		
		if (spin) {
			AunisSoundHelper.playPositionedSound("ringRoll", pos, true);
			
			ringSpinStart = world.getTotalWorldTime();
			lastTick = -1;
			
			ringDecelerating = false;
			ringAccelerating = true;
			ringSpin = true;
		}
		
		else {
			AunisSoundHelper.playPositionedSound("ringRoll", pos, false);
			
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
								AunisSoundHelper.playSound(world, pos, AunisSoundHelper.chevronLockDHD);
							}
						}
						
						if ( tickDecel <= maxTick ) {
							anglePerTick *= MathHelper.cos( tickDecel * accelerationMul );
						}
						
						else {
							ringDecelerating = false;
							ringSpin = false;
							lastTick = -1;
							
							AunisPacketHandler.INSTANCE.sendToServer( new StateUpdateToServer(new LimitedStargateRendererState(pos, ringAngularRotation)) );
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
		AunisSoundHelper.playSound(world, pos, AunisSoundHelper.chevronIncoming);
		
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
	
	private boolean doEventHorizonRender = false;
	private Float whiteOverlayAlpha;
	
	private float gateWaitStart = 0;
	private boolean soundPlayed;
	
	private long gateWaitClose = 0;
	private boolean zeroAlphaSet;	
			
	public void openGate() {
		gateWaitStart = world.getTotalWorldTime();
		soundPlayed = false;
		
		zeroAlphaSet = false;
		backStripClamp = true;
		whiteOverlayAlpha = 1.0f;
		
		vortexState = EnumVortexState.FORMING;
		
		kawooshStart = world.getTotalWorldTime();
		doEventHorizonRender = true;
	}
	
	public void closeGate() {
		AunisSoundHelper.playPositionedSound("wormhole", pos, false);
		
		AunisSoundHelper.playSound(world, pos, AunisSoundHelper.gateClose);
		gateWaitClose = world.getTotalWorldTime();
		
		vortexState = EnumVortexState.CLOSING;
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
		AunisSoundHelper.playPositionedSound("wormhole", pos, true);
	}
	
	private void renderKawoosh(double x, double y, double z, double partialTicks) {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		float gateWait = world.getTotalWorldTime() - gateWaitStart;
		
		if ( gateWait < 25 )
			return;
		
		// This must be done here, because we wait a little before playing any sound
		if ( !soundPlayed && gateWait < 30 ) {
			soundPlayed = true;
			
			AunisSoundHelper.playSound(world, pos, AunisSoundHelper.gateOpen);
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
		
		float inner = Aunis.getRendererInit().eventHorizonRadius - tick/3.957f;
		
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
		if (inner >= Aunis.getRendererInit().kawooshRadius) {
			backStrip = Aunis.getRendererInit().new QuadStrip(0, inner - 0.2f, Aunis.getRendererInit().eventHorizonRadius, tick);
		}
		
		else {
			if (backStripClamp) {
				// Clamping to the desired size
				backStripClamp = false;
				backStrip = Aunis.getRendererInit().new QuadStrip(0, Aunis.getRendererInit().kawooshRadius - 0.2f, Aunis.getRendererInit().eventHorizonRadius, null);
				
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
						for ( Map.Entry<Float, Float> e : Aunis.getRendererInit().Z_RadiusMap.entrySet() ) {
							if (first) {
								first = false;
								prevZ = e.getKey();
								prevRad = e.getValue();
							}
							
							else {
								float zOffset = e.getKey();
								float rad = e.getValue();
								
								// Aunis.getRendererInit().new QuadStrip(0, rad, prevRad, tick).render(tick, zOffset*mul, prevZ*mul);
								Aunis.getRendererInit().new QuadStrip(0, rad, prevRad, tick).render(tick, zOffset*mul, prevZ*mul, false, 1.0f - whiteOverlayAlpha);
								
								prevZ = zOffset;
								prevRad = rad;
							}
						} // for end
					} // not shrinking if
					
					else {
						// Going outwards, closing the gate
						long stateChange = gateWaitClose + 29;
						float arg2 = (float) ((world.getTotalWorldTime() - stateChange + partialTicks) / 3f) - 1.0f;
						
						if (arg2 < Aunis.getRendererInit().eventHorizonRadius+0.1f) {
							backStrip = Aunis.getRendererInit().new QuadStrip(0, arg2, Aunis.getRendererInit().eventHorizonRadius, tick);
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
								backStrip = Aunis.getRendererInit().new QuadStrip(0, arg2, Aunis.getRendererInit().eventHorizonRadius, tick);
							
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
	
	private void renderEventHorizon(double x, double y, double z, double partialTicks, boolean white, Float alpha, boolean backOnly) {			
		float tick = (float) (world.getTotalWorldTime() + partialTicks);	
		
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
				Aunis.getRendererInit().innerCircle.render(tick, true, alpha);
			
			Aunis.getRendererInit().innerCircle.render(tick, false, 1.0f-alpha);
			
			for ( QuadStrip strip : Aunis.getRendererInit().quadStrips ) {
				if (white)
					strip.render(tick, true, alpha);
				
				strip.render(tick, false, 1.0f-alpha);
			}
		}
		
		GlStateManager.disableBlend();
	}

	private boolean doInsertAnimation = false;
	private boolean doRemovalAnimation = false;
	private boolean doUpgradeRender = false;
	private long insertionTime;
	
	@Override
	public void upgradeInteract(boolean hasUpgrade, boolean isHoldingUpgrade) {
		if (hasUpgrade) {
			if (doUpgradeRender) {
				// Removing upgrade from slot				
				doUpgradeRender = false;
				AunisPacketHandler.INSTANCE.sendToServer( new UpgradeTileUpdateToServer(pos, false) );
			}
			
			else {
				// Sliding out upgrade
				if (!doRemovalAnimation) {
					insertionTime = world.getTotalWorldTime();
					doRemovalAnimation = true;
					doUpgradeRender = true;
				}
			}
		}
		
		else {
			if (doUpgradeRender) {
				// Inserting upgrade into DHD
				if (!doInsertAnimation) {
					insertionTime = world.getTotalWorldTime();
					doInsertAnimation = true;
				}
			}
			
			else {
				// Putting upgrade in slot
				if (isHoldingUpgrade) {
					doUpgradeRender = true;
				}
			}
		}
	}
	
	/*public boolean upgradeInSlot() {
		return doUpgradeRender;
	}*/
	
	public void renderUpgrade(double x, double y, double z, double partialTicks) {		
		float arg = (float) ((world.getTotalWorldTime() - insertionTime + partialTicks) / 60.0);
		float mul = 1;
		
		if (doInsertAnimation)
			mul = MathHelper.cos(arg+0.31f)+0.048f;
		else if (doRemovalAnimation)
			mul = MathHelper.sin(arg) + 0.53f;
		
		GlStateManager.pushMatrix();
		
		// Gate diameter/2 + 0.9
		GlStateManager.translate(x, y-4.55f+1*mul, z);
		GlStateManager.rotate(horizontalRotation, 0, 1, 0);
		
		GlStateManager.translate(0.077f, 0, 0.07f);
		GlStateManager.rotate(135, 0, 0, 1);
		
			
		ItemStack stack = new ItemStack(AunisItems.stargateAddressCrystal);
			
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, world, null);
		model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);
	
		GlStateManager.enableBlend();
		
		GlStateManager.color(1, 1, 1, 0.7f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		
		GlStateManager.disableBlend();
		
		if (doInsertAnimation && mul < 0.7f) {
			doUpgradeRender = false;
			doInsertAnimation = false;
			
			// Upgrade inserted, send to server
			AunisPacketHandler.INSTANCE.sendToServer( new UpgradeTileUpdateToServer(pos, true) );
		}
		
		else if (doRemovalAnimation && mul > 1) {
			doRemovalAnimation = false;
		}
		
		GlStateManager.popMatrix();
	}
}
