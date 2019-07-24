package mrjake.aunis.renderer.stargate.orlin;

import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.renderer.stargate.StargateRendererBase;
import mrjake.aunis.renderer.state.stargate.StargateRendererStateBase;
import mrjake.aunis.tileentity.stargate.StargateBaseTileOrlin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;

public class StargateRendererOrlin extends StargateRendererBase {
	
	public static final float GATE_SCALE = 0.43f;
	
	private StargateRendererStateBase state = new StargateRendererStateBase();
	
	public StargateRendererOrlin(StargateBaseTileOrlin te) {
		super(te.getWorld(), te.getPos());
	}

	@Override
	protected boolean shouldRender() {
		IBlockState state = world.getBlockState(pos);
		
		return (!state.getValue(AunisProps.RENDER_BLOCK));
	}
	
	@Override
	protected StargateRendererStateBase getRendererState() {
		return state;
	}

	@Override
	protected void applyLightMap(double partialTicks) {
//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
	}

	@Override
	protected void renderGate() {
		Model orlinModel = ModelLoader.getModel(EnumModel.ORLIN_GATE);
				
		if (orlinModel != null) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(GATE_SCALE, GATE_SCALE, GATE_SCALE);
			
			EnumModel.ORLIN_GATE.bindTexture();
			orlinModel.render();
			
			GlStateManager.popMatrix();
		}
	}

	@Override
	protected void renderRing(double partialTicks) {}

	@Override
	protected void renderChevrons(double partialTicks) {}

	@Override
	public void clearChevrons(Long stateChange) {}
	
	@Override
	protected void renderKawoosh(double partialTicks) {		
		GlStateManager.scale(GATE_SCALE, GATE_SCALE, GATE_SCALE);
		
		GlStateManager.translate(0, 3.80873f, -0.204347f);
		GlStateManager.scale(0.7f, 0.7f, 0.7f);
		
		super.renderKawoosh(partialTicks);
	}

	@Override
	public void setRendererState(StargateRendererStateBase state) {
		// TODO Auto-generated method stub
	}
}
