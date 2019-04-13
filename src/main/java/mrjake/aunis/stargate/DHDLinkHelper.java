package mrjake.aunis.stargate;

import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDLinkHelper {

	public static void findAndLinkGate(DHDTile dhdTile) {
		World world = dhdTile.getWorld();
		BlockPos pos = dhdTile.getPos();
		
		for ( BlockPos sg : BlockPos.getAllInBox(pos.subtract(AunisConfig.dhdRange), pos.add(AunisConfig.dhdRange)) ) {
			IBlockState gateState = world.getBlockState(sg);
			
			if ( gateState.getBlock() instanceof StargateBaseBlock) {		
				StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(sg);					
				if ( !gateTile.isLinked() && !gateState.getValue(AunisProps.RENDER_BLOCK) ) {
					dhdTile.setLinkedGate(sg);
					gateTile.setLinkedDHD(pos);
					break;
				}
			}
		}
	}

	public static void findAndLinkDHD(StargateBaseTile gateTile) {
		World world = gateTile.getWorld();
		BlockPos pos = gateTile.getPos();
		
		for ( BlockPos dhdPos : BlockPos.getAllInBox(pos.subtract(AunisConfig.dhdRange), pos.add(AunisConfig.dhdRange)) ) {			
			if (world.getBlockState(dhdPos).getBlock() == AunisBlocks.dhdBlock) {
				DHDTile dhdTile = (DHDTile) world.getTileEntity(dhdPos);
				
				if (!dhdTile.isLinked()) {
					dhdTile.setLinkedGate(pos);
					gateTile.setLinkedDHD(dhdPos);
				}
			}
		}
	}

}
