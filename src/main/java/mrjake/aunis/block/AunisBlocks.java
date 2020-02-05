package mrjake.aunis.block;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.stargate.StargateMilkyWayBaseBlock;
import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import mrjake.aunis.block.stargate.StargateOrlinBaseBlock;
import mrjake.aunis.block.stargate.StargateOrlinMemberBlock;
import mrjake.aunis.item.StargateMilkyWayMemberItemBlock;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.TRControllerTile;
import mrjake.aunis.tileentity.TransportRingsTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber
public class AunisBlocks {
	public static NaquadahOreBlock naquadahOreBlock = new NaquadahOreBlock();
	public static Block naquadahBlock = new Block(Material.IRON).setRegistryName(Aunis.ModID, "naquadah_block").setTranslationKey(Aunis.ModID + ".naquadah_block");
	
	public static StargateMilkyWayBaseBlock stargateMilkyWayBaseBlock = new StargateMilkyWayBaseBlock();	
	public static StargateOrlinBaseBlock stargateOrlinBaseBlock = new StargateOrlinBaseBlock();	
	public static StargateOrlinMemberBlock stargateOrlinMemberBlock = new StargateOrlinMemberBlock();	
	
	public static DHDBlock dhdBlock = new DHDBlock();
	public static CrystalInfuserBlock crystalInfuserBlock = new CrystalInfuserBlock();
	
	public static TransportRingsBlock transportRingsBlock = new TransportRingsBlock();
	public static TRControllerBlock trControllerBlock = new TRControllerBlock();
	public static InvisibleBlock invisibleBlock = new InvisibleBlock();	
	
	// -----------------------------------------------------------------------------
	public static StargateMilkyWayMemberBlock stargateMilkyWayMemberBlock = new StargateMilkyWayMemberBlock();
	
	
	private static Block[] blocks = {
		naquadahOreBlock,
		naquadahBlock,
		
		stargateMilkyWayBaseBlock,
		stargateOrlinBaseBlock,
		stargateOrlinMemberBlock,
		
		dhdBlock,
		crystalInfuserBlock,
		
		transportRingsBlock,
		trControllerBlock,
		invisibleBlock
	};
		
	@SubscribeEvent
	public static void onRegisterBlocks(Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		
		registry.registerAll(blocks);
		registry.register(stargateMilkyWayMemberBlock);
		
		GameRegistry.registerTileEntity(StargateMilkyWayBaseTile.class, AunisBlocks.stargateMilkyWayBaseBlock.getRegistryName());
		GameRegistry.registerTileEntity(StargateOrlinBaseTile.class, AunisBlocks.stargateOrlinBaseBlock.getRegistryName());
		
		GameRegistry.registerTileEntity(StargateMilkyWayMemberTile.class, AunisBlocks.stargateMilkyWayMemberBlock.getRegistryName());
		GameRegistry.registerTileEntity(DHDTile.class, AunisBlocks.dhdBlock.getRegistryName());
		GameRegistry.registerTileEntity(CrystalInfuserTile.class, AunisBlocks.crystalInfuserBlock.getRegistryName());
		GameRegistry.registerTileEntity(TransportRingsTile.class, AunisBlocks.transportRingsBlock.getRegistryName());
		GameRegistry.registerTileEntity(TRControllerTile.class, AunisBlocks.trControllerBlock.getRegistryName());
	}
	
	@SubscribeEvent
	public static void onRegisterItems(Register<Item> event) {	
		IForgeRegistry<Item> registry = event.getRegistry();
		
		for (Block block : blocks)
			registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
		
		registry.register(new StargateMilkyWayMemberItemBlock(stargateMilkyWayMemberBlock));		
	}
	
	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {
		for (Block block : blocks) {			
			ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
		}
		
		int ringMeta = stargateMilkyWayMemberBlock.getMetaFromState(stargateMilkyWayMemberBlock.getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING));
		int chevronMeta = stargateMilkyWayMemberBlock.getMetaFromState(stargateMilkyWayMemberBlock.getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.CHEVRON));
		
		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(stargateMilkyWayMemberBlock), ringMeta, new ModelResourceLocation("aunis:stargate_milkyway_ring_block"));
		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(stargateMilkyWayMemberBlock), chevronMeta, new ModelResourceLocation("aunis:stargate_milkyway_chevron_block"));
	}
	
	@Nullable
	public static Block remapBlock(String oldBlockName) {
		switch (oldBlockName) {
			case "aunis:stargatebase_block":
				return stargateMilkyWayBaseBlock;
				
			case "aunis:stargate_member_block":
				return stargateMilkyWayMemberBlock;
				
			case "aunis:stargatebase_orlin_block":
				return stargateOrlinBaseBlock;
				
			case "aunis:stargatemember_orlin_block":
				return stargateOrlinMemberBlock;
				
			default:
				return null;
		}
	}
	
	@SubscribeEvent
	public static void onMissingBlockMappings(RegistryEvent.MissingMappings<Block> event) {
		for (Mapping<Block> mapping : event.getMappings()) {
			Block newBlock = remapBlock(mapping.key.toString());
			
			if (newBlock != null)
				mapping.remap(newBlock);
		}
	}
	
	@SubscribeEvent
	public static void onMissingItemMappings(RegistryEvent.MissingMappings<Item> event) {
		for (Mapping<Item> mapping : event.getMappings()) {
			switch (mapping.key.toString()) {
				case "aunis:stargatebase_block":
					mapping.remap(ItemBlock.getItemFromBlock(stargateMilkyWayBaseBlock));
					break;
				
				case "aunis:stargate_member_block":
					mapping.remap(ItemBlock.getItemFromBlock(stargateMilkyWayMemberBlock));
					break;
					
				case "aunis:stargatebase_orlin_block":
					mapping.remap(ItemBlock.getItemFromBlock(stargateOrlinBaseBlock));
					break;
					
				case "aunis:stargatemember_orlin_block":
					mapping.remap(ItemBlock.getItemFromBlock(stargateOrlinMemberBlock));
					break;
			}
		}
	}
}

