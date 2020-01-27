package mrjake.aunis.state;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.transportrings.TransportRings;

public class TransportRingsGuiState extends State {

	private boolean inGrid;
	public boolean isInGrid() { return inGrid; }
	
	private int address;
	public int getAddress() { return address; }
	
	private String name;
	public String getName() { return name != null ? name : ""; }
	
	private List<TransportRings> ringsList = new ArrayList<>();
	public List<TransportRings> getRings() { return ringsList; }
	
	public TransportRingsGuiState() {}
	
	public TransportRingsGuiState(TransportRings rings, Collection<TransportRings> ringsList) {
		inGrid = rings.isInGrid();
		
		if (inGrid) {
			this.address = rings.getAddress();
			this.name = rings.getName();
			
//			this.ringsList.add(rings);
			this.ringsList.addAll(ringsList);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {		
		buf.writeBoolean(inGrid);
		
		if (inGrid) {
			buf.writeInt(address);
			buf.writeInt(name.length());
			buf.writeCharSequence(name, StandardCharsets.UTF_8);
			
			buf.writeInt(ringsList.size());
			
			for (TransportRings rings : ringsList) {
				buf.writeInt(rings.getAddress());
				buf.writeInt(rings.getName().length());
				buf.writeCharSequence(rings.getName(), StandardCharsets.UTF_8);
			}
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		inGrid = buf.readBoolean();
		
		if (inGrid) {
			address = buf.readInt();
			int length = buf.readInt();
			name = buf.readCharSequence(length, StandardCharsets.UTF_8).toString();
			
			int size = buf.readInt();
			for (int i=0; i<size; i++) {
				int address = buf.readInt();
				
				length = buf.readInt();
				String name = buf.readCharSequence(length, StandardCharsets.UTF_8).toString();
				
				ringsList.add(new TransportRings(address, name));
			}
		}
	}
}
