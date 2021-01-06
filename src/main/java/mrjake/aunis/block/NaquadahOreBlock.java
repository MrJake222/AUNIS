package mrjake.aunis.block;

import java.util.Random;

import mrjake.aunis.Aunis;
import mrjake.aunis.item.AunisItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class NaquadahOreBlock extends Block {
		
	public NaquadahOreBlock(String blockName) {
		super(Material.ROCK);
		
		setRegistryName(Aunis.ModID + ":" + blockName);
		setTranslationKey(Aunis.ModID + "." + blockName);
		
		setSoundType(SoundType.STONE); 
		setCreativeTab(Aunis.aunisCreativeTab);
		
		setHardness(4.5f);
		setHarvestLevel("pickaxe", 3);
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random random = world instanceof World ? ((World)world).rand : RANDOM;
		
		int quantity = 5 + random.nextInt(4) + (fortune * random.nextInt(3));
		
		drops.add(new ItemStack(AunisItems.NAQUADAH_SHARD, quantity));
	}
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}
