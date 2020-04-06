package mrjake.aunis.item.dialer;

import li.cil.oc.api.network.Packet;
import li.cil.oc.api.network.WirelessEndpoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
	public void receivePacket(Packet packet, WirelessEndpoint sender) {
		String data = "";
		
		for (int i=0; i<packet.data().length; i++) {
			Object dataObj = packet.data()[i];
			
			if (dataObj instanceof byte[]) {
				dataObj = new String((byte[]) dataObj);
			}
			
			if (dataObj instanceof Boolean) {
				if (((Boolean) dataObj).booleanValue())
					data += TextFormatting.GREEN;
				else
					data += TextFormatting.RED;
			}
			
			else if (dataObj instanceof Double) {
				data += TextFormatting.LIGHT_PURPLE;
			}
			
			data += dataObj;	
			
			if (i < packet.data().length-1)
				data += ", ";
			
			data += TextFormatting.AQUA;
		}
		
		if (data.isEmpty())
			data = "[no data]";
		
		player.sendStatusMessage(new TextComponentTranslation("item.aunis.universe_dialer.oc_message_received").appendText(TextFormatting.AQUA + data), false);
	}
}
