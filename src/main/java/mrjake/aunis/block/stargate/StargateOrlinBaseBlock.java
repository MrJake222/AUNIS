package mrjake.aunis.block.stargate;

import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.stargate.merging.StargateOrlinMergeHelper;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.power.StargateEnergyRequired;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import mrjake.aunis.worldgen.StargateGeneratorNether;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class StargateOrlinBaseBlock extends StargateAbstractBaseBlock {
	
	public StargateOrlinBaseBlock() {
		super("stargate_orlin_base_block");
		setResistance(16.0f);
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
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		ItemStack stack = new ItemStack(this);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("openCount", 0);
		stack.setTagCompound(compound);
		items.add(stack);
	}
	
	// ------------------------------------------------------------------------
	// Block behavior

	@Override
	protected void showGateInfo(EntityPlayer player, EnumHand hand, World world, BlockPos pos) {
		StargateOrlinBaseTile gateTile = (StargateOrlinBaseTile) world.getTileEntity(pos);
		IEnergyStorage energyStorage = gateTile.getCapability(CapabilityEnergy.ENERGY, null);

		String energy = String.format("%,d", energyStorage.getEnergyStored());

		StargateEnergyRequired energyRequired = gateTile.getEnergyRequiredToDial();
		String required = String.format("%,d", energyRequired.energyToOpen);
		int energyStored = gateTile.getEnergyStored();
		boolean hasEnergy = (energyStored >= energyRequired.energyToOpen);

		int missing = energyRequired.energyToOpen - energyStored;
		float secondsLeft = 0;

		if (missing > 0 && gateTile.getEnergyTransferedLastTick() > 0)
			secondsLeft = missing / (float)gateTile.getEnergyTransferedLastTick() / 20;

		String left = String.format("%.2f", secondsLeft);

		player.sendMessage(new TextComponentTranslation("chat.orlins.energyStored", (hasEnergy ? TextFormatting.GREEN : TextFormatting.RED) + energy, TextFormatting.DARK_GREEN + required, TextFormatting.DARK_GREEN + left));
	}

	@Override
	protected IBlockState createMemberState(IBlockState memberState, EnumFacing facing, int meta) {
		return memberState.withProperty(AunisProps.RENDER_BLOCK, true)
				.withProperty(AunisProps.ORLIN_VARIANT, facing);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		StargateOrlinBaseTile gateTile = (StargateOrlinBaseTile) world.getTileEntity(pos);
		EnumFacing facing = placer.getHorizontalFacing().getOpposite();
		
		if (!world.isRemote) {
			state = state.withProperty(AunisProps.FACING_HORIZONTAL, facing)
					.withProperty(AunisProps.RENDER_BLOCK, true);
		
			world.setBlockState(pos, state);
			gateTile.initializeFromItemStack(stack);
			gateTile.updateFacing(facing, true);
			gateTile.updateMergeState(StargateOrlinMergeHelper.INSTANCE.checkBlocks(world, pos, facing), facing);
			
			// ------------------------------------------
			// Nether handler
			if (world.provider.getDimensionType() == DimensionType.OVERWORLD) {
				StargateNetwork network = StargateNetwork.get(world);
				
				if (!network.hasNetherGate()) {
					network.setNetherGate(StargateGeneratorNether.place(world.getMinecraftServer().getWorld(DimensionType.NETHER.getId()), new BlockPos(pos.getX()/8, 32, pos.getZ()/8)));
				}
				
				gateTile.updateNetherAddress();
				
				Aunis.logger.debug("nether address: " + network.getNetherGate());
			}
		}
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		StargateOrlinBaseTile gateTile = (StargateOrlinBaseTile) world.getTileEntity(pos);
		
		gateTile.addDrops(drops);
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
}
