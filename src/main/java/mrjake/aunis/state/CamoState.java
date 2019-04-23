package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Holds {@link IBlockState} of camouflage block to be displayed instead.
 * 
 * @author MrJake
 */
public class CamoState extends State {
	public CamoState() {}
	
	private int id;
	private int meta;
		
	public CamoState(ItemStack itemStack) {
		this.id = Item.getIdFromItem(itemStack.getItem());
		this.meta = itemStack.getMetadata();
	}

	public ItemStack getItemStack() {
		return new ItemStack(Item.getItemById(id), 1, meta);
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(meta);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		meta = buf.readInt();
	}
}
