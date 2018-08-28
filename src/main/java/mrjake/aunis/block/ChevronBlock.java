package mrjake.aunis.block;

import net.minecraft.block.state.IBlockState;

public class ChevronBlock extends StargateMemberBlock {
	
	public ChevronBlock() {
		super("chevron_block");
	}
	
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return state.getValue(BlockTESRMember.RENDER);
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return state.getValue(BlockTESRMember.RENDER);
	}
	
}
