package mrjake.aunis.packet.gui.entry;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.item.dialer.UniverseDialerItem;
import mrjake.aunis.item.dialer.UniverseDialerMode;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OCActionToServer implements IMessage {
	public OCActionToServer() {}
	
	private EnumHand hand;
	private OCActionEnum action;
	private int index;
	private String string;
	
	public OCActionToServer(EnumHand hand, OCActionEnum action, int index, String string) {
		this.hand = hand;
		this.action = action;
		this.index = index;
		this.string = string;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hand.ordinal());
		buf.writeInt(action.ordinal());
		buf.writeInt(index);
		
		buf.writeInt(string.length());
		buf.writeCharSequence(string, StandardCharsets.UTF_8);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		hand = EnumHand.values()[buf.readInt()];
		action = OCActionEnum.values()[buf.readInt()];
		index = buf.readInt();
		
		int size = buf.readInt();
		string = buf.readCharSequence(size, StandardCharsets.UTF_8).toString();
	}
	
	
	public static class OCActionServerHandler implements IMessageHandler<OCActionToServer, IMessage> {

		@Override
		public IMessage onMessage(OCActionToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				NBTTagCompound compound = stack.getTagCompound();
				NBTTagList list = compound.getTagList(UniverseDialerMode.OC.tagListName, NBT.TAG_COMPOUND);
				
				switch (message.action) {
					case CHANGE_ADDRESS:
						UniverseDialerItem.changeOCMessageAtIndex(list, message.index, (ocMessage) -> ocMessage.address = message.string);
						break;
						
					case CHANGE_PORT:
						UniverseDialerItem.changeOCMessageAtIndex(list, message.index, (ocMessage) -> ocMessage.port = (short) Integer.parseInt(message.string));
						break;
						
					case CHANGE_PARAMS:
						UniverseDialerItem.changeOCMessageAtIndex(list, message.index, (ocMessage) -> ocMessage.dataStr = message.string);
						break;
				}
			});
			
			return null;
		}
		
	}
}
