package mrjake.aunis.packet.upgrade;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.upgrade.ITileEntityUpgradeable;
import mrjake.aunis.upgrade.UpgradeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpgradeSlotInteractToClient implements IMessage {
	public UpgradeSlotInteractToClient() {}
	
	private BlockPos pos;
	private EnumUpgradeAction upgradeAction;
	
	public UpgradeSlotInteractToClient(BlockPos pos, EnumUpgradeAction upgradeAction) {
		this.pos = pos;
		this.upgradeAction = upgradeAction;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		 buf.writeLong( pos.toLong() );
		 buf.writeInt(upgradeAction.id);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong( buf.readLong() );
		upgradeAction = EnumUpgradeAction.values()[buf.readInt()];
	}
	
	
	public static class UpgradeSlotInteractHandler implements IMessageHandler<UpgradeSlotInteractToClient, IMessage>{

		@Override
		public IMessage onMessage(UpgradeSlotInteractToClient message, MessageContext ctx) {
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			World world = player.getEntityWorld();	
						
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				ITileEntityUpgradeable upgradeable = (ITileEntityUpgradeable) world.getTileEntity(message.pos);
				UpgradeRenderer renderer = upgradeable.getUpgradeRenderer();
								
				switch (message.upgradeAction) {
					case PUT_UPGRADE:
						renderer.putUpgradeInSlot();
						break;
						
					case INSERT_UPGRADE:
						renderer.insertUpgrade();
						break;
						
					case REMOVE_UPGRADE:
						renderer.removeUpgrade();
						break;
					
					case POP_UPGRADE:
						renderer.popUpgrade();
						break;
	
					default:
						break;
				}
			});
			
			return null;
		}
		
	}
}
