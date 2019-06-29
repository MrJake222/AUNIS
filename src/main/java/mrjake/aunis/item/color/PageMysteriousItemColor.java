package mrjake.aunis.item.color;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class PageMysteriousItemColor implements IItemColor {
		
	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {		
		return tintIndex == 1 ? 0x303000 : 0xFFFFFF;
	}

}
