package mrjake.aunis.packet.gate.onLoadUpdate;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.util.math.BlockPos;

public class DHDUpdatePacket {
	BlockPos pos;
	
	// Chevrons
	private List<Boolean> activeButtons;
	
	public DHDUpdatePacket(DHDTile te) {
		this.pos = te.getPos();
		
		this.activeButtons = te.getRenderer().getActiveButtonList();
		
		Aunis.info("Getting active buttons: " +activeButtons.toString());
	}
	
	/*public DHDUpdatePacket(List<Boolean> activeButtons) {
		this.activeButtons = activeButtons;
	}*/
	
	public void set(DHDTile te) {
		Aunis.info("Setting active buttons: " +activeButtons.toString());
		te.getRenderer().setActiveButtons(activeButtons);
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeLong( pos.toLong() );
		
		for (Boolean state : activeButtons)
			buf.writeBoolean(state);
		
	}
	
	public DHDUpdatePacket (ByteBuf buf) {
		this.pos = BlockPos.fromLong( buf.readLong() );
		
		this.activeButtons = new ArrayList<Boolean>();
		
		for (int i=0; i<=38; i++)
			this.activeButtons.add( buf.readBoolean() );
	}
	
}