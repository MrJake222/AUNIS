package mrjake.aunis.block;

import mrjake.aunis.Aunis;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.dhd.OpenStargateAddressGuiToClient;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.merge.MergeHelper;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.upgrade.UpgradeHelper;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public class StargateBaseBlock extends BlockTESRMember {

	public StargateBaseBlock() {
		super(Material.IRON, SoundType.METAL, "stargatebase_block");
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {		
		StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
		
		StargateNetwork.get(world).removeStargate(gateTile.gateAddress);
		MergeHelper.updateChevRingMergeState(gateTile, state, false);
				
		if (!world.isRemote) {
//			if (gateTile.hasUpgrade() || gateTile.getInsertAnimation()) {
//				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(AunisItems.crystalGlyphStargate));
//			}
			
			// Supports upgrades
			if (gateTile instanceof ITileEntityUpgradeable) {			
				if (gateTile.hasUpgrade() || gateTile.getUpgradeRendererState().doInsertAnimation) {
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(AunisItems.crystalGlyphStargate));
				}
			}
		}
		
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
		ItemStack heldItem = player.getHeldItem(hand);
		
		// Server side
		if (!world.isRemote) {			
			if (heldItem.getItem() == AunisItems.analyzerAncient) {
				AunisPacketHandler.INSTANCE.sendTo(new OpenStargateAddressGuiToClient(pos, gateTile.hasUpgrade() ? 7 : 6), (EntityPlayerMP) player);
				
				EnergyStorage energyStorage = (EnergyStorage) gateTile.getCapability(CapabilityEnergy.ENERGY, null);

				// TODO: Move this to GUI
				Aunis.info("Stargate energy: " + energyStorage.getEnergyStored() + " / " + energyStorage.getMaxEnergyStored());
				
				return true;
			}
			
			else if (heldItem.getItem() == AunisItems.dialerFast) {				
				NBTTagCompound compound = heldItem.getTagCompound();
				if (compound == null) 
					compound = new NBTTagCompound();
				
				byte[] symbols = new byte[gateTile.gateAddress.size()];
				
				for (int i=0; i<gateTile.gateAddress.size(); i++)
					symbols[i] = (byte) gateTile.gateAddress.get(i).id;
				
				compound.setByteArray("address", symbols);
				
				heldItem.setTagCompound(compound);
				
				return true;
			}
			
			else if (!state.getValue(BlockTESRMember.RENDER) && hand == EnumHand.MAIN_HAND) {
				return UpgradeHelper.upgradeInteract((EntityPlayerMP) player, gateTile, heldItem);
			}
			
			return false;
		}
		
		// Client side
		else {
			// Aunis.info("horizontalRotation: " + gateTile.getRenderer().getHorizontalRotation());
			
			return  heldItem.getItem() == AunisItems.analyzerAncient ||
					heldItem.getItem() == AunisItems.dialerFast || 
					heldItem.getItem() == AunisItems.crystalGlyphStargate || 
					heldItem.getItem() == Items.AIR;
		}
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public StargateBaseTile createTileEntity(World world, IBlockState state) {
		return new StargateBaseTile();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		// Client side
		
		if ( state.getValue(BlockTESRMember.RENDER) )
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
		return state.getValue(BlockTESRMember.RENDER);
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return state.getValue(BlockTESRMember.RENDER);
	}
}
