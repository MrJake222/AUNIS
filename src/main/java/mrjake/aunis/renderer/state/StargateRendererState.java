package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.StargateRenderer.EnumVortexState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class StargateRendererState extends RendererState {
	
	// Chevrons
	public int activeChevrons;
	public boolean isFinalActive;

	// Ring		
	public double ringAngularRotation;

	/**
	 * Stores world's time when rotation started.
	 */
	public long tickStart;
	
	/**
	 * Stores starting ring pos to correctly shift the next rotation
	 * 
	 * Set by requestStart()
	 */
	public double ringStartingRotation;

	/**
	 * Defines when ring is spinning ie. no stop animation was performed.
	 */
	public boolean isSpinning;
	
	/**
	 * Set by requestStop(). Set when ring needs to be stopped.
	 */
	public boolean stopRequested;
	
	/**
	 * Time when requestStop() was called
	 */
	public long tickStopRequested;

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
	public StargateRendererState(BlockPos pos) {
		this(pos, 0, false, 0, 0, 0, false, false, 0, false, EnumVortexState.FORMING, false, false, false);
	}
	
	public StargateRendererState(
			BlockPos pos,
			
			int activeChevrons,
			boolean isFinalActive,
			
			float ringAngularRotation,
			double ringStartingRotation,
			long tickStart,
			boolean isSpinning,
			boolean stopRequested,
			long tickStopRequested,
			
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
		this.ringStartingRotation = ringStartingRotation;

		this.tickStart = tickStart;
		this.isSpinning = isSpinning;
		this.stopRequested = stopRequested;
		this.tickStopRequested = tickStopRequested;
		
		// Gate
		this.doEventHorizonRender = doEventHorizonRender;
		this.vortexState = vortexState;
		this.openingSoundPlayed = openingSoundPlayed;
		this.dialingComplete = dialingComplete;
		this.horizonUnstable = horizonInstable;
	}
	
	public StargateRendererState(ByteBuf buf) {
		super(buf);
	}
	
	public StargateRendererState(NBTTagCompound compound) {
		super(compound);
	}
	
	@Override
	protected String getKeyName() {
		return "rendererState";
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(activeChevrons);
		buf.writeBoolean(isFinalActive);
		
		buf.writeDouble(ringAngularRotation);
		buf.writeDouble(ringStartingRotation);
		buf.writeLong(tickStart);
		buf.writeBoolean(isSpinning);
		buf.writeBoolean(stopRequested);
		buf.writeLong(tickStopRequested);
		
		buf.writeBoolean(doEventHorizonRender);
		buf.writeInt(vortexState.index);
		buf.writeBoolean(openingSoundPlayed);
		buf.writeBoolean(dialingComplete);
		buf.writeBoolean(horizonUnstable);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {		
		activeChevrons = buf.readInt();
		isFinalActive = buf.readBoolean();
		
		ringAngularRotation = buf.readDouble();
		ringStartingRotation = buf.readDouble();
		tickStart = buf.readLong();
		isSpinning = buf.readBoolean();
		stopRequested = buf.readBoolean();
		tickStopRequested = buf.readLong();
		
		doEventHorizonRender = buf.readBoolean();
		vortexState = EnumVortexState.valueOf( buf.readInt() );
		openingSoundPlayed = buf.readBoolean();
		dialingComplete = buf.readBoolean();
		horizonUnstable = buf.readBoolean();
	}
}