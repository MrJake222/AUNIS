package mrjake.aunis.block.stargate;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateOrlinMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinMemberTile;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class StargateOrlinMemberBlock extends StargateAbstractMemberBlock {
	
	public StargateOrlinMemberBlock() {
		super("stargate_orlin_member_block");

		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.RENDER_BLOCK, true)
				.withProperty(AunisProps.ORLIN_VARIANT, EnumFacing.DOWN));
		
		setLightOpacity(0);
		setResistance(16.0f);
	}

	@Override
	protected StargateAbstractMergeHelper getMergeHelper() {
		return StargateOrlinMergeHelper.INSTANCE;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			NBTTagCompound compound = stack.getTagCompound();
			
			if (compound.hasKey("openCount")) {
				tooltip.add(Aunis.proxy.localize("tile.aunis.stargate_orlin_base_block.open_count", compound.getInteger("openCount"), AunisConfig.stargateConfig.stargateOrlinMaxOpenCount));
			}
		}
	}
	
	// ------------------------------------------------------------------------
	// Block states
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		ItemStack stack = new ItemStack(this);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("openCount", 0);
		stack.setTagCompound(compound);
		items.add(stack);
	}
	
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
				.withProperty(AunisProps.ORLIN_VARIANT, EnumFacing.getFront(meta & 0x07));
	}
	
	
	// ------------------------------------------------------------------------
	// Block behavior
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing facing = placer.getHorizontalFacing().getOpposite();
		StargateOrlinMemberTile memberTile = (StargateOrlinMemberTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {
			state = state.withProperty(AunisProps.RENDER_BLOCK, true)
					.withProperty(AunisProps.ORLIN_VARIANT, facing);
		
			world.setBlockState(pos, state, 0);
			memberTile.initializeFromItemStack(stack);
			
			StargateAbstractBaseTile gateTile = getMergeHelper().findBaseTile(world, pos, facing);
			
			if (gateTile != null) {
				gateTile.updateMergeState(getMergeHelper().checkBlocks(world, gateTile.getPos(), world.getBlockState(gateTile.getPos()).getValue(AunisProps.FACING_HORIZONTAL)), facing);
			}				
		}
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		StargateOrlinMemberTile memberTile = (StargateOrlinMemberTile) world.getTileEntity(pos);
		
		memberTile.addDrops(drops);
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
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateOrlinMemberTile();
	}
}
