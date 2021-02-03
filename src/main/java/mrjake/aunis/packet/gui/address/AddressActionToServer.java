package mrjake.aunis.packet.gui.address;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.dialer.UniverseDialerItem;
import mrjake.aunis.item.dialer.UniverseDialerMode;
import mrjake.aunis.item.notebook.NotebookItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AddressActionToServer implements IMessage {
	public AddressActionToServer() {}
	
	private EnumHand hand;
	private AddressDataTypeEnum dataType;
	private AddressActionEnum action;
	private int index;
	private String name;
	
	public AddressActionToServer(EnumHand hand, AddressDataTypeEnum dataType, AddressActionEnum action, int index, String name) {
		this.hand = hand;
		this.dataType = dataType;
		this.action = action;
		this.index = index;
		this.name = name;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hand.ordinal());
		buf.writeInt(dataType.ordinal());
		buf.writeInt(action.ordinal());
		buf.writeInt(index);
		
		buf.writeInt(name.length());
		buf.writeCharSequence(name, StandardCharsets.UTF_8);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		hand = EnumHand.values()[buf.readInt()];
		dataType = AddressDataTypeEnum.values()[buf.readInt()];
		action = AddressActionEnum.values()[buf.readInt()];
		index = buf.readInt();
		
		int size = buf.readInt();
		name = buf.readCharSequence(size, StandardCharsets.UTF_8).toString();
	}
	
	
	public static class AddressActionServerHandler implements IMessageHandler<AddressActionToServer, IMessage> {

		@Override
		public IMessage onMessage(AddressActionToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				NBTTagCompound compound = stack.getTagCompound();
				
				if (message.dataType.page()) {
					NBTTagList list = compound.getTagList("addressList", NBT.TAG_COMPOUND);
					
					switch (message.action) {
						case RENAME:
							NotebookItem.setNameForIndex(stack, message.index, message.name);
							break;
						
						case MOVE_UP:
							NBTBase prev = list.get(message.index-1);
							list.set(message.index-1, list.get(message.index));
							list.set(message.index, prev);
							
							break;
							
						case MOVE_DOWN:
							NBTBase next = list.get(message.index+1);
							list.set(message.index+1, list.get(message.index));
							list.set(message.index, next);
							
							break;
							
						case REMOVE:
							NBTTagCompound selectedCompound = list.getCompoundTagAt(message.index);
							list.removeTag(message.index);
							
							ItemStack pageStack = new ItemStack(AunisItems.PAGE_NOTEBOOK_ITEM, 1, 1);
							stack.setTagCompound(selectedCompound);
							player.addItemStackToInventory(pageStack);
							
							break;
					}
				}
				
				else if (message.dataType.universe()) {
					NBTTagList list = compound.getTagList(UniverseDialerMode.MEMORY.tagListName, NBT.TAG_COMPOUND);
					
					switch (message.action) {
						case RENAME:
							UniverseDialerItem.setMemoryNameForIndex(stack, message.index, message.name);
							break;
						
						case MOVE_UP:
							NBTBase prev = list.get(message.index-1);
							list.set(message.index-1, list.get(message.index));
							list.set(message.index, prev);
							
							break;
							
						case MOVE_DOWN:
							NBTBase next = list.get(message.index+1);
							list.set(message.index+1, list.get(message.index));
							list.set(message.index, next);
							
							break;
							
						case REMOVE:
							list.removeTag(message.index);
							
							break;
					}
				}
			});
			
			return null;
		}
		
	}
}
