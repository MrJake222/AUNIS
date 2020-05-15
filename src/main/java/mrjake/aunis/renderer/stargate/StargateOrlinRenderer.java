package mrjake.aunis.renderer.stargate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.AunisProps;
import mrjake.aunis.loader.ElementEnum;
import mrjake.aunis.particle.ParticleBlender;
import mrjake.aunis.particle.ParticleBlender.RandomizeInterface;
import mrjake.aunis.particle.ParticleBlender.SimpleVector;
import mrjake.aunis.particle.ParticleBlenderSmoke;
import mrjake.aunis.particle.ParticleBlenderSparks;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateOrlinRenderer extends StargateAbstractRenderer<StargateAbstractRendererState> {
	
	public static final float GATE_SCALE = 0.43f;	
	
	// ---------------------------------------------------------------------------------------
	// Render

	@Override
	protected void renderGate(StargateAbstractRendererState rendererState, double partialTicks) {
		GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
		
		ElementEnum.ORLIN_GATE.bindTextureAndRender();
	}

	@Override
	protected Map<BlockPos, IBlockState> getMemberBlockStates(StargateAbstractMergeHelper mergeHelper, EnumFacing facing) {
		Map<BlockPos, IBlockState> map = new HashMap<BlockPos, IBlockState>();
		
		for (BlockPos pos : mergeHelper.getRingBlocks())
			map.put(pos, mergeHelper.getMemberBlock().getDefaultState().withProperty(AunisProps.ORLIN_VARIANT, facing));
		
		return map;
	}
	
	@Override
	protected void applyLightMap(StargateAbstractRendererState rendererState, double partialTicks) {}
	
	@Override
	protected void applyTransformations(StargateAbstractRendererState rendererState) {
		GlStateManager.translate(0.5, 0, 0.5);			
		GlStateManager.scale(GATE_SCALE, GATE_SCALE, GATE_SCALE);
	}
	
	@Override
	protected void renderKawoosh(StargateAbstractRendererState rendererState, double partialTicks) {				
		GlStateManager.translate(0, 3.80873f, -0.204347f);
		GlStateManager.scale(0.7f, 0.7f, 0.7f);
		
		super.renderKawoosh(rendererState, partialTicks);
	}
	
	
	// ---------------------------------------------------------------------------------------
	// Particles
	
	private static final List<ParticleBlender> GATE_PARTICLES = Arrays.asList(
			new ParticleBlenderSmoke(-2.71127f,  0.188794f, 5.76731f, 1, 10, -0.1f, 0, false, (motion) -> { motion.z = -0.03f + Math.random()*0.06f; }),
			new ParticleBlenderSmoke(2.698340f,  0.210467f, 1.65171f, 1, 10, 0.1f, 0, false, (motion) -> { motion.z = -0.03f + Math.random()*0.06f; }),
			new ParticleBlenderSmoke(-2.81152f,  0.007747f, 4.34894f, 1, 10, true,  (motion) -> { motion.z = 0.03f + Math.random()*0.05f; }),
			new ParticleBlenderSmoke(0.880709f, -0.045567f, 6.63663f, 2, 20, true,  (motion) -> { motion.x = -0.03f + Math.random()*0.06f; motion.z = 0.03f + Math.random()*0.01f; }),
			new ParticleBlenderSmoke(-1.27690f, -0.025613f, 1.15695f, 5, 50, false, (motion) -> { motion.x = -0.03f + Math.random()*0.06f; motion.z = 0.03f + Math.random()*0.01f; }),
			new ParticleBlenderSmoke(1.276900f, -0.025613f, 1.15695f, 5, 50, false, (motion) -> { motion.x = -0.03f + Math.random()*0.06f; motion.z = 0.03f + Math.random()*0.01f; }),
			new ParticleBlenderSmoke(2.279630f,  0.453827f, 5.72200f, 5, 50, 0, -0.01f, true, (motion) -> { motion.x = -0.03f + Math.random()*0.06f; motion.z = -0.03f + Math.random()*-0.01f; }),
			new ParticleBlenderSmoke(-2.36438f,  0.644607f, 5.53441f, 5, 50, 0, -0.01f, true, (motion) -> { motion.x = -0.03f + Math.random()*0.06f; motion.z = -0.03f + Math.random()*-0.01f; }),
			new ParticleBlenderSmoke(-1.26211f,  0.451610f, 1.12577f, 5, 50, 0, -0.01f, true, (motion) -> { motion.x = -0.03f + Math.random()*0.06f; motion.z = -0.03f + Math.random()*-0.01f; })
	);
	
	private static final RandomizeInterface SPARK_RANDOMIZER = new RandomizeInterface() {
		
		@Override
		public void randomize(SimpleVector motion) {
			motion.x = 0.02 - Math.random()/25;
			motion.z = 0.2 + Math.random()/10;
		}
	};
	
	private static final List<ParticleBlender> SPARK_PARTICLES = Arrays.asList(
			new ParticleBlenderSparks(2.3029000f, -0.025317f, 5.64195f, 1, 1, false, SPARK_RANDOMIZER),
			new ParticleBlenderSparks(2.8649800f, -0.020389f, 3.16358f, 1, 1, false, SPARK_RANDOMIZER),
			new ParticleBlenderSparks(1.2703200f, -0.020800f, 1.17375f, 1, 1, false, SPARK_RANDOMIZER),
			new ParticleBlenderSparks(-1.279090f, -0.026023f, 1.15467f, 1, 1, false, SPARK_RANDOMIZER),
			new ParticleBlenderSparks(-2.861490f, -0.022729f, 3.15241f, 1, 1, false, SPARK_RANDOMIZER),
			new ParticleBlenderSparks(-2.327020f, -0.014086f, 5.54528f, 1, 1, false, SPARK_RANDOMIZER),
			new ParticleBlenderSparks(-0.000057f, -0.024829f, 6.74985f, 1, 1, false, SPARK_RANDOMIZER)
	);
	
	public static void spawnParticles(World world, StargateAbstractRendererState rendererState) {
		for (ParticleBlender particle : GATE_PARTICLES) {
			particle.spawn(world, rendererState.pos, rendererState.horizontalRotation, !rendererState.doEventHorizonRender);
		}
		
		// ---------------------------------------------------------------------------------------
		// Sparks
				
		// 6 here increases amount of sparks shot at one time
		long animTime = (world.getTotalWorldTime() - ((StargateOrlinRendererState) rendererState).sparkStart) / 6 + 1;
		
		if (animTime < 4) {
			
			// Easing method, greater the animTime, lower the frequency of the sparks
			if (animTime != 0 && world.getTotalWorldTime() % (animTime) == 0) {
				SPARK_PARTICLES.get(((StargateOrlinRendererState) rendererState).sparkIndex).spawn(world, rendererState.pos, rendererState.horizontalRotation, false);
			}
		}
	}
}
