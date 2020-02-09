package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateMilkyWayMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateRendererStateSG1 extends StargateRendererStateBase {
	
	// Chevrons
	
	@Override
	public void setActiveChevrons(World world, BlockPos gatePos, int activeChevrons) {
		super.setActiveChevrons(world, gatePos, activeChevrons);
				
		if (AunisConfig.debugConfig.checkGateMerge) {
			int index = 0;
			IBlockState state = world.getBlockState(gatePos);
			
			for (BlockPos chevPos : StargateMilkyWayMergeHelper.CHEVRON_BLOCKS) {
				StargateMilkyWayMemberTile memberTile = (StargateMilkyWayMemberTile) world.getTileEntity(chevPos.rotate(FacingToRotation.get(state.getValue(AunisProps.FACING_HORIZONTAL))).add(gatePos));
				
				if (memberTile != null)
					memberTile.setLitUp(activeChevrons > index);
				
				index++;
			}	
		}
	}
	
	@Override
	public void setFinalActive(World world, BlockPos gatePos, boolean isFinalActive) {
		super.setFinalActive(world, gatePos, isFinalActive);

		if (AunisConfig.debugConfig.checkGateMerge) {
			IBlockState state = world.getBlockState(gatePos);
			
			BlockPos chevPos = StargateMilkyWayMergeHelper.CHEVRON_BLOCKS.get(8);
			StargateMilkyWayMemberTile memberTile = (StargateMilkyWayMemberTile) world.getTileEntity(chevPos.rotate(FacingToRotation.get(state.getValue(AunisProps.FACING_HORIZONTAL))).add(gatePos));
				
			if (memberTile != null)
				memberTile.setLitUp(isFinalActive);
		}
	}

	// Ring		
	public EnumSymbol ringCurrentSymbol = EnumSymbol.ORIGIN;
	public StargateSpinState spinState = new StargateSpinState();
	
//	@Override
//	public String toString() {
//		return String.format(pos+": activeChevrons: %d, isFinalActive: %b, doEventHorizonRender: %b, vortexState: %s, openingSoundPlayed: %b", activeChevrons, isFinalActive,
//				doEventHorizonRender, vortexState.toString(), openingSoundPlayed);
//	}
	
	@Override
	public void toBytes(ByteBuf buf) {		
		buf.writeInt(ringCurrentSymbol != null ? ringCurrentSymbol.id : EnumSymbol.ORIGIN.id);
		spinState.toBytes(buf);
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {		
				
		ringCurrentSymbol = EnumSymbol.valueOf(buf.readInt());
		
		if (spinState == null)
			spinState = new StargateSpinState();
		
		spinState.fromBytes(buf);
				
		super.fromBytes(buf);
	}
}