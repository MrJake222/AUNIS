package mrjake.aunis.block;

import mrjake.aunis.Aunis;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.tileentity.TRControllerTile;
import mrjake.aunis.tileentity.TransportRingsTile;
import mrjake.aunis.transportrings.TransportRings;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TransportRingsBlock extends Block {

private static final String blockName = "transportrings_block";
	
	public TransportRingsBlock() {	
		super(Material.IRON);
		
		setRegistryName(Aunis.ModID + ":" + blockName);
		setUnlocalizedName(Aunis.ModID + "." + blockName);
		
		setSoundType(SoundType.STONE); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setLightOpacity(0);
		
		setHardness(3.0f);
		setHarvestLevel("pickaxe", 3);
	}
	
	// ------------------------------------------------------------------------
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {			
			if (player.getHeldItem(hand).getItem() == AunisItems.analyzerAncient)
				AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, EnumStateType.GUI_STATE, ringsTile.getState(EnumStateType.GUI_STATE)), (EntityPlayerMP) player);
			
			else {
				Aunis.info(pos+": linked rings: ");
								
				for (TransportRings rings : ringsTile.ringsMap.values()) {
					Aunis.info(rings.toString());
				}
			}	
		}
		
		else {
			ringsTile.getTransportRingsRenderer().which++;
		}
		
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		
//		TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {
			for (BlockPos controller : BlockPos.getAllInBoxMutable(pos.add(-10, -5, -10), pos.add(10, 5, 10))) {
				if (world.getBlockState(controller).getBlock() == AunisBlocks.trControllerBlock) {
					
					TRControllerTile controllerTile = (TRControllerTile) world.getTileEntity(controller);
					controllerTile.setLinkedRings(pos);
					
					break;
				}
			}
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(pos);

		ringsTile.removeAllRings();
	}
	
	// ------------------------------------------------------------------------
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TransportRingsTile createTileEntity(World world, IBlockState state) {
		return new TransportRingsTile();
	}
}
