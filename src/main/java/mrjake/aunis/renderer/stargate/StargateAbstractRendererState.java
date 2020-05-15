package mrjake.aunis.renderer.stargate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.stargate.StargateAbstractRenderer.EnumVortexState;
import mrjake.aunis.renderer.stargate.StargateRendererStatic.QuadStrip;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.state.State;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class StargateAbstractRendererState extends State {
	public StargateAbstractRendererState() {}
	
	protected StargateAbstractRendererState(StargateAbstractRendererStateBuilder builder) {
		if (builder.stargateState.engaged()) {
			doEventHorizonRender = true;
			vortexState = EnumVortexState.STILL;
		}
	}
	
	public StargateAbstractRendererState initClient(BlockPos pos, EnumFacing facing) {
		this.pos = pos;
		this.facing = facing;
		
		if (facing.getAxis() == EnumFacing.Axis.X)
			facing = facing.getOpposite();
		
		this.horizontalRotation = facing.getHorizontalAngle();
		
		return this;
	}
	
	// Global
	// Not saved
	public BlockPos pos;
	public EnumFacing facing;
	public float horizontalRotation;
	
	// Gate
	// Saved
	public boolean doEventHorizonRender = false;
	public EnumVortexState vortexState = EnumVortexState.FORMING;
	
	// Event horizon
	// Not saved
	public QuadStrip backStrip;
	public boolean backStripClamp;
	public Float whiteOverlayAlpha;
	public long gateWaitStart = 0;
	public long gateWaitClose = 0;
	public boolean zeroAlphaSet;	
	public boolean horizonUnstable = false;
	public int horizonSegments = 0;
	
	public void openGate(long totalWorldTime) {		
		gateWaitStart = totalWorldTime;
		
		zeroAlphaSet = false;
		backStripClamp = true;
		whiteOverlayAlpha = 1.0f;
		
		vortexState = EnumVortexState.FORMING;
		doEventHorizonRender = true;
	}
	
	public void closeGate(long totalWorldTime) {		
		gateWaitClose = totalWorldTime;
		vortexState = EnumVortexState.CLOSING;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(doEventHorizonRender);
		buf.writeInt(vortexState.index);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {		
		doEventHorizonRender = buf.readBoolean();
		vortexState = EnumVortexState.valueOf( buf.readInt() );
	}
	
	
	// ------------------------------------------------------------------------
	// Builder
	
	public static StargateAbstractRendererStateBuilder builder() {
		return new StargateAbstractRendererStateBuilder();
	}
	
	public static class StargateAbstractRendererStateBuilder {
		
		// Gate
		protected EnumStargateState stargateState;
		
		public StargateAbstractRendererStateBuilder setStargateState(EnumStargateState stargateState) {
			this.stargateState = stargateState;
			return this;
		}
		
		public StargateAbstractRendererState build() {
			return new StargateAbstractRendererState(this);
		}
	}
}
