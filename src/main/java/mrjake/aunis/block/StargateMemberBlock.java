package mrjake.aunis.block;

import mrjake.aunis.stargate.merge.MergeHelper;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateMemberBlock extends BlockTESRMember {	
	
	public StargateMemberBlock(String name) {
		super(Material.IRON, SoundType.METAL, name);
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			MergeHelper.updateBaseMergeState(worldIn, pos);
		}		
		
		super.onBlockAdded(worldIn, pos, state);
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			MergeHelper.unmergeBase(worldIn, pos);
		}
		
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		// Client side
		
		if ( state.getValue(BlockTESRMember.RENDER) )
			return EnumBlockRenderType.MODEL;
		else
			return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return state.getValue(BlockTESRMember.RENDER);
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
