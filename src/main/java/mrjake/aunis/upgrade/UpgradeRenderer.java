package mrjake.aunis.upgrade;

import mrjake.aunis.item.ItemBase;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.UpgradeRendererState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public abstract class UpgradeRenderer implements ISpecialRenderer<UpgradeRendererState> {
	private World world;
	
	protected float horizontalRotation;
	
	protected boolean doInsertAnimation = false;
	protected boolean doRemovalAnimation = false;
	private boolean doUpgradeRender = false;
	private long insertionTime;
	
	protected float mul;
	
	/**
	 * Creates UpgradeRenderer instance.
	 * 
	 * @param te - TileEntity(required for world and position).
	 */
	public UpgradeRenderer(TileEntity te, float horizontalRotation) {
		this.world = te.getWorld();
		
		this.horizontalRotation = horizontalRotation;
	}
	
	/**
	 * Should do the necessary translations to the upgrade model
	 * 
	 */
	protected abstract void translateUpgradeModel(double x, double y, double z, float arg);
	
	/**
	 * Checks if the insert should be finished
	 * 
	 * @return True - finish animation
	 */
	protected abstract boolean insertDoneCondition();

	/**
	 * Checks if the removal should be finished
	 * 
	 * @return True - finish animation
	 */
	protected abstract boolean removalDoneCondition();

	/**
	 * Should return ItemBase for the upgrade item(ex. Aunis.crystalGlyphDhd).
	 * Used for rendering proper model
	 * 
	 * @return upgrade's ItemBase.
	 */
	protected abstract ItemBase getUpgradeItem();
	
	/**
	 * Applies given state to this Renderer 
	 */
	@Override
	public void setState(UpgradeRendererState rendererState) {
		this.doInsertAnimation = rendererState.doInsertAnimation;
		this.doRemovalAnimation = rendererState.doRemovalAnimation;
		this.doUpgradeRender = rendererState.doUpgradeRender;
		this.insertionTime = rendererState.insertionTime;
	}
	
	@Override
	public float getHorizontalRotation() {
		return horizontalRotation;
	}
	
	public boolean isUpgradeRenderRunning() {
		return doUpgradeRender;
	}
	
	/**
	 * Called when upgrade is putted into a slot
	 *   - No upgrade in tile
	 *   - Upgrade hold in hand
	 * 
	 */
	public void putUpgradeInSlot() {
		doInsertAnimation = false;
		doRemovalAnimation = false;
		
		doUpgradeRender = true;
	}
	
	/**
	 * Called when upgrade is slided into the slot
	 *   - Upgrade in slot
	 *   - Hand = don't care
	 */
	public void insertUpgrade() {
		insertionTime = world.getTotalWorldTime();

		doInsertAnimation = true;
	}
	
	/**
	 * Called upon upgrade removal
	 *   - Tile has upgrade
	 *   - Tile clicked (hand = don't care)
	 */
	public void removeUpgrade() {
		doInsertAnimation = false;
		doRemovalAnimation = true;
		
		insertionTime = world.getTotalWorldTime();
		
		doUpgradeRender = true;
	}
	
	/** 
	 * Called on upgrade drop to the ground
	 *   - Upgrade in slot
	 *   - Clicked (hand = don't care)
	 */
	public void popUpgrade() {
		doUpgradeRender = false;
	}
	
	/**
	 * Renders upgrade using abstract functions
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param partialTicks
	 */
	public void render(double x, double y, double z, double partialTicks) {		
		if (!doUpgradeRender)
			return;
		
		float arg = (float) ((world.getTotalWorldTime() - insertionTime + partialTicks) / 60.0);
		
		GlStateManager.pushMatrix();
		
		translateUpgradeModel(x, y, z, arg);		
			
		ItemStack stack = new ItemStack(getUpgradeItem());
			
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, world, null);
		model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);
	
		GlStateManager.enableBlend();
		
		GlStateManager.color(1, 1, 1, 0.7f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		
		GlStateManager.disableBlend();
		
		if (doInsertAnimation && insertDoneCondition()) {
			doUpgradeRender = false;
			doInsertAnimation = false;
			
			// Upgrade inserted, send to server
//			AunisPacketHandler.INSTANCE.sendToServer( new UpgradeTileUpdateToServer(pos, true) );
		}
		
		else if (doRemovalAnimation && removalDoneCondition()) {
			doRemovalAnimation = false;
		}
		
		GlStateManager.popMatrix();
	}
}
