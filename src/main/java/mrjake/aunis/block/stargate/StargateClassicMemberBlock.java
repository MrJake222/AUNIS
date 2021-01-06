package mrjake.aunis.block.stargate;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.gui.GuiIdEnum;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.stargate.CamoPropertiesHelper;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public abstract class StargateClassicMemberBlock extends StargateAbstractMemberBlock {
	
	public StargateClassicMemberBlock(String blockName) {
		super(blockName);

		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH)
				.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING)
				.withProperty(AunisProps.RENDER_BLOCK, true));
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {	
		// Optifine shit
		if (world.getBlockState(pos).getBlock() != this)
			return 0;
		
		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos);
				
		if (memberTile != null)
			return memberTile.isLitUp(state) ? 7 : 0;
		
		else
			return 0;
	}
	
	// ------------------------------------------------------------------------
	@Override
	public void getSubBlocks(CreativeTabs creativeTabs, NonNullList<ItemStack> items) {
		for (EnumMemberVariant variant : EnumMemberVariant.values()) {
			items.add(new ItemStack(this, 1, getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, variant))));
		}
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		EnumMemberVariant variant = state.getValue(AunisProps.MEMBER_VARIANT);
//		Aunis.info("state: " + state + ", meta:"+getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, variant)));
		
		return new ItemStack(this, 1, getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, variant)));
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		EnumMemberVariant variant = state.getValue(AunisProps.MEMBER_VARIANT);
		
		drops.add(new ItemStack(this, 1, getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, variant))));
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
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos);

		if (memberTile != null) {
			IBlockState doubleSlabState = memberTile.getCamoState();
			
			if (doubleSlabState != null) {
				return ((IExtendedBlockState) state).withProperty(AunisProps.CAMO_BLOCKSTATE, doubleSlabState);
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
			StargateClassicMemberBlockBakedModel memberBlockBakedModel = new StargateClassicMemberBlockBakedModel(this, defaultModel);
			
			registry.putObject(modelResourceLocation, memberBlockBakedModel);
		}
	}
	
	

	// ------------------------------------------------------------------------	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItemStack = player.getHeldItem(EnumHand.MAIN_HAND);
		Item heldItem = heldItemStack.getItem();
		Block heldBlock = Block.getBlockFromItem(heldItemStack.getItem());
		
		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos);
//		StargateMilkyWayBaseTile gateTile = StargateMilkyWayMergeHelper.findBaseTile(world, pos, state.getValue(AunisProps.FACING_HORIZONTAL));
		
		if (!world.isRemote) {	
//			BlockPos vec = pos.subtract(gateTile.getPos());
//			Aunis.info("new BlockPos(" + vec.getX() + ", " + vec.getY() + ", " + vec.getZ() + "),");

			IBlockState camoBlockState = memberTile.getCamoState();
			
			if (heldItem == Item.getItemFromBlock(AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK) ||
				heldItem == Item.getItemFromBlock(AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK) ||
				!memberTile.isMerged())
				
				return false;
			
			if (!(heldItem instanceof ItemBlock) && camoBlockState == null) {
				BlockPos basePos = memberTile.getBasePos();
				player.openGui(Aunis.instance, GuiIdEnum.GUI_STARGATE.id, world, basePos.getX(), basePos.getY(), basePos.getZ());
				
				return true;
			}
			
			if (camoBlockState != null) {
				Block camoBlock = camoBlockState.getBlock();
				
				if (camoBlock.getMetaFromState(camoBlockState) == heldItemStack.getMetadata()) {
					if (camoBlock instanceof BlockSlab && heldBlock instanceof BlockSlab) {
						if (((BlockSlab) camoBlock).isDouble()) {
							return false;
						}
					}
					
					else {
						if (camoBlock == heldBlock) {
							return false;
						}
					}
				}
			}
			
			if (camoBlockState != null && !(camoBlockState.getBlock() instanceof BlockSlab && heldBlock instanceof BlockSlab && !((BlockSlab) camoBlockState.getBlock()).isDouble())) {
				Block camoBlock = camoBlockState.getBlock();
				int quantity = 1;
				int meta;
				
				if (camoBlock instanceof BlockSlab) {
					BlockSlab blockSlab = (BlockSlab) camoBlock;
					meta = blockSlab.getMetaFromState(camoBlockState);
					
					if (blockSlab.isDouble()) {
						 quantity = 2;
						
						if (blockSlab == Blocks.DOUBLE_STONE_SLAB)
							camoBlock = Blocks.STONE_SLAB;
						
						else if (blockSlab == Blocks.DOUBLE_STONE_SLAB2)
							camoBlock = Blocks.STONE_SLAB2;
						
						else if (blockSlab == Blocks.DOUBLE_WOODEN_SLAB)
							camoBlock = Blocks.WOODEN_SLAB;
						
						else if (blockSlab == Blocks.PURPUR_DOUBLE_SLAB)
							camoBlock = Blocks.PURPUR_SLAB;
					}
				}
				
				else {
					meta = camoBlock.getMetaFromState(camoBlockState);
				}
				
				if (!player.capabilities.isCreativeMode) {
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(camoBlock, quantity, meta));
				}
				
				SoundType soundtype = camoBlock.getSoundType(camoBlock.getDefaultState(), world, pos, player);
				world.playSound(null, pos, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				
				memberTile.setCamoState(null);
				camoBlockState = null;
			}
			
			if (heldItem instanceof ItemBlock) {	
				Block block = null;
				int meta;
				
				if (camoBlockState != null && camoBlockState.getBlock() == heldBlock && camoBlockState.getBlock().getMetaFromState(camoBlockState) == heldItemStack.getMetadata()) {						
					BlockSlab blockSlab = (BlockSlab) camoBlockState.getBlock();
					meta = blockSlab.getMetaFromState(camoBlockState);
					
					if (facing != EnumFacing.UP)
						return false;
					
					if (blockSlab == Blocks.STONE_SLAB)
						block = Blocks.DOUBLE_STONE_SLAB;
					
					else if (blockSlab == Blocks.STONE_SLAB2)
						block = Blocks.DOUBLE_STONE_SLAB2;
					
					else if (blockSlab == Blocks.WOODEN_SLAB)
						block = Blocks.DOUBLE_WOODEN_SLAB;
					
					else if (blockSlab == Blocks.PURPUR_SLAB)
						block = Blocks.PURPUR_DOUBLE_SLAB;						
				}
				
				else {
					if (camoBlockState != null && !player.capabilities.isCreativeMode) {
						InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(camoBlockState.getBlock(), 1, camoBlockState.getBlock().getMetaFromState(camoBlockState)));
					}
					
					block = Block.getBlockFromItem(heldItemStack.getItem());
					meta = heldItemStack.getMetadata();
				}
				
				memberTile.setCamoState(block.getStateFromMeta(meta));
				
				if (!player.capabilities.isCreativeMode)
					heldItemStack.shrink(1);
				
				SoundType soundtype = block.getSoundType(block.getDefaultState(), world, pos, player);
				world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				
				world.setBlockState(pos, state.withProperty(AunisProps.RENDER_BLOCK, true), 0);
			}
			
			else {						
				world.setBlockState(pos, state.withProperty(AunisProps.RENDER_BLOCK, false), 0);
			}
			
			TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.CAMO_STATE, memberTile.getState(StateTypeEnum.CAMO_STATE)), point);
		
			return true;
		}
		
		else {			
			return 	heldItem != Item.getItemFromBlock(AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK) &&
					heldItem != Item.getItemFromBlock(AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK) &&
					heldItem != Item.getItemFromBlock(AunisBlocks.STARGATE_UNIVERSE_BASE_BLOCK) &&
					heldItem != Item.getItemFromBlock(AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK);
		}
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (!world.isRemote) {
			EnumFacing facing = placer.getHorizontalFacing().getOpposite();
			
			state = state.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.byId((stack.getMetadata() >> 3) & 0x01))
					.withProperty(AunisProps.RENDER_BLOCK, true)
					.withProperty(AunisProps.FACING_HORIZONTAL, facing);
		
			world.setBlockState(pos, state); 
			
			StargateAbstractBaseTile gateTile = getMergeHelper().findBaseTile(world, pos, facing);
							
			if (gateTile != null && !gateTile.isMerged())
				gateTile.updateMergeState(gateTile.getMergeHelper().checkBlocks(world, gateTile.getPos(), world.getBlockState(gateTile.getPos()).getValue(AunisProps.FACING_HORIZONTAL)), facing);
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);

		if(!world.isRemote) {
			StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos);
			if (memberTile.getCamoItemStack() != null)
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), memberTile.getCamoItemStack());
		}
	}
	
	// ------------------------------------------------------------------------

	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return CamoPropertiesHelper.getLightOpacity(state, world, pos);
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.SOLID;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return CamoPropertiesHelper.getStargateBlockBoundingBox(state, access, pos, false);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return CamoPropertiesHelper.getStargateBlockBoundingBox(state, access, pos, true);
	}
}
