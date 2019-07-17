package mrjake.aunis.upgrade;

import mrjake.aunis.item.AunisItems;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

public class StargateUpgradeRenderer extends UpgradeRenderer{

	public StargateUpgradeRenderer(TileEntity te) {
		super(te, ((ITileEntityRendered) te).getRenderer().getHorizontalRotation());
	}

	@Override
	public void translateUpgradeModel(double x, double y, double z, float arg) {
		mul = 1;
		
		if (doInsertAnimation)
			mul = MathHelper.cos(arg+0.31f)+0.048f;
		else if (doRemovalAnimation)
			mul = MathHelper.sin(arg) + 0.53f;
		
		// Gate diameter/2 + 0.9
		GlStateManager.translate(x, y-4.55f+1*mul, z);
		GlStateManager.rotate(this.horizontalRotation, 0, 1, 0);
		
		GlStateManager.translate(0.077f, 0, 0.07f);
		GlStateManager.rotate(135, 0, 0, 1);
	}

	@Override
	protected boolean insertDoneCondition() {
		return mul < 0.7f;
	}
	
	@Override
	protected boolean removalDoneCondition() {
		return mul > 1;
	}
	
	@Override
	public Item getUpgradeItem() {
		return AunisItems.crystalGlyphStargate;
	}
}
