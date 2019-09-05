package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.renderer.stargate.StargateRenderer.EnumVortexState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.MergeHelper;
import mrjake.aunis.tileentity.StargateMemberTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateRendererState extends RendererState {
	
	// Chevrons
	private int activeChevrons = 0;
	private boolean isFinalActive = false;
	
	public void setActiveChevrons(World world, BlockPos gatePos, int activeChevrons) {
		this.activeChevrons = activeChevrons;
				
		if (AunisConfig.debugConfig.checkGateMerge) {
			int index = 0;
			IBlockState state = world.getBlockState(gatePos);
			
			for (BlockPos chevPos : MergeHelper.getWithoutLastChevronBlock()) {
				StargateMemberTile memberTile = (StargateMemberTile) world.getTileEntity(MergeHelper.rotateAndGlobal(chevPos, state.getValue(AunisProps.FACING_HORIZONTAL), gatePos));
				
				if (memberTile != null)
					memberTile.setLitUp(activeChevrons > index);
				
				index++;
			}	
		}
	}
	
	public int getActiveChevrons() {
		return activeChevrons;
	}
	
	public void setFinalActive(World world, BlockPos gatePos, boolean isFinalActive) {
		this.isFinalActive = isFinalActive;

		if (AunisConfig.debugConfig.checkGateMerge) {
			IBlockState state = world.getBlockState(gatePos);
			
			BlockPos chevPos = MergeHelper.getLastChevronBlock();
			StargateMemberTile memberTile = (StargateMemberTile) world.getTileEntity(MergeHelper.rotateAndGlobal(chevPos, state.getValue(AunisProps.FACING_HORIZONTAL), gatePos));
				
			memberTile.setLitUp(isFinalActive);
		}
	}
	
	public boolean isFinalActive() {
		return isFinalActive;
	}

	// Ring		
	public EnumSymbol ringCurrentSymbol = EnumSymbol.ORIGIN;
	public StargateSpinState spinState = new StargateSpinState();
	
	// Gate
	public boolean doEventHorizonRender = false;
	public EnumVortexState vortexState = EnumVortexState.FORMING;
//	public boolean openingSoundPlayed = false;
	public boolean dialingComplete = false;
	
//	@Override
//	public String toString() {
//		return String.format(pos+": activeChevrons: %d, isFinalActive: %b, doEventHorizonRender: %b, vortexState: %s, openingSoundPlayed: %b", activeChevrons, isFinalActive,
//				doEventHorizonRender, vortexState.toString(), openingSoundPlayed);
//	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(activeChevrons);
		buf.writeBoolean(isFinalActive);
		
		buf.writeInt(ringCurrentSymbol != null ? ringCurrentSymbol.id : EnumSymbol.ORIGIN.id);
		spinState.toBytes(buf);
		
		buf.writeBoolean(doEventHorizonRender);
		buf.writeInt(vortexState.index);
		buf.writeBoolean(dialingComplete);
	}
	
	@Override
	public RendererState fromBytes(ByteBuf buf) {		
		activeChevrons = buf.readInt();
		isFinalActive = buf.readBoolean();
				
		ringCurrentSymbol = EnumSymbol.valueOf(buf.readInt());
		
		if (spinState == null)
			spinState = new StargateSpinState();
		
		spinState.fromBytes(buf);
		
		doEventHorizonRender = buf.readBoolean();
		vortexState = EnumVortexState.valueOf( buf.readInt() );
		dialingComplete = buf.readBoolean();
		
		return this;
	}
}