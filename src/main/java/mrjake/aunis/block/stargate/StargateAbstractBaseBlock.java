package mrjake.aunis.block.stargate;

import java.util.List;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class StargateAbstractBaseBlock extends Block {

    public StargateAbstractBaseBlock(String blockName) {
        super(Material.IRON);

        setRegistryName(Aunis.ModID + ":" + blockName);
        setUnlocalizedName(Aunis.ModID + "." + blockName);

        setSoundType(SoundType.METAL);
        setCreativeTab(Aunis.aunisCreativeTab);

        setDefaultState(blockState.getBaseState()
                .withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH)
                .withProperty(AunisProps.RENDER_BLOCK, true));

        setLightOpacity(0);
        setHardness(3.0f);
        setHarvestLevel("pickaxe", 3);
    }


    // --------------------------------------------------------------------------------------
    // Block states

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AunisProps.FACING_HORIZONTAL, AunisProps.RENDER_BLOCK);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(AunisProps.RENDER_BLOCK) ? 0x04 : 0) |
                state.getValue(AunisProps.FACING_HORIZONTAL).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(AunisProps.RENDER_BLOCK, (meta & 0x04) != 0)
                .withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.getHorizontal(meta & 0x03));
    }


    // ------------------------------------------------------------------------
    // Block behavior

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            if(!player.isSneaking() && !tryAutobuild(player, world, pos, hand)) {
                showGateInfo(player, world, pos);
            }
        }
        return !player.isSneaking();
    }

    protected abstract void showGateInfo(EntityPlayer player, World world, BlockPos pos);

    protected boolean tryAutobuild(EntityPlayer player, World world, BlockPos basePos, EnumHand hand){
        final StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) world.getTileEntity(basePos);
        final EnumFacing facing = gateTile.getFacing();
        if(!gateTile.isMerged()) {
            final StargateAbstractMergeHelper mergeHelper = gateTile.getMergeHelper();
            final ItemStack stack = player.getHeldItem(hand);
            if(stack.getItem() instanceof ItemBlock) {
                if(((ItemBlock) stack.getItem()).getBlock().equals(mergeHelper.getMemberBlock())) {
                    List<BlockPos> posList = getAbsentBlockPositions(mergeHelper, world, basePos, facing, stack.getMetadata());

                    BlockPos pos;
                    if(!posList.isEmpty() && world.getBlockState( pos = posList.get(0) ).getBlock().isReplaceable(world, pos)) {
                        world.setBlockState(pos, createMemberState(mergeHelper.getMemberBlock().getDefaultState(), facing, stack.getMetadata()));
                        if(!player.capabilities.isCreativeMode)
                            stack.shrink(1);
                        if(posList.size() == 1)
                            gateTile.updateMergeState(gateTile.getMergeHelper().checkBlocks(world, basePos, facing), facing);
                    }
                }
            }
            return true;
        }
        return false;
    }

    protected abstract List<BlockPos> getAbsentBlockPositions(StargateAbstractMergeHelper mergeHelper, World world, BlockPos basePos, EnumFacing facing, int meta);

    protected abstract IBlockState createMemberState(IBlockState state, EnumFacing facing, int meta);

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) world.getTileEntity(pos);
            gateTile.updateMergeState(false, state.getValue(AunisProps.FACING_HORIZONTAL));
            gateTile.onBlockBroken();
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
}
