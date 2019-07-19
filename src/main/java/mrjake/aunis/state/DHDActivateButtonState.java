package mrjake.aunis.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;

public class DHDActivateButtonState extends State {
	public DHDActivateButtonState() {}
	
	public boolean clearOnly;
	public List<Integer> idList;
	
	public DHDActivateButtonState(boolean clearOnly) {
		this.clearOnly = clearOnly;
	}
	
	public DHDActivateButtonState(List<Integer> idList) {
		this.idList = idList;
	}
	
	public DHDActivateButtonState(int id) {
		this.idList = Arrays.asList(id);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(clearOnly);
		
		if (!clearOnly) {
			buf.writeInt(idList.size());

			for (int id : idList)
				buf.writeInt(id);
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		clearOnly = buf.readBoolean();
		
		if (!clearOnly) {
			int count = buf.readInt();
			idList = new ArrayList<Integer>(count);
			
			for (int i=0; i<count; i++) {
				idList.add(buf.readInt());
			}
		}
	}
}
