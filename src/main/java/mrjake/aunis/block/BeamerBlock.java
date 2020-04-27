package mrjake.aunis.block;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.beamer.BeamerLinkingHelper;
import mrjake.aunis.gui.GuiIdEnum;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.tileentity.BeamerTile;
import mrjake.aunis.util.ItemHandlerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BeamerBlock extends Block {
	
	public static final String BLOCK_NAME = "beamer_block";
	
	public BeamerBlock() {		
		super(Material.IRON);
		
		setRegistryName(Aunis.ModID + ":" + BLOCK_NAME);
		setTranslationKey(Aunis.ModID + "." + BLOCK_NAME);
		
		setSoundType(SoundType.METAL); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH));
		
		setHardness(3.0f);
		setHarvestLevel("pickaxe", 3);
	}
	
	// ------------------------------------------------------------------------
	// Block states
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AunisProps.FACING_HORIZONTAL, AunisProps.BEAMER_MODE);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {		
		return state.getValue(AunisProps.FACING_HORIZONTAL).getHorizontalIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {		
		return getDefaultState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.byHorizontalIndex(meta & 0x03));
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		BeamerTile beamerTile = (BeamerTile) world.getTileEntity(pos);
		return state.withProperty(AunisProps.BEAMER_MODE, beamerTile.getMode());
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		BeamerTile beamerTile = (BeamerTile) world.getTileEntity(pos);
		return beamerTile.getComparatorOutput();
	}
	
	// ------------------------------------------------------------------------
	// Block actions

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if (!player.isSneaking()) {
			ItemStack heldStack = player.getHeldItem(hand);
			Item heldItem = heldStack.getItem();
			
			BeamerTile beamerTile = (BeamerTile) world.getTileEntity(pos);
			ItemStackHandler itemStackHandler = (ItemStackHandler) beamerTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			ItemStack currentStack = itemStackHandler.getStackInSlot(0);
			
			if ((heldItem == AunisItems.BEAMER_CRYSTAL_POWER || heldItem == AunisItems.BEAMER_CRYSTAL_FLUID || heldItem == AunisItems.BEAMER_CRYSTAL_ITEMS) && currentStack.getItem() != heldStack.getItem()) {
				
				ItemStack copy = heldStack.copy();
				copy.setCount(1);
				itemStackHandler.setStackInSlot(0, copy);
				heldStack.shrink(1);
				
				if (!currentStack.isEmpty()) {
					player.addItemStackToInventory(currentStack);
				}
			}
			
			else if (!FluidUtil.interactWithFluidHandler(player, hand, world, pos, null)) {
				player.openGui(Aunis.instance, GuiIdEnum.GUI_BEAMER.id, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}
				
		return !player.isSneaking();
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing facing = placer.getHorizontalFacing().getOpposite();
		state = state.withProperty(AunisProps.FACING_HORIZONTAL, facing);
		world.setBlockState(pos, state);
		
		if (!world.isRemote) {
			BeamerLinkingHelper.findGateInFrontAndLink(world, pos, facing);
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		BeamerTile beamerTile = (BeamerTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {
			ItemHandlerHelper.dropInventoryItems(world, pos, beamerTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
			
			if (beamerTile.isLinked())
				beamerTile.getLinkedGateTile().removeLinkedBeamer(pos);
			
			beamerTile.clearTargetBeamerPos();
			
			AunisSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.BEAMER_LOOP, false);
		}
		
		super.breakBlock(world, pos, state);
	}
	

	// ------------------------------------------------------------------------
	// Tile Entity
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new BeamerTile();
	}
	
	
	// ------------------------------------------------------------------------
	// Rendering
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}
