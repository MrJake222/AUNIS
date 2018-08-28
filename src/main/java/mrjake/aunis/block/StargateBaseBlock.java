package mrjake.aunis.block;

import mrjake.aunis.gui.StargateGUI;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.merge.MergeHelper;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.TileEntityTESRMember;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateBaseBlock extends TileEntityTESRMember<StargateBaseTile> {

	public StargateBaseBlock() {
		super(Material.IRON, SoundType.METAL, "stargatebase_block");
	}
	
	@Override
	public Class<StargateBaseTile> getTileEntityClass() {
		return StargateBaseTile.class;
	}

	@Override
	public StargateBaseTile createTileEntity(World world, IBlockState state) {
		return new StargateBaseTile();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {		
		StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
		
		StargateNetwork.get(world).removeStargate(gateTile.gateAddress);
		MergeHelper.updateChevRingMergeState(gateTile, state, false);
		
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		// Client side
		if (world.isRemote) {
			StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
			
			// Open GUI
			Minecraft.getMinecraft().displayGuiScreen( new StargateGUI(gateTile) );
		}
		
		return true;
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
