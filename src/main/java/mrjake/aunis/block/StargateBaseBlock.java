package mrjake.aunis.block;

import javafx.stage.Stage;
import mrjake.aunis.gui.StargateGUI;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.upgrade.UpgradeSlotInteractToClient;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.merge.MergeHelper;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.TileEntityTESRMember;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class StargateBaseBlock extends TileEntityTESRMember<StargateBaseTile> {

	public StargateBaseBlock() {
		super(Material.IRON, SoundType.METAL, "stargatebase_block");
	}
	
	@Override
	public Class<StargateBaseTile> getTileEntityClass() {
		return StargateBaseTile.class;
	}

	@Override
	public StargateBaseTile createTileEntity(World world, IBlockState state) {
		return new StargateBaseTile();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {		
		StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
		
		StargateNetwork.get(world).removeStargate(gateTile.gateAddress);
		MergeHelper.updateChevRingMergeState(gateTile, state, false);
				
		if (!world.isRemote) {
			if (gateTile.hasUpgrade() || gateTile.getInsertAnimation()) {
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(AunisItems.stargateAddressCrystal));
			}
		}
		
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
		ItemStack heldItem = player.getHeldItemMainhand();	
		
		// Client side
		if (world.isRemote) {
			if (heldItem.getItem() == AunisItems.ancientAnalyzer) {
				Minecraft.getMinecraft().displayGuiScreen( new StargateGUI(gateTile) );
			}
		}
		
		else {
			if ( hand == EnumHand.MAIN_HAND/* && facing == EnumFacing.UP */&& !state.getValue(BlockTESRMember.RENDER) ) {												
				boolean hasUpgrade = gateTile.hasUpgrade();
				boolean isHoldingUpgrade = heldItem.getItem() == AunisItems.stargateAddressCrystal;
					
				if (!gateTile.getInsertAnimation()) {
					// Reduce ItemStack
					if (!hasUpgrade && isHoldingUpgrade)
						player.setHeldItem(hand, new ItemStack(heldItem.getItem(), heldItem.getCount()-1) );
						
					gateTile.setInsertAnimation(true);
				}
					
				AunisPacketHandler.INSTANCE.sendToAllAround( new UpgradeSlotInteractToClient(pos, hasUpgrade, isHoldingUpgrade), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64) );
			}
		}
		
		return true;
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
