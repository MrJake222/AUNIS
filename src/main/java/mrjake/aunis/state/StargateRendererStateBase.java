package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.stargate.StargateRendererBase.EnumVortexState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateRendererStateBase extends State {
	
	public void setStargateOpen(World world, BlockPos pos, int dialedAddressSize, boolean isInitiating) {
		doEventHorizonRender = true;
		vortexState = EnumVortexState.STILL;
		dialingComplete = true;
		
		if (isInitiating)
			setActiveChevrons(world, pos, dialedAddressSize);
		else
			setActiveChevrons(world, pos, dialedAddressSize);
		
		setFinalActive(world, pos, true);
		
		horizonSegments = 0;
	}
	
	public void setStargateClosed(World world, BlockPos pos) {
		doEventHorizonRender = false;
		vortexState = EnumVortexState.FORMING;
		dialingComplete = false;
		
		setActiveChevrons(world, pos, 0);
		setFinalActive(world, pos, false);
	}
	
	// Gate
	public boolean doEventHorizonRender = false;
	public EnumVortexState vortexState = EnumVortexState.FORMING;
	public boolean dialingComplete = false;
	
	// Chevrons
	private int activeChevrons = 0;
	private boolean isFinalActive = false;
	
	// Event horizon killing box
	public int horizonSegments = 0;
	
	public void setActiveChevrons(World world, BlockPos gatePos, int activeChevrons) {
		this.activeChevrons = activeChevrons;
	}
	
	public int getActiveChevrons() {
		return activeChevrons;
	}
	
	public void setFinalActive(World world, BlockPos gatePos, boolean isFinalActive) {
		this.isFinalActive = isFinalActive;
	}
	
	public boolean isFinalActive() {
		return isFinalActive;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(activeChevrons);
		buf.writeBoolean(isFinalActive);
		
		buf.writeBoolean(doEventHorizonRender);
		buf.writeInt(vortexState.index);
		buf.writeBoolean(dialingComplete);
		buf.writeInt(horizonSegments);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		activeChevrons = buf.readInt();
		isFinalActive = buf.readBoolean();
		
		doEventHorizonRender = buf.readBoolean();
		vortexState = EnumVortexState.valueOf( buf.readInt() );
		dialingComplete = buf.readBoolean();
		horizonSegments = buf.readInt();
	}
}
