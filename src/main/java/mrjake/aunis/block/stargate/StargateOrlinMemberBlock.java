package mrjake.aunis.block.stargate;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.StargateOrlinMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinMemberTile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class StargateOrlinMemberBlock extends Block {

	private static final String BLOCK_NAME = "stargate_orlin_member_block";
	
	public StargateOrlinMemberBlock() {
		super(Material.IRON);
		
		setRegistryName(Aunis.ModID + ":" + BLOCK_NAME);
		setTranslationKey(Aunis.ModID + "." + BLOCK_NAME);
		
		setSoundType(SoundType.METAL); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.RENDER_BLOCK, true)
				.withProperty(AunisProps.ORLIN_VARIANT, EnumFacing.DOWN));
		
		setLightOpacity(0);
		
		setHardness(3.0f);
		setHarvestLevel("pickaxe", 3);
	}
	
	
	// ------------------------------------------------------------------------
	// Block states
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AunisProps.RENDER_BLOCK, AunisProps.ORLIN_VARIANT);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {		
		return (state.getValue(AunisProps.RENDER_BLOCK) ? 0x08 : 0) |
				state.getValue(AunisProps.ORLIN_VARIANT).getIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {		
		return getDefaultState()
				.withProperty(AunisProps.RENDER_BLOCK, (meta & 0x08) != 0)
				.withProperty(AunisProps.ORLIN_VARIANT, EnumFacing.byIndex(meta & 0x07));
	}
	
	
	// ------------------------------------------------------------------------
	// Block behavior
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing facing = placer.getHorizontalFacing().getOpposite();
		
		if (!world.isRemote) {
			state = state.withProperty(AunisProps.RENDER_BLOCK, true)
					.withProperty(AunisProps.ORLIN_VARIANT, facing);
		
			world.setBlockState(pos, state, 0);
			
			StargateAbstractBaseTile gateTile = StargateOrlinMergeHelper.INSTANCE.findBaseTile(world, pos, facing);
			
			if (gateTile != null) {
				gateTile.updateMergeState(StargateOrlinMergeHelper.INSTANCE.checkBlocks(world, gateTile.getPos(), world.getBlockState(gateTile.getPos()).getValue(AunisProps.FACING_HORIZONTAL)), facing);
			}				
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		StargateOrlinMemberTile memberTile = (StargateOrlinMemberTile) world.getTileEntity(pos);
		StargateOrlinBaseTile gateTile = memberTile.getBaseTile(world);
		
		if (gateTile != null) {
			gateTile.updateMergeState(false, world.getBlockState(gateTile.getPos()).getValue(AunisProps.FACING_HORIZONTAL));
		}	
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		StargateOrlinMemberTile gateTile = (StargateOrlinMemberTile) world.getTileEntity(pos);
		
		Random rand = new Random();
				
		if (gateTile.isBroken()) {
			drops.add(new ItemStack(Items.IRON_INGOT, 1 + rand.nextInt(2)));
			drops.add(new ItemStack(Items.REDSTONE, 2 + rand.nextInt(3)));
		}
			
		else {
			drops.add(new ItemStack(Item.getItemFromBlock(this)));
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
	
	// ------------------------------------------------------------------------
	// Render
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return state.getValue(AunisProps.RENDER_BLOCK) ? new AxisAlignedBB(0, 0, 0, 1, 1, 1) : new AxisAlignedBB(0.4, 0, 0.25, 0.75, 1, 0.6);
	}
	
	private static final AxisAlignedBB BOTTOM	= new AxisAlignedBB(0.40, 0.00, 0.25,	0.75, 0.50, 0.60);
	private static final AxisAlignedBB TOP		= new AxisAlignedBB(0.40, 0.50, 0.25,	0.75, 1.00, 0.60);
	
	private static final AxisAlignedBB SOUTH	= new AxisAlignedBB(0.40, 0.00, 0.00,	0.75, 1.00, 0.50);
	private static final AxisAlignedBB WEST		= new AxisAlignedBB(0.50, 0.50, 0.25,	1.00, 1.00, 0.60);
	private static final AxisAlignedBB NORTH	= new AxisAlignedBB(0.40, 0.50, 0.50,	0.75, 1.00, 1.00);
	private static final AxisAlignedBB EAST		= new AxisAlignedBB(0.00, 0.50, 0.25,	0.50, 1.00, 0.60);
	
	private void add(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, AxisAlignedBB bb) {
		AxisAlignedBB bb2 = bb.offset(pos);
		
		if (entityBox.intersects(bb2))
			collidingBoxes.add(bb2);
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {

		EnumFacing facing = state.getValue(AunisProps.ORLIN_VARIANT);
		
		if (state.getValue(AunisProps.RENDER_BLOCK))
			add(pos, entityBox, collidingBoxes, new AxisAlignedBB(0, 0, 0, 1, 1, 1));
		
		else {			
			if (facing != EnumFacing.UP && facing != EnumFacing.DOWN)
				add(pos, entityBox, collidingBoxes, BOTTOM);
			
			switch (facing) {
				case UP:
					add(pos, entityBox, collidingBoxes, TOP);
					break;
			
				case EAST:
					add(pos, entityBox, collidingBoxes, EAST);
					break;
					
				case NORTH:
					add(pos, entityBox, collidingBoxes, NORTH);
					break;
					
				case SOUTH:
					add(pos, entityBox, collidingBoxes, SOUTH);
					break;
					
				case WEST:
					add(pos, entityBox, collidingBoxes, WEST);
					break;
					
				default:
					break;
			}
		}
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateOrlinMemberTile();
	}
	
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
