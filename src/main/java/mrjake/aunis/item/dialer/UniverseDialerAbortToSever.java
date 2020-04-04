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
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UniverseDialerAbortToSever implements IMessage {
	public UniverseDialerAbortToSever() {}
	
	private EnumHand hand;
		
	public UniverseDialerAbortToSever(EnumHand hand) {
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
	
	
	public static class UniverseDialerAbortServerHandler implements IMessageHandler<UniverseDialerAbortToSever, IMessage> {
		
		public IMessage onMessage(UniverseDialerAbortToSever message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();

			world.addScheduledTask(() -> {
				ItemStack stack = player.getHeldItem(message.hand);
				
				if (stack.getItem() == AunisItems.UNIVERSE_DIALER && stack.hasTagCompound()) {
					NBTTagCompound compound = stack.getTagCompound();
					if (compound.hasKey("linkedGate")) {
						BlockPos pos = BlockPos.fromLong(compound.getLong("linkedGate"));
						StargateUniverseBaseTile gateTile = (StargateUniverseBaseTile) world.getTileEntity(pos);
						
						if (gateTile.getStargateState() == EnumStargateState.DIALING) {
							gateTile.abort();
							player.sendStatusMessage(new TextComponentTranslation("item.aunis.universe_dialer.aborting"), true);
						}
					}
				}
			});
			
			return null;
		}
	}
}
