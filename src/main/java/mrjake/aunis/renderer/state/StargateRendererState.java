package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.stargate.StargateRenderer.EnumVortexState;

public class StargateRendererState extends RendererState {
	
	// Chevrons
	public int activeChevrons;
	public boolean isFinalActive;

	// Ring		
	public double ringAngularRotation;
	public SpinState spinState;
	
	// Gate
	public boolean doEventHorizonRender;
	public EnumVortexState vortexState;
	public boolean openingSoundPlayed;
	public boolean dialingComplete;
	
	/**
	 * When power is low, this becomes true. Flashing begins.
	 *  
	 * Individual flashes are handled client-side
	 */
	public boolean horizonUnstable;
	
//	@Override
//	public String toString() {
//		return String.format(pos+": activeChevrons: %d, isFinalActive: %b, doEventHorizonRender: %b, vortexState: %s, openingSoundPlayed: %b", activeChevrons, isFinalActive,
//				doEventHorizonRender, vortexState.toString(), openingSoundPlayed);
//	}
	
	// Default state
	public StargateRendererState() {
		this(0, false, 0, new SpinState(), false, EnumVortexState.FORMING, false, false, false);
	}
	
	public StargateRendererState(			
			int activeChevrons,
			boolean isFinalActive,
			
			float ringAngularRotation,
			SpinState spinState,
			
			boolean doEventHorizonRender,
			EnumVortexState vortexState,
			boolean openingSoundPlayed,
			boolean dialingComplete,
			boolean horizonInstable) {
				
		// Chevrons
		this.activeChevrons = activeChevrons;
		this.isFinalActive = isFinalActive;
		
		// Ring		
		this.ringAngularRotation = ringAngularRotation;
		this.spinState = spinState;
		
		// Gate
		this.doEventHorizonRender = doEventHorizonRender;
		this.vortexState = vortexState;
		this.openingSoundPlayed = openingSoundPlayed;
		this.dialingComplete = dialingComplete;
		this.horizonUnstable = horizonInstable;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(activeChevrons);
		buf.writeBoolean(isFinalActive);
		
		buf.writeDouble(ringAngularRotation);
		spinState.toBytes(buf);
		
		buf.writeBoolean(doEventHorizonRender);
		buf.writeInt(vortexState.index);
		buf.writeBoolean(openingSoundPlayed);
		buf.writeBoolean(dialingComplete);
		buf.writeBoolean(horizonUnstable);
	}
	
	@Override
	public RendererState fromBytes(ByteBuf buf) {		
		activeChevrons = buf.readInt();
		isFinalActive = buf.readBoolean();
				
		ringAngularRotation = buf.readDouble();
		
		if (spinState == null)
			spinState = new SpinState();
		
		spinState.fromBytes(buf);
		
		doEventHorizonRender = buf.readBoolean();
		vortexState = EnumVortexState.valueOf( buf.readInt() );
		openingSoundPlayed = buf.readBoolean();
		dialingComplete = buf.readBoolean();
		horizonUnstable = buf.readBoolean();
		
		return this;
	}
}