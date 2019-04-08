package mrjake.aunis.item.color;

import mrjake.aunis.Aunis;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class CrystalControlDHDItemColor implements IItemColor {

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		Aunis.info("colorMultiplier("+stack+", "+tintIndex+")");
		return 0x0000FF00;
	}

}
