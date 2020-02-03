package mrjake.aunis.renderer.stargate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.StargateActivation;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisPositionedSound;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.MergeHelper;
import mrjake.aunis.state.StargateRendererStateBase;
import mrjake.aunis.state.StargateRendererStateSG1;
import mrjake.aunis.state.StargateSpinStateRequest;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;

public class StargateRendererSG1 extends StargateRendererBase {
	
	private static final Vec3d RING_LOC = new Vec3d(0.0, -0.122333, -0.000597);
		
	public StargateRendererSG1(StargateMilkyWayBaseTile te) {
		super(te.getWorld(), te.getPos());
		
		for (int i=0; i<9; i++)
			chevronTextureList.add(CHEVRON_TEXTURE_BASE + "0.png");
		
		this.rendererState = new StargateRendererStateSG1();
		// Load chevron textures
//		for (int i=0; i<=10; i++)
//			ModelLoader.getTexture("stargate/chevron/chevron" + i + ".png");
	}
	
	private StargateRendererStateSG1 getRendererStateSG1() {
		return (StargateRendererStateSG1) rendererState;
	}
	
	public void setCurrentSymbol(EnumSymbol symbol) {
		getRendererStateSG1().ringCurrentSymbol = symbol;
	}
	
	@Override
	public void setRendererState(StargateRendererStateBase state) {		
		super.setRendererState(state);
		
		ringAngularRotation = getRendererStateSG1().ringCurrentSymbol.angle;
		
		setActiveChevrons(state.getActiveChevrons(), state.isFinalActive());
		
		ringSpinHelper = new StargateRingSpinHelper(world, pos, this, getRendererStateSG1().spinState);
		
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.WORMHOLE, pos, state.doEventHorizonRender);
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.RING_ROLL_START, pos, false);
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.RING_ROLL_LOOP, pos, getRendererStateSG1().spinState.isSpinning);
	}
	
	public static List<BlockPos> chevronBlocks = Arrays.asList(
			new BlockPos(-4, 2, 0), 
			new BlockPos(-5, 5, 0), 
			new BlockPos(5, 5, 0), 
			new BlockPos(4, 2, 0) ); 
	
	@Override
	protected void applyLightMap(double partialTicks) {
		int skyLight = 0;
		int blockLight = 0;
		
		for (int i=0; i<chevronBlocks.size(); i++) {
			BlockPos blockPos = MergeHelper.rotateAndGlobal(chevronBlocks.get(i), world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL), pos); //((int) world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL).getHorizontalAngle(), pos);
			
			skyLight += world.getLightFor(EnumSkyBlock.SKY, blockPos);// - subt;
			blockLight += world.getLightFor(EnumSkyBlock.BLOCK, blockPos);
		}
		
		skyLight /= 4;
		blockLight /= 4;
		
		int clamped = MathHelper.clamp(skyLight+blockLight, 0, 15);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight * 16, clamped * 16);
	}
	
	@Override
	protected boolean shouldRender() {
		IBlockState state = world.getBlockState(pos);
		
		if (state.getBlock() != AunisBlocks.stargateMilkyWayBaseBlock)
			return false;
		
		return (!state.getValue(AunisProps.RENDER_BLOCK));
	}
	
	@Override
	protected void renderGate() {		
		Model gateModel = ModelLoader.getModel( EnumModel.GATE_MODEL );
		
		if ( gateModel != null ) {			
			EnumModel.GATE_MODEL.bindTexture();
			
			gateModel.render();
		}
	}

	private StargateRingSpinHelper ringSpinHelper;
	
	private StargateRingSpinHelper getRingSpinHelper() {
		if (ringSpinHelper == null)
			ringSpinHelper = new StargateRingSpinHelper(world, pos, this, getRendererStateSG1().spinState);
		
		return ringSpinHelper;
	}
		
	public void setRingSpin(boolean spin, boolean dialingComplete, StargateSpinStateRequest stateRequest) {
		rendererState.dialingComplete = dialingComplete;
		
		if (spin) {
			AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.RING_ROLL_START, pos, true);
			
			if (getRendererStateSG1().spinState.isSpinning)
				ringAngularRotation = getRingSpinHelper().spin(0);
			
			getRendererStateSG1().spinState.direction = stateRequest.direction;
			getRendererStateSG1().spinState.targetSymbol = stateRequest.targetSymbol;
			getRendererStateSG1().spinState.finalChevron = stateRequest.lock;
			
			getRingSpinHelper().requestStart(ringAngularRotation, getRendererStateSG1().spinState.direction, stateRequest.targetSymbol, stateRequest.lock, false);		
		}
		
		else {
			stopRingSounds();
						
			getRingSpinHelper().requestStop();
		}
	}
	
	public void setRingSpin(boolean spin, boolean dialingComplete) {
		setRingSpin(spin, dialingComplete, new StargateSpinStateRequest());
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
		getRendererStateSG1().spinState.finalChevron = finalChevron;
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
	
	@Override
	protected void renderRing(double partialTicks) {
//		ModelLoader.loadModel(EnumModel.RING_MODEL);
		
		Model ringModel = ModelLoader.getModel(EnumModel.RING_MODEL);
				
		if (ringModel != null) {
			
			if (getRendererStateSG1().spinState.isSpinning) {				
				ringAngularRotation = (float) (getRingSpinHelper().spin(partialTicks) % 360);
			}
			
			if (waitForFinalMove && (world.getTotalWorldTime() - waitForFinalMoveStart) >= (getRendererStateSG1().spinState.finalChevron ? 20 : 15)) {
				waitForFinalMove = false;
								
				long start = waitForFinalMoveStart + (getRendererStateSG1().spinState.finalChevron ? 20 : 15);
				moveFinalChevron(start, true);
			}
			
			GlStateManager.pushMatrix();
			
//			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			float angularRotation = (float) ringAngularRotation;
			
			if (horizontalRotation == 90 || horizontalRotation == 0)
				angularRotation *= -1;

			
			if (horizontalRotation == 90 || horizontalRotation == 270) {
				GlStateManager.translate(RING_LOC.y, RING_LOC.z, RING_LOC.x);
				GlStateManager.rotate(angularRotation, 1, 0, 0);
				GlStateManager.translate(-RING_LOC.y, -RING_LOC.z, -RING_LOC.x);
			}
			
			else {
				GlStateManager.translate(RING_LOC.x, RING_LOC.z, RING_LOC.y);
				GlStateManager.rotate(angularRotation, 0, 0, 1);
				GlStateManager.translate(-RING_LOC.x, -RING_LOC.z, -RING_LOC.y);
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
	
	@Override
	public void clearChevrons(Long stateChange) {
		changeChevrons(true, stateChange, activeChevrons, isLastChevronActive());
	}
	
	public void clearChevrons() {
		clearChevrons(null);
	}
	
	public void lightUpChevrons(int chevronsToLightUp) {
		AunisSoundHelper.playSoundEventClientSide((WorldClient) world, pos, EnumAunisSoundEvent.CHEVRON_INCOMING, 0.5f);
		
		Aunis.info("lightUpChevrons: " + chevronsToLightUp);
		this.rendererState.dialingComplete = true;
		
		changeChevrons(false, null, chevronsToLightUp, true);
	}
	
	private void changeChevrons(boolean clear, Long stateChange, int chevronsToChange, boolean changeFinal) {			
		long activationStateChange;

		Aunis.info("changeChevrons: " + chevronsToChange + ", final: " + changeFinal);
		
		if (stateChange != null)
			activationStateChange = stateChange;
		else
			activationStateChange = world.getTotalWorldTime();
		
		if (clear) {
			if (rendererState.dialingComplete)
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
	
	private void renderChevron(int index, double partialTicks) {
//		ModelLoader.loadModel(EnumModel.GATE_MODEL);
		
		Model ChevronLight = ModelLoader.getModel( EnumModel.ChevronLight );
		Model ChevronFrame = ModelLoader.getModel( EnumModel.ChevronFrame );
		Model ChevronMoving = ModelLoader.getModel( EnumModel.ChevronMoving );
		Model ChevronBack = ModelLoader.getModel( EnumModel.ChevronBack );
		
		if ( ChevronLight != null && ChevronFrame != null && ChevronMoving != null && ChevronBack != null ) {
			GlStateManager.pushMatrix();
			
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
	
	@Override
	protected void renderChevrons( double partialTicks) {
		for (int i=0; i<9; i++)
			renderChevron(i, partialTicks);
		
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
		
		if (lastChevronActive)
			activeChevrons--;
		
		for (int i=0; i<9; i++) {	
			String tex = CHEVRON_TEXTURE_BASE;
			
			if ( i < activeChevrons || (i == 8 && lastChevronActive) )
				tex += "10.png";
			else
				tex += "0.png";
						
			chevronTextureList.add(tex);
		}
	}
}
