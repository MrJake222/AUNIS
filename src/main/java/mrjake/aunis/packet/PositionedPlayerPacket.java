package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PositionedPlayerPacket implements IMessage {
	public PositionedPlayerPacket() {}
	
	protected BlockPos pos;
	protected int entityPlayerId;
		
	public PositionedPlayerPacket(BlockPos pos, EntityPlayer player) {
		this.pos = pos;
		this.entityPlayerId = player.getEntityId();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		buf.writeInt(entityPlayerId);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		entityPlayerId = buf.readInt();
	}
	
	public void respond(World world, IMessage message) {
		AunisPacketHandler.INSTANCE.sendTo(message, (EntityPlayerMP) world.getEntityByID(entityPlayerId));
	}
}
