package mrjake.aunis.item.notebook;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PageNotebookSetNameToServer implements IMessage {
	public PageNotebookSetNameToServer() {}
	
	private EnumHand hand;
	private String name;
	private boolean notebook;
	
	public PageNotebookSetNameToServer(EnumHand hand, String name, boolean notebook) {
		this.hand = hand;
		this.name = name;
		this.notebook = notebook;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hand == EnumHand.MAIN_HAND ? 0 : 1);
		buf.writeInt(name.length());
		buf.writeCharSequence(name, StandardCharsets.UTF_8);
		buf.writeBoolean(notebook);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		int size = buf.readInt();
		name = buf.readCharSequence(size, StandardCharsets.UTF_8).toString();
		notebook = buf.readBoolean();
	}
	
	
	public static class PageNotebookSetNameServerHandler implements IMessageHandler<PageNotebookSetNameToServer, IMessage> {

		@Override
		public IMessage onMessage(PageNotebookSetNameToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				NBTTagCompound compound = stack.getTagCompound();
				
				if (message.notebook) {
					compound = NotebookItem.getSelectedPageFromCompound(compound);
				}
				
				NBTTagCompound display = new NBTTagCompound();
				display.setString("Name", message.name);
				compound.setTag("display", display);
			});
			
			return null;
		}
		
	}
}
