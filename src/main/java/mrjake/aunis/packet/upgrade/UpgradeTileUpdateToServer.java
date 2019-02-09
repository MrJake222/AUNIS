package mrjake.aunis.packet.upgrade;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.tileentity.TileEntityRenderer;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpgradeTileUpdateToServer implements IMessage {
	public UpgradeTileUpdateToServer() {}
	
	private BlockPos dhdPos;
	private boolean hasUpgrade;
	
	public UpgradeTileUpdateToServer(BlockPos dhdPos, boolean hasUpgrade) {
		this.dhdPos = dhdPos;
		this.hasUpgrade = hasUpgrade;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		 buf.writeLong( dhdPos.toLong() );
		 buf.writeBoolean(hasUpgrade);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		dhdPos = BlockPos.fromLong( buf.readLong() );
		hasUpgrade = buf.readBoolean();
	}
	
	
	public static class UpgradeTileUpdateHandler implements IMessageHandler<UpgradeTileUpdateToServer, IMessage>{

		@Override
		public IMessage onMessage(UpgradeTileUpdateToServer message, MessageContext ctx) {			
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer worldServer = player.getServerWorld();
			
			worldServer.addScheduledTask(() -> {
				TileEntityRenderer TileEntityRenderer = (TileEntityRenderer) worldServer.getTileEntity(message.dhdPos);
				TileEntityRenderer.setUpgrade(message.hasUpgrade);
				TileEntityRenderer.setInsertAnimation(false);
				
				ItemStack itemStack;
				
				if (TileEntityRenderer instanceof StargateBaseTile)
					itemStack = new ItemStack(AunisItems.crystalGlyphStargate);
				else
					itemStack = new ItemStack(AunisItems.crystalGlyphDhd);
				
				if (!message.hasUpgrade) {
					if (player.getHeldItemMainhand().isItemEqual(ItemStack.EMPTY))
						player.setHeldItem(EnumHand.MAIN_HAND, itemStack);
					else
						player.inventory.addItemStackToInventory(itemStack);
				}
			});
			
			return null;
		}
		
	}
}
