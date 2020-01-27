package mrjake.aunis.tesr;

import mrjake.aunis.renderer.SpecialRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/**
 * Implement this on a {@link TileEntity} to use it with {@link SpecialRenderer}, a universal {@link TileEntitySpecialRenderer}.
 * 
 * @author MrJake222
 */
public interface SpecialRendererProviderInterface {

	/**
	 * Main render function.
	 * 
	 * @param x X-coord.
	 * @param y Y-coord.
	 * @param z Z-coord.
	 * @param partialTicks Partial ticks.
	 */
	void render(double x, double y, double z, float partialTicks);

}
