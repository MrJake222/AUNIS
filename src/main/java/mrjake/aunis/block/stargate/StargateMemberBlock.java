package mrjake.aunis.block.stargate;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateAbstractMemberTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinMemberTile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class StargateMemberBlock extends Block {

    public StargateMemberBlock(String blockName) {
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
}
