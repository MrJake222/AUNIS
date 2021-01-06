package mrjake.aunis.block.stargate;

import mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class StargateClassicMemberBlockColor implements IBlockColor {

	@Override
	public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
		TileEntity tile = world.getTileEntity(pos);
		
		if (tile instanceof StargateClassicMemberTile) {
			StargateClassicMemberTile memberTile = (StargateClassicMemberTile) tile;
			IBlockState camoState = memberTile.getCamoState();
			
			if (camoState != null) {
				return Minecraft.getMinecraft().getBlockColors().colorMultiplier(camoState, world, pos, tintIndex);
			}
		}
		
		return 0;
	}

}
