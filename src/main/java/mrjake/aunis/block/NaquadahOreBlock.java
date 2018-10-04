package mrjake.aunis.block;

import java.util.Random;

import mrjake.aunis.item.AunisItems;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

public class NaquadahOreBlock extends BlockBase {
	
	public NaquadahOreBlock() {
		super(Material.ROCK, SoundType.STONE, "naquadah_ore");
		
		setHardness(1.5f);
		setHarvestLevel("pickaxe", 3);
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 3 + random.nextInt(2);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return AunisItems.naquadahOreShard;
	}
}
