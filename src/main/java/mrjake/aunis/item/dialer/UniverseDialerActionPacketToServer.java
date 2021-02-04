package mrjake.aunis.item.dialer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.tileentity.stargate.StargateUniverseBaseTile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UniverseDialerActionPacketToServer implements IMessage {
	public UniverseDialerActionPacketToServer() {}
	
	private UniverseDialerActionEnum action;
	private EnumHand hand;
	private boolean next;
	
	public UniverseDialerActionPacketToServer(UniverseDialerActionEnum action, EnumHand hand, boolean next) {
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
		action = UniverseDialerActionEnum.values()[buf.readInt()];
		hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		next = buf.readBoolean();
	}
	
	
	public static class UniverseDialerActionPacketServerHandler implements IMessageHandler<UniverseDialerActionPacketToServer, IMessage> {

		@Override
		public IMessage onMessage(UniverseDialerActionPacketToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				
				if (stack.getItem() == AunisItems.UNIVERSE_DIALER && stack.hasTagCompound()) {
					NBTTagCompound compound = stack.getTagCompound();
					UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
					byte selected = compound.getByte("selected");

					switch (message.action) {
					
						case MODE_CHANGE:
							if (message.next) // message.offset < 0
								mode = mode.next();
							else
								mode = mode.prev();
							
							compound.setByte("mode", mode.id);
							compound.setByte("selected", (byte) 0);
							
							break;
							
							
						case ADDRESS_CHANGE:							
							int addressCount = compound.getTagList(mode.tagListName, NBT.TAG_COMPOUND).tagCount();
							
							if (message.next && selected < addressCount-1) // message.offset < 0
								compound.setByte("selected", (byte) (selected+1));
							
							if (!message.next && selected > 0)
								compound.setByte("selected", (byte) (selected-1));
							
							break;
							
							
						case ABORT:
							if (compound.hasKey("linkedGate")) {
								BlockPos pos = BlockPos.fromLong(compound.getLong("linkedGate"));
								StargateUniverseBaseTile gateTile = (StargateUniverseBaseTile) world.getTileEntity(pos);
								
								if (gateTile.getStargateState() == EnumStargateState.DIALING) {
									gateTile.abort();
									player.sendStatusMessage(new TextComponentTranslation("item.aunis.universe_dialer.aborting"), true);
								}
								
								else {
									player.sendStatusMessage(new TextComponentTranslation("item.aunis.universe_dialer.not_dialing"), true);
								}
							}
							
							else {
								player.sendStatusMessage(new TextComponentTranslation("item.aunis.universe_dialer.not_linked"), true);
							}
							
							break;
					}
				}
			});
			
			return null;
		}
		
	}
}
