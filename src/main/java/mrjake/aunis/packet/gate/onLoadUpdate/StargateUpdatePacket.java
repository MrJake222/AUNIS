package mrjake.aunis.packet.gate.onLoadUpdate;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.render.StargateRenderer;
import mrjake.aunis.render.StargateRenderer.EnumVortexState;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.util.math.BlockPos;

public class StargateUpdatePacket {
	BlockPos pos;
	
	// Chevrons
	private List<Boolean> activeChevronList;

	// Ring
	private float ringAngularRotation;

	private boolean ringSpin;
	private long ringSpinStart;

	// Gate
	private boolean doEventHorizonRender = false;
	private EnumVortexState vortexState;
	private boolean soundPlayed;
	
	public StargateUpdatePacket(StargateBaseTile te) {
		this.pos = te.getPos();
		
		StargateRenderer renderer = te.getRenderer();
		
		this.activeChevronList = renderer.getActiveChevronsList();
		this.ringAngularRotation = renderer.ringAngularRotation;
		this.ringSpin = renderer.ringSpin;
		this.ringSpinStart = renderer.ringSpinStart;
		this.doEventHorizonRender = renderer.doEventHorizonRender;
		this.vortexState = renderer.vortexState;
		this.soundPlayed = renderer.soundPlayed;
	}
	
	public void set(StargateBaseTile te) {
		StargateRenderer renderer = te.getRenderer();
		
		renderer.setActiveChevrons(this.activeChevronList);
		renderer.ringAngularRotation = this.ringAngularRotation;
		renderer.ringSpin = this.ringSpin;
		renderer.ringSpinStart = this.ringSpinStart;
		renderer.vortexState = this.vortexState;
		renderer.soundPlayed = this.soundPlayed;
		
		renderer.doEventHorizonRender = this.doEventHorizonRender;
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeLong( pos.toLong() );
		
		for ( Boolean state : activeChevronList )
			buf.writeBoolean(state);
		
		buf.writeFloat(ringAngularRotation);
		buf.writeBoolean(ringSpin);
		buf.writeLong(ringSpinStart);
		buf.writeBoolean(doEventHorizonRender);
		buf.writeInt(vortexState.index);
		buf.writeBoolean(soundPlayed);
	}
	
	public StargateUpdatePacket(ByteBuf buf) {
		this.pos = BlockPos.fromLong( buf.readLong() );
		
		activeChevronList = new ArrayList<Boolean>();
		for (int i=0; i<9; i++)
			activeChevronList.add( buf.readBoolean() );
		
		this.ringAngularRotation = buf.readFloat();
		this.ringSpin = buf.readBoolean();
		this.ringSpinStart = buf.readLong();
		this.doEventHorizonRender = buf.readBoolean();
		this.vortexState = EnumVortexState.valueOf( buf.readInt() );
		this.soundPlayed = buf.readBoolean();
	}
	
}