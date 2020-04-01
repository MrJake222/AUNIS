package mrjake.aunis.item.dialer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.item.AunisItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UniverseDialerAddressChangeToServer implements IMessage {
	public UniverseDialerAddressChangeToServer() {}
	
	private EnumHand hand;
	private byte offset;
		
	public UniverseDialerAddressChangeToServer(EnumHand hand, byte offset) {
		this.hand = hand;
		this.offset = offset;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hand == EnumHand.MAIN_HAND ? 0 : 1);
		buf.writeByte(offset);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		offset = buf.readByte();
	}
	
	
	public static class UniverseDialerAddressChangeServerHandler implements IMessageHandler<UniverseDialerAddressChangeToServer, IMessage> {
		
		public IMessage onMessage(UniverseDialerAddressChangeToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				
				if (stack.getItem() == AunisItems.UNIVERSE_DIALER && stack.hasTagCompound()) {
					NBTTagCompound compound = stack.getTagCompound();
					UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
					
					byte selected = compound.getByte("selected");
					int addressCount = compound.getTagList(mode.tagListName, NBT.TAG_COMPOUND).tagCount();
					
					if (message.offset < 0 && selected < addressCount-1)
						compound.setByte("selected", (byte) (selected+1));
					else if(message.offset > 0 && selected > 0)
						compound.setByte("selected", (byte) (selected-1));
					
					stack.setTagCompound(compound);
				}
			});
			
			return null;
		}
	}
}
