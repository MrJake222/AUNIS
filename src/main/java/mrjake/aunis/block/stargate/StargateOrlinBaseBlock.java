package mrjake.aunis.block.stargate;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.orlin.MergeHelperOrlin;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class StargateOrlinBaseBlock extends Block {
	
	private static final String BLOCK_NAME = "stargate_orlin_base_block";
	
	public StargateOrlinBaseBlock() {
		super(Material.IRON);
		
		setRegistryName(Aunis.ModID + ":" + BLOCK_NAME);
		setTranslationKey(Aunis.ModID + "." + BLOCK_NAME);
		
		setSoundType(SoundType.METAL); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH)
				.withProperty(AunisProps.RENDER_BLOCK, false));
		
		setLightOpacity(0);
		
		setHardness(3.0f);
		setHarvestLevel("pickaxe", 3);
	}
	
	
	// ------------------------------------------------------------------------
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
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.byHorizontalIndex(meta & 0x03));
	}
	
	
	// ------------------------------------------------------------------------
	// Block behavior
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItemStack = player.getHeldItem(hand);
		Item heldItem = heldItemStack.getItem();
		
		if (!world.isRemote) {
			StargateOrlinBaseTile gateTile = (StargateOrlinBaseTile) world.getTileEntity(pos);
		
			if (heldItem == AunisItems.pageNotebookItem) {
				NBTTagCompound compound = player.getHeldItem(hand).getTagCompound();
				if (compound != null && compound.hasKey("address")) {
									
					List<EnumSymbol> address = EnumSymbol.toSymbolList(EnumSymbol.fromLong(compound.getLong("address")));
					
					if (compound.hasKey("7th"))
						address.add(EnumSymbol.valueOf(compound.getInteger("7th")));
					
					address.add(EnumSymbol.ORIGIN);
					gateTile.dialedAddress = address;
					
					player.sendMessage(new TextComponentString("Bound to: " + address));
					
					return true;
				}
			}
			
			else if (heldItem == AunisItems.analyzerAncient) {
				AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_STATE, gateTile.getState(StateTypeEnum.GUI_STATE)), (EntityPlayerMP) player);
				
				return true;
			}
		}
				
		return  heldItem == AunisItems.pageNotebookItem ||
				heldItem == AunisItems.analyzerAncient;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		StargateOrlinBaseTile gateTile = (StargateOrlinBaseTile) world.getTileEntity(pos);
		EnumFacing facing = placer.getHorizontalFacing().getOpposite();
		
		if (!world.isRemote) {
			state = state.withProperty(AunisProps.FACING_HORIZONTAL, facing)
					.withProperty(AunisProps.RENDER_BLOCK, true);
		
			world.setBlockState(pos, state);
			gateTile.updateFacing(facing);
			
			MergeHelperOrlin.updateMergeState(world, pos, state, MergeHelperOrlin.checkBlocks(world, pos));
		}
		
		else {
			gateTile.updateFacing(facing);
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			StargateOrlinBaseTile gateTile = (StargateOrlinBaseTile) world.getTileEntity(pos);
			
			MergeHelperOrlin.updateMergeState(world, pos, state, false);			
			StargateNetwork.get(world).removeStargate(gateTile.gateAddress);
		}
	}
	
	
	// ------------------------------------------------------------------------
	// Redstone
		
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		// Server
		
		StargateOrlinBaseTile gateTile = (StargateOrlinBaseTile) world.getTileEntity(pos);
		gateTile.redstonePowerUpdate(world.isBlockPowered(pos));
	}
	
	// ------------------------------------------------------------------------
	// TileEntity
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateOrlinBaseTile();
	}
	
	
	// ------------------------------------------------------------------------
	// Render
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return state.getValue(AunisProps.RENDER_BLOCK) ? new AxisAlignedBB(0, 0, 0, 1, 1, 1) : new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return state.getValue(AunisProps.RENDER_BLOCK) ? new AxisAlignedBB(0, 0, 0, 1, 1, 1) : new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
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
