package mrjake.aunis.item.dialer;

import li.cil.oc.api.network.Packet;
import li.cil.oc.api.network.WirelessEndpoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class UniverseDialerWirelessEndpoint implements WirelessEndpoint {

	private EntityPlayer player;
	
	public UniverseDialerWirelessEndpoint(EntityPlayer player) {
		this.player = player;
	}
	
	@Override
	public int x() { return player.getPosition().getX(); }
	
	@Override
	public int y() { return player.getPosition().getY(); }
	
	@Override
	public int z() { return player.getPosition().getZ(); }
	
	@Override
	public World world() { return player.getEntityWorld(); }
	
	@Override
	public void receivePacket(Packet packet, WirelessEndpoint sender) {}
}
