package mrjake.aunis.item.notebook;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.SoundEventEnum;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class NotebookActionPacketToServer implements IMessage {
	public NotebookActionPacketToServer() {}
	
	private NotebookActionEnum action;
	private EnumHand hand;
	private boolean next;
	
	public NotebookActionPacketToServer(NotebookActionEnum action, EnumHand hand, boolean next) {
		this.action = action;
		this.hand = hand;
		this.next = next;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(action.ordinal());
		buf.writeInt(hand == EnumHand.MAIN_HAND ? 0 : 1);
		buf.writeBoolean(next);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		action = NotebookActionEnum.values()[buf.readInt()];
		hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		next = buf.readBoolean();
	}
	
	
	public static class NotebookActionPacketServerHandler implements IMessageHandler<NotebookActionPacketToServer, IMessage> {

		@Override
		public IMessage onMessage(NotebookActionPacketToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				
				if (stack.getItem() == AunisItems.NOTEBOOK_ITEM && stack.hasTagCompound()) {
					NBTTagCompound compound = stack.getTagCompound();
					int selected = compound.getInteger("selected");

					switch (message.action) {
						case ADDRESS_CHANGE:							
							int addressCount = compound.getTagList("addressList", NBT.TAG_COMPOUND).tagCount();
														
							if (message.next && selected < addressCount-1) { // message.offset < 0
								compound.setInteger("selected", (byte) (selected+1));
								AunisSoundHelper.playSoundEvent(world, player.getPosition(), SoundEventEnum.PAGE_FLIP);
							}
							
							if (!message.next && selected > 0) {
								compound.setInteger("selected", (byte) (selected-1));
								AunisSoundHelper.playSoundEvent(world, player.getPosition(), SoundEventEnum.PAGE_FLIP);
							}
							
							break;
					}
				}
			});
			
			return null;
		}
		
	}
}
