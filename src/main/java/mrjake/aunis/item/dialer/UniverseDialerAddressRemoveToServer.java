package mrjake.aunis.item.dialer;

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

public class UniverseDialerAddressRemoveToServer implements IMessage {
	public UniverseDialerAddressRemoveToServer() {}
	
	private EnumHand hand;
		
	public UniverseDialerAddressRemoveToServer(EnumHand hand) {
		this.hand = hand;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hand == EnumHand.MAIN_HAND ? 0 : 1);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
	}
	
	
	public static class UniverseDialerAddressRemoveServerHandler implements IMessageHandler<UniverseDialerAddressRemoveToServer, IMessage> {
		
		public IMessage onMessage(UniverseDialerAddressRemoveToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				
				if (stack.getItem() == AunisItems.UNIVERSE_DIALER && stack.hasTagCompound()) {
					NBTTagCompound compound = stack.getTagCompound();
					UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));

					if (mode == UniverseDialerMode.MEMORY) {
						NBTTagList addressList = compound.getTagList("saved", NBT.TAG_COMPOUND);
						byte selected = compound.getByte("addressSelected");
						
						addressList.removeTag(selected);					
						stack.setTagCompound(compound);
					}
				}
			});
			
			return null;
		}
	}
}
