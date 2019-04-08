package mrjake.aunis.block;

import net.minecraft.item.Item;

public interface IBlockBase {
	public abstract Item getItemBlock();
	
	public abstract void registerItemRenderer();
}