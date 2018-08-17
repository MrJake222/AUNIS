package mrjake.aunis.block;

import mrjake.aunis.Aunis;
import mrjake.aunis.gui.StargateGUI;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.TileEntityFaced;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateBaseBlock extends TileEntityFaced<StargateBaseTile> {

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
	
	/*@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
				
		super.breakBlock(world, pos, state);
	}*/
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		// Client side
		if (world.isRemote) {
			StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
			
			// Open GUI
			Minecraft.getMinecraft().displayGuiScreen( new StargateGUI(gateTile) );
		}
		
		else {
			StargateBaseTile te = (StargateBaseTile) world.getTileEntity(pos);
			Aunis.info("Address: " +  te.gateAddress);
		}
		
		return true;
	}
	
	/*@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }*/
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
}
