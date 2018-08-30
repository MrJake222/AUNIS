package mrjake.aunis.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class AunisBlocks {
	public static StargateBaseBlock stargateBaseBlock = new StargateBaseBlock();
	public static DHDBlock dhdBlock = new DHDBlock();
	public static StargateMemberBlock ringBlock = new StargateMemberBlock("ring_block");
	public static StargateMemberBlock chevronBlock = new StargateMemberBlock("chevron_block");
	public static NaquadahOreBlock naquadahOreBlock = new NaquadahOreBlock();
	
	public static Block[] blocks = {
			stargateBaseBlock,
			dhdBlock,
			ringBlock,
			chevronBlock,
			naquadahOreBlock };
	
	public static Item[] getItems() {
		Item[] items = new Item[blocks.length];
		
		for (int i=0; i<blocks.length; i++)
			items[i] = ((BlockBase) blocks[i]).getItemBlock();
		
		return items;
	}
}

