package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.AunisProps;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateMilkyWayMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateMilkyWayRendererState extends StargateRendererStateBase {

	// Chevrons
	
	@Override
	public void setActiveChevrons(World world, BlockPos gatePos, int activeChevrons) {
		super.setActiveChevrons(world, gatePos, activeChevrons);
				
		if (AunisConfig.debugConfig.checkGateMerge) {
			int index = 0;
			IBlockState state = world.getBlockState(gatePos);
			
			for (BlockPos chevPos : StargateMilkyWayMergeHelper.INSTANCE.getChevronBlocks()) {
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
			
			BlockPos chevPos = StargateMilkyWayMergeHelper.INSTANCE.getChevronBlocks().get(8);
			StargateMilkyWayMemberTile memberTile = (StargateMilkyWayMemberTile) world.getTileEntity(chevPos.rotate(FacingToRotation.get(state.getValue(AunisProps.FACING_HORIZONTAL))).add(gatePos));
				
			if (memberTile != null)
				memberTile.setLitUp(isFinalActive);
		}
	}

	// Ring		
	public EnumSymbol ringCurrentSymbol = EnumSymbol.ORIGIN;
	public StargateSpinState spinState = new StargateSpinState();
	
	// Stargate size
	public StargateSizeEnum stargateSize = AunisConfig.stargateSize;
	
//	@Override
//	public String toString() {
//		return String.format(pos+": activeChevrons: %d, isFinalActive: %b, doEventHorizonRender: %b, vortexState: %s, openingSoundPlayed: %b", activeChevrons, isFinalActive,
//				doEventHorizonRender, vortexState.toString(), openingSoundPlayed);
//	}
	
	@Override
	public void toBytes(ByteBuf buf) {		
		buf.writeInt(ringCurrentSymbol != null ? ringCurrentSymbol.id : EnumSymbol.ORIGIN.id);
		spinState.toBytes(buf);
		buf.writeInt(stargateSize.id);
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {		
				
		ringCurrentSymbol = EnumSymbol.valueOf(buf.readInt());
		
		if (spinState == null)
			spinState = new StargateSpinState();
		
		spinState.fromBytes(buf);
		stargateSize = StargateSizeEnum.fromId(buf.readInt());
				
		super.fromBytes(buf);
	}
}