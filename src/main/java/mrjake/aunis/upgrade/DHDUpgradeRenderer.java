package mrjake.aunis.upgrade;

import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.ItemBase;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

public class DHDUpgradeRenderer extends UpgradeRenderer {

	public DHDUpgradeRenderer(TileEntity te) {
		super(te, ((ITileEntityRendered) te).getRenderer().getHorizontalRotation());
	}

	@Override
	protected void translateUpgradeModel(double x, double y, double z, float arg) {
		mul = 1;
		
		if (doInsertAnimation)
			mul = MathHelper.cos(arg+0.31f)+0.048f;
		else if (doRemovalAnimation)
			mul = MathHelper.sin(arg) + 0.53f;
		
		GlStateManager.translate(x+0.5, y, z+0.5);
		GlStateManager.rotate(horizontalRotation, 0, 1, 0);
		
		GlStateManager.translate(0, 0.5, 0.5*mul);
		GlStateManager.rotate(-90, 0, 1, 0);	
		GlStateManager.rotate(45, 0, 0, 1);
	}

	@Override
	protected boolean insertDoneCondition() {
		return mul < 0.53f;
	}

	@Override
	protected boolean removalDoneCondition() {
		return mul > 1;
	}
	
	@Override
	protected ItemBase getUpgradeItem() {
		return AunisItems.crystalGlyphDhd;
	}
}
