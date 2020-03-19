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
import mrjake.aunis.tileentity.stargate.StargateOrlinMemberTile;
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
	public static final NaquadahOreBlock ORE_NAQUADAH_BLOCK = new NaquadahOreBlock();
	public static final Block NAQUADAH_BLOCK = new Block(Material.IRON).setRegistryName(Aunis.ModID, "naquadah_block").setTranslationKey(Aunis.ModID + ".naquadah_block");
	
	public static final StargateMilkyWayBaseBlock STARGATE_MILKY_WAY_BASE_BLOCK = new StargateMilkyWayBaseBlock();	
	public static final StargateOrlinBaseBlock STARGATE_ORLIN_BASE_BLOCK = new StargateOrlinBaseBlock();	
	public static final StargateOrlinMemberBlock STARGATE_ORLIN_MEMBER_BLOCK = new StargateOrlinMemberBlock();	
	
	public static final DHDBlock DHD_BLOCK = new DHDBlock();
	public static final CrystalInfuserBlock CRYSTAL_INFUSER_BLOCK = new CrystalInfuserBlock();
	
	public static final TransportRingsBlock TRANSPORT_RINGS_BLOCK = new TransportRingsBlock();
	public static final TRControllerBlock TR_CONTROLLER_BLOCK = new TRControllerBlock();
	public static final InvisibleBlock INVISIBLE_BLOCK = new InvisibleBlock();	
	
	public static final CapacitorBlock CAPACITOR_BLOCK = new CapacitorBlock();
	
	// -----------------------------------------------------------------------------
	public static StargateMilkyWayMemberBlock stargateMilkyWayMemberBlock = new StargateMilkyWayMemberBlock();
	
	
	private static Block[] blocks = {
		ORE_NAQUADAH_BLOCK,
		NAQUADAH_BLOCK,
		
		STARGATE_MILKY_WAY_BASE_BLOCK,
		STARGATE_ORLIN_BASE_BLOCK,
		STARGATE_ORLIN_MEMBER_BLOCK,
		
		DHD_BLOCK,
		CRYSTAL_INFUSER_BLOCK,
		
		TRANSPORT_RINGS_BLOCK,
		TR_CONTROLLER_BLOCK,
		INVISIBLE_BLOCK,
		CAPACITOR_BLOCK
	};
		
	@SubscribeEvent
	public static void onRegisterBlocks(Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		
		registry.registerAll(blocks);
		registry.register(stargateMilkyWayMemberBlock);
		
		GameRegistry.registerTileEntity(StargateMilkyWayBaseTile.class, AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK.getRegistryName());
		GameRegistry.registerTileEntity(StargateOrlinBaseTile.class, AunisBlocks.STARGATE_ORLIN_BASE_BLOCK.getRegistryName());
		
		GameRegistry.registerTileEntity(StargateMilkyWayMemberTile.class, AunisBlocks.stargateMilkyWayMemberBlock.getRegistryName());
		GameRegistry.registerTileEntity(StargateOrlinMemberTile.class, AunisBlocks.STARGATE_ORLIN_MEMBER_BLOCK.getRegistryName());
		GameRegistry.registerTileEntity(DHDTile.class, AunisBlocks.DHD_BLOCK.getRegistryName());
		GameRegistry.registerTileEntity(CrystalInfuserTile.class, AunisBlocks.CRYSTAL_INFUSER_BLOCK.getRegistryName());
		GameRegistry.registerTileEntity(TransportRingsTile.class, AunisBlocks.TRANSPORT_RINGS_BLOCK.getRegistryName());
		GameRegistry.registerTileEntity(TRControllerTile.class, AunisBlocks.TR_CONTROLLER_BLOCK.getRegistryName());
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
				return STARGATE_MILKY_WAY_BASE_BLOCK;
				
			case "aunis:stargate_member_block":
				return stargateMilkyWayMemberBlock;
				
			case "aunis:stargatebase_orlin_block":
				return STARGATE_ORLIN_BASE_BLOCK;
				
			case "aunis:stargatemember_orlin_block":
				return STARGATE_ORLIN_MEMBER_BLOCK;
				
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
					mapping.remap(ItemBlock.getItemFromBlock(STARGATE_MILKY_WAY_BASE_BLOCK));
					break;
				
				case "aunis:stargate_member_block":
					mapping.remap(ItemBlock.getItemFromBlock(stargateMilkyWayMemberBlock));
					break;
					
				case "aunis:stargatebase_orlin_block":
					mapping.remap(ItemBlock.getItemFromBlock(STARGATE_ORLIN_BASE_BLOCK));
					break;
					
				case "aunis:stargatemember_orlin_block":
					mapping.remap(ItemBlock.getItemFromBlock(STARGATE_ORLIN_MEMBER_BLOCK));
					break;
			}
		}
	}
}

