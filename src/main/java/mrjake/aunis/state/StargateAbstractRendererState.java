package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.stargate.StargateAbstractRenderer.EnumVortexState;
import mrjake.aunis.renderer.stargate.StargateRendererStatic.QuadStrip;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisPositionedSound;
import mrjake.aunis.stargate.EnumStargateState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class StargateAbstractRendererState extends State {
	public StargateAbstractRendererState() {}
	
	public StargateAbstractRendererState(EnumStargateState stargateState) {
		if (stargateState.engaged()) {
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
		
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.WORMHOLE, pos, false);
	}
	
	public void engageGate() {
		vortexState = EnumVortexState.STILL;
		AunisSoundHelper.playPositionedSoundClientSide(EnumAunisPositionedSound.WORMHOLE, pos, true);
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
}
