package mrjake.aunis.packet.upgrade;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.Renderer;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.RenderedTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpgradeSlotInteractToClient implements IMessage {
	public UpgradeSlotInteractToClient() {}
	
	private BlockPos dhdPos;
	private boolean hasUpgrade;
	private boolean isHoldingUpgrade;
	
	public UpgradeSlotInteractToClient(BlockPos dhdPos, boolean hasUpgrade, boolean isHoldingUpgrade) {
		this.dhdPos = dhdPos;
		this.hasUpgrade = hasUpgrade;
		this.isHoldingUpgrade = isHoldingUpgrade;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		 buf.writeLong( dhdPos.toLong() );
		 buf.writeBoolean(hasUpgrade);
		 buf.writeBoolean(isHoldingUpgrade);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		dhdPos = BlockPos.fromLong( buf.readLong() );
		hasUpgrade = buf.readBoolean();
		isHoldingUpgrade = buf.readBoolean();
	}
	
	
	public static class UpgradeSlotInteractHandler implements IMessageHandler<UpgradeSlotInteractToClient, IMessage>{

		@Override
		public IMessage onMessage(UpgradeSlotInteractToClient message, MessageContext ctx) {
						
			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityPlayerSP player = Minecraft.getMinecraft().player;
				World world = player.getEntityWorld();
				
				RenderedTileEntity renderedTileEntity = (RenderedTileEntity) world.getTileEntity(message.dhdPos);
				Renderer renderer = renderedTileEntity.getRenderer();
				
				renderer.upgradeInteract(message.hasUpgrade, message.isHoldingUpgrade);
			});
			
			return null;
		}
		
	}
}
