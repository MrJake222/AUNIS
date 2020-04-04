package mrjake.aunis.item.dialer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.item.AunisItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class UniverseDialerModeChangeToServer implements IMessage {
	public UniverseDialerModeChangeToServer() {}
	
	private EnumHand hand;
	private byte offset;
		
	public UniverseDialerModeChangeToServer(EnumHand hand, byte offset) {
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
	
	
	public static class UniverseDialerModeChangeServerHandler implements IMessageHandler<UniverseDialerModeChangeToServer, IMessage> {
		
		public IMessage onMessage(UniverseDialerModeChangeToServer message, net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();
			
			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				
				if (stack.getItem() == AunisItems.UNIVERSE_DIALER && stack.hasTagCompound()) {
					NBTTagCompound compound = stack.getTagCompound();
					UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
					
					if (message.offset < 0)
						mode = mode.next();
					else if(message.offset > 0)
						mode = mode.prev();
					
					compound.setByte("mode", mode.id);
					compound.setByte("selected", (byte) 0);
					
					stack.setTagCompound(compound);
				}
			});
			
			return null;
		}
	}
}
