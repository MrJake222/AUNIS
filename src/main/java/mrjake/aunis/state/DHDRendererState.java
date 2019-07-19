package mrjake.aunis.state;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

public class DHDRendererState extends State {
	public DHDRendererState() {}
	
	public List<Integer> activeButtons = new ArrayList<Integer>();
	
	public DHDRendererState(List<Integer> activeButtons) {
		this.activeButtons = activeButtons;
	}
	
	public void toBytes(ByteBuf buf) {		
		int size = activeButtons.size();
		buf.writeInt(size);
		
		for (int i=0; i<size; i++) {
			buf.writeInt(activeButtons.get(i));
		}	
	}
	
	public void fromBytes(ByteBuf buf) {		
		int size = buf.readInt();
		
		for (int i=0; i<size; i++) {
			this.activeButtons.add(buf.readInt());
		}
	}
}