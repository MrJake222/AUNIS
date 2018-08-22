package mrjake.aunis.renderer;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mrjake.aunis.Aunis;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class DHDRendererState extends RendererState {
	
	// Buttons
	private List<Integer> activeButtons;
	
	public DHDRendererState(BlockPos pos) {
		super(pos);
		
		this.activeButtons = new ArrayList<Integer>();
	}
	
	public DHDRendererState(BlockPos pos, List<Integer> activeButtons) {
		super(pos);
		
		this.activeButtons = activeButtons;
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeLong( pos.toLong() );
		
		int size = activeButtons.size();
		buf.writeInt(size);
		
		for (int i=0; i<size; i++)
			buf.writeInt( activeButtons.get(i) );	
		
	}
	
	public void fromBytes(ByteBuf buf) {
		this.pos = BlockPos.fromLong( buf.readLong() );
		
		this.activeButtons = new ArrayList<Integer>();
		int size = buf.readInt();
		
		for (int i=0; i<size; i++)
			this.activeButtons.add( buf.readInt() );
	}
}