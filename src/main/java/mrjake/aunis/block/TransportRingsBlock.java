package mrjake.aunis.block;

import mrjake.aunis.Aunis;
import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TransportRingsBlock extends Block {

private static final String blockName = "transport_rings_block";
	
	public TransportRingsBlock() {	
		super(Material.IRON);
		
		setRegistryName(Aunis.ModID + ":" + blockName);
		setUnlocalizedName(Aunis.ModID + "." + blockName);
		
		setSoundType(SoundType.STONE); 
		setCreativeTab(Aunis.aunisCreativeTab);
	}
	
	// ------------------------------------------------------------------------
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {			
//			ringsTile.animationStart();
//			ringsTile.listAllRings();
			TransportRingsTile closestTile = ringsTile.getClosest();
			
			if (closestTile != null) {
				ringsTile.startAnimationAndTeleport();
				closestTile.startAnimationAndTeleport();
			}
			
			return true;
		}
		
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		
		TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(pos);
		
		if (!world.isRemote) {
			int x = pos.getX();
			int z = pos.getZ();
			
			for (BlockPos newRings : BlockPos.getAllInBoxMutable(new BlockPos(x-25, 0, z-25), new BlockPos(x+25, 255, z+25))) {
				if (world.getBlockState(newRings).getBlock() == AunisBlocks.transportRingsBlock && !pos.equals(newRings)) {
					TransportRingsTile newRingsTile = (TransportRingsTile) world.getTileEntity(newRings);
					int address = newRingsTile.getAddress();
					
					ringsTile.addRings(newRingsTile);
					newRingsTile.addRings(ringsTile);
					
					Aunis.info(newRings + ": Found rings[address="+address+"]");
				}
			}
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(pos);

		ringsTile.unlinkAllRings();
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
