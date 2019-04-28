package mrjake.aunis.block;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.stargate.BoundingHelper;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.stargate.MergeHelper;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.StargateMemberTile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class StargateMemberBlock extends Block {	
	
	
	public static final String blockName = "stargate_member_block";
	
	public StargateMemberBlock() {
		super(Material.IRON);
		
		setRegistryName(Aunis.ModID + ":" + blockName);
		setUnlocalizedName(Aunis.ModID + "." + blockName);
		
		setSoundType(SoundType.METAL); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH)
				.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING)
				.withProperty(AunisProps.RENDER_BLOCK, true));
		
		setHardness(3.0f);
		setHarvestLevel("pickaxe", 3);
		
//		setLightOpacity(0);
//		setLightLevel(1.0f);
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {	
		StargateMemberTile memberTile = (StargateMemberTile) world.getTileEntity(pos);
				
		if (memberTile != null)
			return memberTile.isLitUp(state) ? 7 : 0;
		
		else
			return 0;
	}
	
	// ------------------------------------------------------------------------
	@Override
	public void getSubBlocks(CreativeTabs creativeTabs, NonNullList<ItemStack> items) {
		for (EnumMemberVariant variant : EnumMemberVariant.values()) {
			items.add(new ItemStack(this, 1, variant.id << 3));
		}
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		int meta = 0;
		
		switch (state.getValue(AunisProps.MEMBER_VARIANT)) {
			case CHEVRON: meta = 8; break;
			case RING: meta = 0; break;
		}
		
		drops.add(new ItemStack(AunisBlocks.stargateMemberBlock, 1, meta));
	}
	
	
	// ------------------------------------------------------------------------
	@SuppressWarnings("rawtypes")
	private static final IProperty[] LISTED_PROPS = new IProperty[] { AunisProps.RENDER_BLOCK, AunisProps.FACING_HORIZONTAL, AunisProps.MEMBER_VARIANT };
	
	@SuppressWarnings("rawtypes")
	private static final IUnlistedProperty[] UNLISTED_PROPS = new IUnlistedProperty[] { AunisProps.CAMO_BLOCKSTATE };
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, LISTED_PROPS, UNLISTED_PROPS);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {		
		return 	(state.getValue(AunisProps.MEMBER_VARIANT).id << 3) |
				(state.getValue(AunisProps.RENDER_BLOCK) ? 0x04 : 0) |
				 state.getValue(AunisProps.FACING_HORIZONTAL).getHorizontalIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {		
		return getDefaultState()
				.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.byId((meta >> 3) & 0x01))
				.withProperty(AunisProps.RENDER_BLOCK, (meta & 0x04) != 0)
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.getHorizontal(meta & 0x03));
	}
	
	
	// ------------------------------------------------------------------------		
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		StargateMemberTile memberTile = (StargateMemberTile) world.getTileEntity(pos);

		if (memberTile != null) {
			ItemStack itemStack = memberTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
			
			if (!itemStack.isEmpty()) {
				ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
			
				IBlockState stateForPlacement = itemBlock.getBlock().getStateFromMeta(itemStack.getMetadata());

				return ((IExtendedBlockState) state).withProperty(AunisProps.CAMO_BLOCKSTATE, stateForPlacement);
			}
		}
		
		return state;
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerCustomModel(IRegistry<ModelResourceLocation, IBakedModel> registry) {
		
		for (IBlockState state : getBlockState().getValidStates()) {						
			String variant = "";
			
			for (IProperty prop : state.getPropertyKeys()) {
				Object value = state.getValue(prop);
				
				variant += prop.getName() + "=" + value.toString() + ",";
			}
			
			variant = variant.substring(0, variant.length()-1);
			
			ModelResourceLocation modelResourceLocation = new ModelResourceLocation(getRegistryName(), variant);
			
			IBakedModel defaultModel = registry.getObject(modelResourceLocation);
			StargateMemberBlockBakedModel memberBlockBakedModel = new StargateMemberBlockBakedModel(this, defaultModel);
			
			registry.putObject(modelResourceLocation, memberBlockBakedModel);
		}
	}
	
	

	// ------------------------------------------------------------------------	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItemStack = player.getHeldItem(hand);
		Item heldItem = heldItemStack.getItem();
		
		StargateMemberTile memberTile = (StargateMemberTile) world.getTileEntity(pos);
		StargateBaseTile gateTile = MergeHelper.findBaseTile(world, pos, state);
		ItemStackHandler handler = (ItemStackHandler) memberTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		
		ItemStack stack = handler.getStackInSlot(0);
		
		if (!world.isRemote) {
			if (heldItem == AunisItems.analyzerAncient) {
				Aunis.info("camo block: " + handler.getStackInSlot(0));
			}
			
			else {
				if (heldItem == Item.getItemFromBlock(AunisBlocks.stargateMemberBlock) ||
					heldItem == Item.getItemFromBlock(AunisBlocks.stargateBaseBlock) ||
					!gateTile.isMerged() ||
					heldItemStack.isItemEqual(stack))
						return false;
				
				if (!stack.isEmpty()) {		
					ItemStack extract = handler.extractItem(0, 1, false);

					SoundType soundtype = Block.getBlockFromItem(stack.getItem()).getSoundType(Block.getBlockFromItem(stack.getItem()).getDefaultState(), world, pos, player);
					world.playSound(null, pos, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					
					if (!player.capabilities.isCreativeMode) {
						InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), extract);
					}
				}
				
				if (heldItem instanceof ItemBlock) {
					ItemStack insert = heldItemStack.copy();
					insert.setCount(1);
					
					handler.insertItem(0, insert, false);
					
					if (!player.capabilities.isCreativeMode)
						heldItemStack.shrink(1);
					
					SoundType soundtype = Block.getBlockFromItem(heldItem).getSoundType(Block.getBlockFromItem(heldItem).getDefaultState(), world, pos, player);
					world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					
					world.setBlockState(pos, state.withProperty(AunisProps.RENDER_BLOCK, true), 0);
				}
				
				else {						
					world.setBlockState(pos, state.withProperty(AunisProps.RENDER_BLOCK, false), 0);
				}
				
				TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
				AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, EnumStateType.CAMO_STATE, memberTile.getState(EnumStateType.CAMO_STATE)), point);
			}
			
			return true;
		}
		
		else {			
			return 	heldItem != Item.getItemFromBlock(AunisBlocks.stargateMemberBlock) &&
					heldItem != Item.getItemFromBlock(AunisBlocks.stargateBaseBlock) &&
					!heldItemStack.isItemEqual(stack);
		}
		
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (!world.isRemote) {
			state = state.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.byId((stack.getMetadata() >> 3) & 0x01))
					.withProperty(AunisProps.RENDER_BLOCK, true)
					.withProperty(AunisProps.FACING_HORIZONTAL, placer.getHorizontalFacing().getOpposite());
		
			world.setBlockState(pos, state); 
			
//			StargateMemberTile memberTile = (StargateMemberTile) world.getTileEntity(pos);
			StargateBaseTile gateTile = MergeHelper.findBaseTile(world, pos, state);
							
			if (gateTile != null)
				gateTile.updateMergeState(MergeHelper.checkBlocks(world, gateTile.getPos()), null);
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			StargateMemberTile memberTile = (StargateMemberTile) world.getTileEntity(pos);
			StargateBaseTile gateTile = MergeHelper.findBaseTile(world, pos, state);
			
			if (gateTile != null)
				gateTile.updateMergeState(false, state);
			
			ItemStackHandler handler = (ItemStackHandler) memberTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(0));
		}
		
		super.breakBlock(world, pos, state);
	}
	
	// ------------------------------------------------------------------------
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateMemberTile();
	}

	@Override
	public int getLightOpacity(IBlockState state) {		
		if (state.getValue(AunisProps.RENDER_BLOCK))
			return 255;
		else
			return 0;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.SOLID;
	}
		
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
//		return EnumBlockRenderType.MODEL;
		
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
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BoundingHelper.getStargateBlockBoundingBox(state);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return BoundingHelper.getStargateBlockBoundingBox(state);
	}
}
