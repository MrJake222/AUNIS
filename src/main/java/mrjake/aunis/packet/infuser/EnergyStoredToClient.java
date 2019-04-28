package mrjake.aunis.packet.infuser;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.renderer.crystalinfuser.CrystalInfuserRenderer;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EnergyStoredToClient extends PositionedPacket {
	public EnergyStoredToClient() {}
	
	private int energyStored;
	
	public EnergyStoredToClient(BlockPos pos, int energyStored) {
		super(pos);
		
		this.energyStored = energyStored;
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(energyStored);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		energyStored = buf.readInt();
	}
	
	public static class EnergyStorageToClientHandler implements IMessageHandler<EnergyStoredToClient, IMessage> {
		
		@Override
		public IMessage onMessage(EnergyStoredToClient message, MessageContext ctx) {
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityPlayer player = Aunis.proxy.getPlayerInMessageHandler(ctx);
				World world = player.getEntityWorld();

				CrystalInfuserTile infuserTile = (CrystalInfuserTile) world.getTileEntity(message.pos);
				
				if (infuserTile != null) {
					CrystalInfuserRenderer infuserRenderer = (CrystalInfuserRenderer) infuserTile.getRenderer();
				
					infuserRenderer.setEnergyStored(message.energyStored);
				}
			});
			
			return null;
		}
	}
}
