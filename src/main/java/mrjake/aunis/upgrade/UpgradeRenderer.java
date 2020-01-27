package mrjake.aunis.upgrade;

import mrjake.aunis.renderer.ItemRenderer;
import mrjake.aunis.state.UpgradeRendererState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public abstract class UpgradeRenderer {
	private World world;
	
	protected float horizontalRotation;
	
	protected boolean doInsertAnimation = false;
	protected boolean doRemovalAnimation = false;
	private boolean doUpgradeRender = false;
	private long insertionTime;
	
	protected float mul;
	
	private ItemRenderer itemRenderer;
	
	/**
	 * Creates UpgradeRenderer instance.
	 * 
	 * @param te - TileEntity(required for world and position).
	 */
	public UpgradeRenderer(World world, float horizontalRotation) {
		this.world = world;
		
		this.horizontalRotation = horizontalRotation;
		
		this.itemRenderer = new ItemRenderer(getUpgradeItem());
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
	protected abstract Item getUpgradeItem();
	
	/**
	 * Applies given state to this Renderer 
	 */
	public void setState(UpgradeRendererState rendererState) {
		this.doInsertAnimation = rendererState.doInsertAnimation;
		this.doRemovalAnimation = rendererState.doRemovalAnimation;
		this.doUpgradeRender = rendererState.doUpgradeRender;
		this.insertionTime = rendererState.insertionTime;
	}
	
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
		
		GlStateManager.enableBlend();
		
		GlStateManager.color(1, 1, 1, 0.7f);
		
		GlStateManager.scale(0.7, 0.7, 0.7);
		GlStateManager.translate(-0.15, 0, 0);
		
		itemRenderer.render();
		
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
