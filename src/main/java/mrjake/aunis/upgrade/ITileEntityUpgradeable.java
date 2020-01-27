package mrjake.aunis.upgrade;

import li.cil.oc.api.event.RackMountableRenderEvent.TileEntity;
import mrjake.aunis.state.UpgradeRendererState;
import net.minecraft.item.Item;

/**
 * Implemented by TileEntities that use TESR
 * 
 * Manages getting the Renderer(client side only),
 * getting the RendererState(server side, for Renderer update on block load)
 *
 */
public interface ITileEntityUpgradeable {	
	
	/**
	 * Should return instance of specified renderer(StargateRenderer for StartgateBaseTile for ex.)
	 * or create it if it doesn't exists already. Doesn't set any of it's parameters.
	 * 
	 * Used by client-side TESR.
	 * 
	 * @return Renderer instance.
	 */
	public abstract UpgradeRenderer getUpgradeRenderer();
	
	/**
	 * Should return an RendererState instance with all parameters that matter to client-side renderer.
	 * 
	 * @return Specific RendererState instance.
	 */
	public abstract UpgradeRendererState getUpgradeRendererState();
	
	/**
	 * Should return true, if upgrade was applied, false otherwise.
	 * 
	 * @return Upgrade state.
	 */
	public abstract boolean hasUpgrade();
	
	/**
	 * Sets upgrade state
	 * 
	 * @param hasUpgrade
	 */
	public abstract void setUpgrade(boolean hasUpgrade);
	
	/**
	 * Should return which item will be accepted as an upgrade
	 * 
	 * @return Item
	 */
	public abstract Item getAcceptedUpgradeItem();
	
	/**
	 * Runs {@link TileEntity#markDirty()}
	 */
	public abstract void markDirty();
}
