package mrjake.aunis.renderer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.StargateRenderer.EnumVortexState;
import net.minecraft.util.math.BlockPos;

public class StargateRendererState extends RendererState {
	
	// Chevrons
	public int activeChevrons;
	public boolean isFinalActive;

	// Ring
	public float ringAngularRotation;
	public boolean ringSpin;
	public long ringSpinStart;

	// Gate
	public boolean doEventHorizonRender;
	public EnumVortexState vortexState;
	public boolean soundPlayed;
	public boolean dialingComplete;
	
	@Override
	public String toString() {
		return String.format(pos+":  activeChevrons: %d, isFinalActive: %b, doEventHorizonRender: %b, vortexState: %s, soundPlayed: %b", activeChevrons, isFinalActive,
				doEventHorizonRender, vortexState.toString(), soundPlayed);
	}
	
	// Default state
	public StargateRendererState(BlockPos pos) {
		this(pos, 0, false, 0, false, 0, false, EnumVortexState.FORMING, false, false);
	}
	
	public StargateRendererState(BlockPos pos, int activeChevrons, boolean isFinalActive, float ringAngularRotation, boolean ringSpin, long ringSpinStart, boolean doEventHorizonRender, EnumVortexState vortexState, boolean soundPlayed, boolean dialingComplete) {
		super(pos);
		
		this.activeChevrons = activeChevrons;
		this.isFinalActive = isFinalActive;
		
		this.ringAngularRotation = ringAngularRotation;
		this.ringSpin = ringSpin;
		this.ringSpinStart = ringSpinStart;
		
		this.doEventHorizonRender = doEventHorizonRender;
		this.vortexState = vortexState;
		this.soundPlayed = soundPlayed;
		this.dialingComplete = dialingComplete;
	}
	
	public StargateRendererState(ByteBuf buf) {
		super(buf);
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeLong( pos.toLong() );
		
		buf.writeInt(activeChevrons);
		buf.writeBoolean(isFinalActive);
		
		buf.writeFloat(ringAngularRotation);
		buf.writeBoolean(ringSpin);
		buf.writeLong(ringSpinStart);
		
		buf.writeBoolean(doEventHorizonRender);
		buf.writeInt(vortexState.index);
		buf.writeBoolean(soundPlayed);
		buf.writeBoolean(dialingComplete);
	}
	
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong( buf.readLong() );
		
		activeChevrons = buf.readInt();
		isFinalActive = buf.readBoolean();
		
		ringAngularRotation = buf.readFloat();
		ringSpin = buf.readBoolean();
		ringSpinStart = buf.readLong();
		
		doEventHorizonRender = buf.readBoolean();
		vortexState = EnumVortexState.valueOf( buf.readInt() );
		soundPlayed = buf.readBoolean();
		dialingComplete = buf.readBoolean();
	}
}