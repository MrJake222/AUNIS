package mrjake.aunis.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Basically a simplified {@link ItemStack} with {@code equals} and {@code hashCode}.
 * 
 * @author MrJake222
 *
 */
public class ItemMetaPair {
	
	private final Item item;
	private final int meta;

	public ItemMetaPair(Item item, int meta) {
		this.item = item;
		this.meta = meta;
	}
	
	public ItemMetaPair(ItemStack stack) {
		this(stack.getItem(), stack.getMetadata());
	}


	public String getDisplayName() {
		return item.getItemStackDisplayName(new ItemStack(item, 1, meta));
	}
	
	public boolean equalsItemStack(ItemStack stack) {
		if (item != stack.getItem())
			return false;
		
		if (meta != stack.getMetadata())
			return false;
		
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + meta;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ItemMetaPair))
			return false;
		ItemMetaPair other = (ItemMetaPair) obj;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (item != other.item) // Items are singletons
			return false;
		if (meta != other.meta)
			return false;
		return true;
	}
}
