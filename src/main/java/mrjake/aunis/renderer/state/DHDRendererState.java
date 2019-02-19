package mrjake.aunis.renderer.state;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class DHDRendererState extends RendererState {
	
	// Buttons
	public List<Integer> activeButtons;
	
	public DHDRendererState() {
		this.activeButtons = new ArrayList<Integer>();
	}
	
	public DHDRendererState(List<Integer> activeButtons) {
		this.activeButtons = activeButtons;
	}
	
	public DHDRendererState(ByteBuf buf) {
		super(buf);
	}
	
	public DHDRendererState(NBTTagCompound compound) {
		super(compound);
	}

	@Override
	public String toString() {
		return "activeButtons: " + activeButtons;
	}
	
	@Override
	protected String getKeyName() {
		return "rendererState";
	}
	
	public void toBytes(ByteBuf buf) {		
		int size = activeButtons.size();
		buf.writeInt(size);
		
		for (int i=0; i<size; i++)
			buf.writeInt( activeButtons.get(i) );	
		
	}
	
	public void fromBytes(ByteBuf buf) {		
		this.activeButtons = new ArrayList<Integer>();
		int size = buf.readInt();
		
		for (int i=0; i<size; i++)
			this.activeButtons.add( buf.readInt() );
	}
}