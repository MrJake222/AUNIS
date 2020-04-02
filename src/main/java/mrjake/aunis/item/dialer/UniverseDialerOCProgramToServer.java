package mrjake.aunis.item.dialer;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.item.AunisItems;
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

public class UniverseDialerOCProgramToServer implements IMessage {
	public UniverseDialerOCProgramToServer() {}
	
	private EnumHand hand;
	private String name;
	private String address;
	private short port;
	private String data;
		
	public UniverseDialerOCProgramToServer(EnumHand hand, String name, String address, short port, String data) {
		this.hand = hand;
		this.name = name;
		this.address = address;
		this.port = port;
		this.data = data;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hand == EnumHand.MAIN_HAND ? 0 : 1);
		buf.writeShort(port);
		
		writeString(buf, name);
		writeString(buf, address);
		writeString(buf, data);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		port = buf.readShort();
		
		name = readString(buf);
		address = readString(buf);
		data = readString(buf);
	}
	
	private static void writeString(ByteBuf buf, String string) {
		buf.writeInt(string.length());
		buf.writeCharSequence(string, StandardCharsets.UTF_8);
	}
	
	private static String readString(ByteBuf buf) {
		int len = buf.readInt();
		return buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
	}
	
	
	public static class UniverseDialerOCProgramServerHandler implements IMessageHandler<UniverseDialerOCProgramToServer, IMessage> {
		
		public IMessage onMessage(UniverseDialerOCProgramToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				
				if (stack.getItem() == AunisItems.UNIVERSE_DIALER && stack.hasTagCompound()) {
					NBTTagCompound compound = stack.getTagCompound();
					UniverseDialerOCMessage ocMessage = new UniverseDialerOCMessage(message.name, message.address, message.port, message.data);
					
					NBTTagList ocList = compound.getTagList(UniverseDialerMode.OC.tagListName, NBT.TAG_COMPOUND);
					ocList.appendTag(ocMessage.serializeNBT());
					compound.setTag(UniverseDialerMode.OC.tagListName, ocList);
				}
			});
			
			return null;
		}
	}
}
