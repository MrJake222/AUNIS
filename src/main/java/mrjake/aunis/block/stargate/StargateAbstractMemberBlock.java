package mrjake.aunis.block.stargate;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateAbstractMemberTile;
import mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class StargateAbstractMemberBlock extends Block {

    public StargateAbstractMemberBlock(String blockName) {
        super(Material.IRON);

        setRegistryName(Aunis.ModID + ":" + blockName);
        setUnlocalizedName(Aunis.ModID + "." + blockName);

        setSoundType(SoundType.METAL);
        setCreativeTab(Aunis.aunisCreativeTab);

        setHardness(3.0f);
        setHarvestLevel("pickaxe", 3);
    }

    protected abstract StargateAbstractMergeHelper getMergeHelper();


    // --------------------------------------------------------------------------------------
    // Interactions

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        StargateAbstractMemberTile memberTile = (StargateAbstractMemberTile) world.getTileEntity(pos);
        StargateAbstractBaseTile gateTile = memberTile.getBaseTile(world);
        
        if (gateTile != null) {
            gateTile.updateMergeState(false, world.getBlockState(gateTile.getPos()).getValue(AunisProps.FACING_HORIZONTAL));
        }
    }
    
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }

    // --------------------------------------------------------------------------------------
    // TileEntity


    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public abstract TileEntity createTileEntity(World world, IBlockState state);


    // --------------------------------------------------------------------------------------
    // Rendering

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        if (state.getValue(AunisProps.RENDER_BLOCK))
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
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing facing) {
    	if (state.getValue(AunisProps.RENDER_BLOCK)) {
    		// Rendering some block
    		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos);
			if (memberTile != null && memberTile.getCamoState() != null) {
				return memberTile.getCamoState().getBlockFaceShape(world, pos, facing);
			}
    	}
        
    	return BlockFaceShape.UNDEFINED;
    }
}
