package mrjake.aunis.renderer.stargate;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.stargate.StargateMilkyWayMergeHelper;
import mrjake.aunis.util.FacingToRotation;
import mrjake.aunis.util.math.MathFunction;
import mrjake.aunis.util.math.MathFunctionImpl;
import mrjake.aunis.util.math.MathRange;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;

public class StargateMilkyWayRenderer extends StargateAbstractRenderer {
	
	private static final Vec3d RING_LOC = new Vec3d(0.0, -0.122333, -0.000597);
	private static final float GATE_DIAMETER = 10.1815f;

	public StargateMilkyWayRenderer() {		
		
	}
	
	@Override
	protected Map<BlockPos, IBlockState> getMemberBlockStates(EnumFacing facing) {
		Map<BlockPos, IBlockState> map = new HashMap<BlockPos, IBlockState>();
		
		for (BlockPos pos : StargateMilkyWayMergeHelper.INSTANCE.getRingBlocks())
			map.put(pos, AunisBlocks.stargateMilkyWayMemberBlock.getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING).withProperty(AunisProps.FACING_HORIZONTAL, facing));
		
		for (BlockPos pos : StargateMilkyWayMergeHelper.INSTANCE.getChevronBlocks())
			map.put(pos, AunisBlocks.stargateMilkyWayMemberBlock.getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.CHEVRON).withProperty(AunisProps.FACING_HORIZONTAL, facing));
		
		return map;
	}
	
	@Override
	protected void applyLightMap(StargateAbstractRendererState rendererState, double partialTicks) {
		final int chevronCount = 6;
		int skyLight = 0;
		int blockLight = 0;
		
		for (int i=0; i<chevronCount; i++) {
			BlockPos blockPos = StargateMilkyWayMergeHelper.INSTANCE.getChevronBlocks().get(i).rotate(FacingToRotation.get(rendererState.facing)).add(rendererState.pos);
			
			skyLight += getWorld().getLightFor(EnumSkyBlock.SKY, blockPos);
			blockLight += getWorld().getLightFor(EnumSkyBlock.BLOCK, blockPos);
		}
		
		skyLight /= chevronCount;
		blockLight /= chevronCount;
		
//		int clamped = MathHelper.clamp(skyLight+blockLight, 0, 15);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight * 16, skyLight * 16);
//		Aunis.info("bl: " + blockLight + " sky: " + skyLight);
	}
		
	@Override
	protected double getRenderScale(StargateAbstractRendererState rendererState) {
		return ((StargateMilkyWayRendererState) rendererState).stargateSize.renderScale;
	}
	
	@Override
	protected Vec3d getRenderTranslation(StargateAbstractRendererState rendererState) {
		return new Vec3d(0.50, GATE_DIAMETER/2 + ((StargateMilkyWayRendererState) rendererState).stargateSize.renderTranslationY, 0.50);
	}
	
	@Override
	protected void renderGate() {		
		Model gateModel = ModelLoader.getModel( EnumModel.GATE_MODEL );
		
		if ( gateModel != null ) {			
			EnumModel.GATE_MODEL.bindTexture();
			
			gateModel.render();
		}
	}
		
	@Override
	protected void renderRing(StargateAbstractRendererState rendererState, double partialTicks) {
		StargateMilkyWayRendererState mwRendererState = (StargateMilkyWayRendererState) rendererState;
		
//		ModelLoader.loadModel(EnumModel.RING_MODEL);
		
		Model ringModel = ModelLoader.getModel(EnumModel.RING_MODEL);
				
		if (ringModel != null) {
			
			GlStateManager.pushMatrix();
			float angularRotation = mwRendererState.spinHelper.currentSymbol.angle;
			
			if (mwRendererState.spinHelper.isSpinning)
				angularRotation += ((StargateMilkyWayRendererState) rendererState).spinHelper.apply(getWorld().getTotalWorldTime() + partialTicks);
			
			if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 0)
				angularRotation *= -1;

			
			if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 270) {
				GlStateManager.translate(RING_LOC.y, RING_LOC.z, RING_LOC.x);
				GlStateManager.rotate(angularRotation, 1, 0, 0);
				GlStateManager.translate(-RING_LOC.y, -RING_LOC.z, -RING_LOC.x);
			}
			
			else {
				GlStateManager.translate(RING_LOC.x, RING_LOC.z, RING_LOC.y);
				GlStateManager.rotate(angularRotation, 0, 0, 1);
				GlStateManager.translate(-RING_LOC.x, -RING_LOC.z, -RING_LOC.y);
			}
			
			GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
			
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
	
	private static MathRange chevronOpenRange = new MathRange(0, 1.57f);
	private static MathFunction chevronOpenFunction = new MathFunctionImpl(x -> x*x*x*x/80f);
	
	private static MathRange chevronCloseRange = new MathRange(0, 1.428f);
	private static MathFunction chevronCloseFunction = new MathFunctionImpl(x0 -> MathHelper.cos(x0*1.1f) / 12f);
	
//	private static Map<MathRange, MathFunction> topChevronMovementPhases = new HashMap<>(3);
//	static {
//		topChevronMovementPhases.put(new MathRange(0, 1.57f), new MathFunctionImpl(x -> x*x*x*x/80f));
//		topChevronMovementPhases.put(new MathRange(1.57f, 3.14f), new MathFunctionImpl(x -> 0.08333f)); // 1 / 12
//		topChevronMovementPhases.put(new MathRange(3.14f, 4.71f), new MathFunctionImpl(x -> -MathHelper.cos(x) / 12f));
//	}
	
	private float calculateTopChevronOffset(StargateMilkyWayRendererState rendererState, double partialTicks) {
		float tick = (float) (getWorld().getTotalWorldTime() - rendererState.chevronActionStart + partialTicks);
		float x = tick / 6.0f;
		
		if (rendererState.chevronOpening) {
			if (chevronOpenRange.test(x))
				return chevronOpenFunction.apply(x);
			else {
				rendererState.chevronOpen = true;
				rendererState.chevronOpening = false;
			}
		}
		
		else if (rendererState.chevronClosing) {
			if (chevronCloseRange.test(x))
				return chevronCloseFunction.apply(x);
			else {
				rendererState.chevronOpen = false;
				rendererState.chevronClosing = false;
			}
		}
		
		return rendererState.chevronOpen ? 0.08333f : 0;
	}
	
	private void renderChevron(StargateAbstractRendererState rendererState, int index, double partialTicks) {
		StargateMilkyWayRendererState mwRendererState = (StargateMilkyWayRendererState) rendererState;
//		ModelLoader.loadModel(EnumModel.GATE_MODEL);
		
		Model ChevronLight = ModelLoader.getModel( EnumModel.ChevronLight );
		Model ChevronFrame = ModelLoader.getModel( EnumModel.ChevronFrame );
		Model ChevronMoving = ModelLoader.getModel( EnumModel.ChevronMoving );
		Model ChevronBack = ModelLoader.getModel( EnumModel.ChevronBack );
		
		if ( ChevronLight != null && ChevronFrame != null && ChevronMoving != null && ChevronBack != null ) {
			GlStateManager.pushMatrix();
			
			int angularPosition = EnumChevron.getRotation(index);
			GlStateManager.rotate(angularPosition, 0, 0, 1);
			
			ModelLoader.bindTexture(mwRendererState.chevronTextureList.get(index));
						
			if (index == 8) {
				float chevronOffset = calculateTopChevronOffset(mwRendererState, partialTicks);
				
				GlStateManager.pushMatrix();
				
				GlStateManager.translate(0, chevronOffset, 0);
				ChevronLight.render();
				
				GlStateManager.translate(0, -2*chevronOffset, 0);
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
	protected void renderChevrons(StargateAbstractRendererState rendererState, double partialTicks) {
		for (int i=0; i<9; i++)
			renderChevron(rendererState, i, partialTicks);
		
		((StargateMilkyWayRendererState) rendererState).chevronTextureList.iterate(getWorld(), partialTicks);
	}
}
