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
	private List<Integer> activeButtons;
	
	public DHDUpdatePacket(DHDTile te) {
		this.pos = te.getPos();
		
		this.activeButtons = te.getRenderer().getActiveButtons();
		
		Aunis.info("Getting active buttons: " +activeButtons.toString());
	}
	
	public void set(DHDTile te) {
		Aunis.info("Setting active buttons: " +activeButtons.toString());
		te.getRenderer().setActiveButtons(activeButtons);
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeLong( pos.toLong() );
		
		int size = activeButtons.size();
		buf.writeInt(size);
		
		for (int i=0; i<size; i++)
			buf.writeInt( activeButtons.get(i) );	
		
	}
	
	public DHDUpdatePacket (ByteBuf buf) {
		this.pos = BlockPos.fromLong( buf.readLong() );
		
		this.activeButtons = new ArrayList<Integer>();
		int size = buf.readInt();
		
		for (int i=0; i<size; i++)
			this.activeButtons.add( buf.readInt() );
		
	}
	
}