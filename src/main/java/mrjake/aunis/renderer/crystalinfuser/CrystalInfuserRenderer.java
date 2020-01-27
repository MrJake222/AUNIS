package mrjake.aunis.renderer.crystalinfuser;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.capability.EnergyStorageSerializable;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.renderer.ItemRenderer;
import mrjake.aunis.renderer.SpinHelper;
import mrjake.aunis.state.CrystalInfuserRendererState;
import mrjake.aunis.state.SpinState;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

public class CrystalInfuserRenderer {

	private World world;
	private BlockPos pos;
	
	private ItemRenderer itemRenderer;
	private ItemStack renderedItemStack;

	private long creationTime;
	
	public CrystalInfuserRenderer(CrystalInfuserTile te) {
		this.world = te.getWorld();
		this.pos = te.getPos();
		creationTime = world.getTotalWorldTime();
		
		renderedItemStack = new ItemStack(AunisItems.crystalControlDhd);
		itemRenderer = new ItemRenderer(renderedItemStack);
	}
	
	public void render(double x, double y, double z, double partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		long tick = world.getTotalWorldTime() - creationTime;
		
		GlStateManager.color(0.4f, 0.4f, 0.4f, 1.0f);
				
		renderBaseModel();
		
		if (state.doCrystalRender) {
			renderCrystal(tick, partialTicks);
			
			if (state.renderWaves || !waves.isEmpty())
				renderWaves(tick, partialTicks);
		}
		
		GlStateManager.popMatrix();
	}
	
	private void renderBaseModel() {		
		Model infuserPylon = ModelLoader.getModel( EnumModel.CrystalInfuserPylon );
		Model infuserBase = ModelLoader.getModel( EnumModel.CrystalInfuserBase );
				
		if(infuserPylon != null && infuserBase != null) {
			EnumModel.CrystalInfuserPylon.bindTexture();
			
			GlStateManager.pushMatrix();

			GlStateManager.scale(1.3, 1, 1.3);
			infuserBase.render();
			
			GlStateManager.popMatrix();
		}
	}
	
	private double crystalRotation;
	
	private SpinHelper spinHelper;
	private SpinState spinState;
	
	private SpinHelper getSpinHelper() {
		if (spinHelper == null) {			
			spinHelper = new SpinHelper(world, getSpinState(), 10);
		}
		
		return spinHelper;
	}
	
	private SpinState getSpinState() {
		if (spinState == null)
			spinState = new SpinState();
			
		return spinState;
	}
		
	private void renderCrystal(long tick, double partialTicks) {
		double tickPartial = tick + partialTicks;
		double y = 0.9 + (Math.sin(tickPartial/16.0f) / 24.0f);		
				
		if (getSpinState().isSpinning) {
			crystalRotation = getSpinHelper().spin(partialTicks) % 360;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) crystalRotation, 0, 1, 0);
		GlStateManager.scale(0.45, 0.45, 0.45);
		GlStateManager.translate(0, y, 0);
		
		GlStateManager.rotate(45, 0, 0, 1);
		
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		itemRenderer.render();
		
		GlStateManager.disableBlend();
		
		GlStateManager.popMatrix();
	}
	
	// ------------------------------------------------------------------------------------------
	private List<CrystalInfuserWave> waves = new ArrayList<CrystalInfuserWave>();
	private long lastCreatedTick = 0;
	
	/**
	 * Renders waves
	 * 
	 * Each x seconds a wave is added to the list
	 * Then, it ascends and looses opacity
	 * When all opacity is lost, it is removed from the list
	 * @param partialTicks 
	 */
	private void renderWaves(long tick, double partialTicks) {
		double tickPartial = tick + partialTicks;
				
		if (tick % 4 == 0 && tick != lastCreatedTick && state.renderWaves) {
			waves.add(new CrystalInfuserWave(tickPartial));
			
			lastCreatedTick = tick;
		}
		
		CrystalInfuserWave toBeRemoved = null;
		
		for (CrystalInfuserWave wave : waves) {
			if (wave.render(tickPartial)) {
				toBeRemoved = wave;
			}
		}
		
		if (toBeRemoved != null) {
			waves.remove(toBeRemoved);
		}
	}
	
	
	// ------------------------------------------------------------------------------------------
	CrystalInfuserRendererState state = new CrystalInfuserRendererState();
	
	public void setRendererState(CrystalInfuserRendererState state) {
		
		// Starting spinning
		if (!this.state.doCrystalRender && state.doCrystalRender) {
			crystalRotation = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL).getHorizontalAngle();
			
			waves.clear();
			getSpinHelper().requestStart(crystalRotation);
		}
		
		// Stopping spinning		
		if (this.state.doCrystalRender && !state.doCrystalRender) {
			getSpinHelper().requestStop();
		}
		
		this.state = state;

		EnergyStorageSerializable energyStorage = (EnergyStorageSerializable) this.renderedItemStack.getCapability(CapabilityEnergy.ENERGY, null);
		energyStorage.setEnergyStored(state.energyStored);
	}
}
