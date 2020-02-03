package mrjake.aunis.worldgen;

import java.util.List;
import java.util.Map;
import java.util.Random;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.capability.EnergyStorageUncapped;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.MergeHelper;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class StargateGenerator {
	
	public static GeneratedStargate generateStargate(World world) {
		Random rand = new Random();
		
//		boolean nether = rand.nextFloat() < AunisConfig.mysteriousConfig.netherChance;
		BlockPos pos;
		int tries = 0;
		
		WorldServer worldToSpawn = world.getMinecraftServer().getWorld(0);
		
		do {
			int x = (int) (AunisConfig.mysteriousConfig.minOverworldCoords + (rand.nextFloat() * (AunisConfig.mysteriousConfig.maxOverworldCoords - AunisConfig.mysteriousConfig.minOverworldCoords))) * (rand.nextBoolean() ? -1 : 1);
			int z = (int) (AunisConfig.mysteriousConfig.minOverworldCoords + (rand.nextFloat() * (AunisConfig.mysteriousConfig.maxOverworldCoords - AunisConfig.mysteriousConfig.minOverworldCoords))) * (rand.nextBoolean() ? -1 : 1);
			
			pos = StargateGenerator.checkForPlace(worldToSpawn, x/16, z/16);
			tries++;
		} while (pos == null && tries < 100);
		
		if (tries == 100) {
			Aunis.log("Failed to find place");
			
			return null;
		}
		
		EnumFacing facing = findOptimalRotation(worldToSpawn, pos);
		Rotation rotation;
		
		switch (facing) {
			case SOUTH: rotation = Rotation.CLOCKWISE_90; break;
			case WEST:  rotation = Rotation.CLOCKWISE_180; break;
			case NORTH: rotation = Rotation.COUNTERCLOCKWISE_90; break;
			case EAST:  rotation = Rotation.NONE; break;
			default:    rotation = Rotation.NONE; break;
		}
		
		return generateStargateDesert(worldToSpawn, pos, facing, rotation);
	}
	
	private static final int SG_SIZE_X = 12;
	private static final int SG_SIZE_Z = 13;
	
	private static final int SG_SIZE_X_PLAINS = 11;
	private static final int SG_SIZE_Z_PLAINS = 11;
	
	private static BlockPos checkForPlace(World world, int chunkX, int chunkZ) {
		if (world.isChunkGeneratedAt(chunkX, chunkZ))
			return null;
		
		Chunk chunk = world.getChunk(chunkX, chunkZ);
		
		int y = chunk.getHeightValue(8, 8);
		
		if (y > 240)
			return null;
		
		BlockPos pos = new BlockPos(chunkX*16, y, chunkZ*16);
		String biomeName = chunk.getBiome(pos, world.getBiomeProvider()).getRegistryName().getPath();
					
		boolean desert = biomeName.contains("desert");		
		
		if (!biomeName.contains("ocean") && !biomeName.contains("river") && !biomeName.contains("beach")) {
//		if (biomeName.contains("Ocean")) {
			int x = desert ? SG_SIZE_X : SG_SIZE_X_PLAINS;
			int z = desert ? SG_SIZE_Z : SG_SIZE_Z_PLAINS;
			
			int y1 = chunk.getHeightValue(0, 0);
			int y2 = chunk.getHeightValue(x, z);
			
			int y3 = chunk.getHeightValue(x, 0);
			int y4 = chunk.getHeightValue(0, z);
			
			// No steep hill
			if (Math.abs(y1 - y2) <= 1 && Math.abs(y3 - y4) <= 1) {
				return pos.subtract(new BlockPos(0, 1, 0));
			}
			
			else {
				Aunis.log("too steep");
			}
		}
		
		else {
			Aunis.log("failed, " + biomeName);
		}
		
		return null;
	}
	
	private static final int MAX_CHECK = 100;
	
	private static EnumFacing findOptimalRotation(World world, BlockPos pos) {
		BlockPos start = pos.add(0, 5, 5);
		int max = -1;
		EnumFacing maxFacing = EnumFacing.EAST;
		
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			RayTraceResult rayTraceResult = world.rayTraceBlocks(new Vec3d(start), new Vec3d(start.offset(facing, MAX_CHECK)));
			
			if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
				int distance = (int) rayTraceResult.getBlockPos().distanceSq(start);
//				Aunis.info(facing.getName().toUpperCase() + ": distance: " + distance);
				
				if (distance > max) {
					max = distance;
					maxFacing = facing;
				}
			}
			
			else {
//				Aunis.info(facing.getName().toUpperCase() + ": null");
				
				max = 100000;
				maxFacing = facing;
			}
		}
		
//		Aunis.info("maxFacing: " + maxFacing.getName().toUpperCase());
		return maxFacing;
	}
	
	private static GeneratedStargate generateStargateDesert(World world, BlockPos pos, EnumFacing facing, Rotation rotation) {
		WorldServer worldServer = (WorldServer) world;
		MinecraftServer server = world.getMinecraftServer();

		Biome biome = world.getBiome(pos);
		boolean desert = biome.getRegistryName().getPath().contains("desert");		
		
		TemplateManager templateManager = worldServer.getStructureTemplateManager();
		Template template = templateManager.getTemplate(server, new ResourceLocation(Aunis.ModID, desert ? "sg_desert" : "sg_plains"));
		
		if (template != null) {			
			Random rand = new Random();
			
			PlacementSettings settings = new PlacementSettings().setIgnoreStructureBlock(false).setRotation(rotation);
			template.addBlocksToWorld(world, pos, settings);
			
			Map<BlockPos, String> datablocks = template.getDataBlocks(pos, settings);
			BlockPos gatePos = null;
			BlockPos dhdPos = null;
			
			for (BlockPos dataPos : datablocks.keySet()) {
				String name = datablocks.get(dataPos);
								
				if (name.equals("base")) {
					gatePos = dataPos.add(0, -3, 0);
					
					world.setBlockState(gatePos, world.getBlockState(gatePos).withProperty(AunisProps.FACING_HORIZONTAL, facing));
					MergeHelper.updateChevRingRotation(world, gatePos, facing);
					MergeHelper.updateChevRingBasePos(world, gatePos, facing);
					
					world.setBlockToAir(dataPos);
					world.setBlockToAir(dataPos.down()); // save block
				}
				
				else if (name.equals("dhd")) {
					dhdPos = dataPos.down();
					
					if (rand.nextFloat() < AunisConfig.mysteriousConfig.despawnDhdChance) {						
						world.setBlockToAir(dhdPos);
					}
					
					else {
						IBlockState dhdState = world.getBlockState(dhdPos);
						int horizontalRotation = dhdState.getValue(AunisProps.ROTATION_HORIZONTAL);
						horizontalRotation = rotation.rotate(horizontalRotation, 16);
						
						world.setBlockState(dhdPos, dhdState.withProperty(AunisProps.ROTATION_HORIZONTAL, horizontalRotation));
						
						if (rand.nextFloat() < AunisConfig.mysteriousConfig.despawnCrystalChance) {							
//							Aunis.info("despawning crystal");
						}
						
						else {
							int power = (int) ((0.3 + (rand.nextFloat() * 0.6)) * AunisConfig.powerConfig.dhdCrystalEnergyStorage);
							ItemStack crystal = new ItemStack(AunisItems.crystalControlDhd);
							EnergyStorageUncapped uncapped = (EnergyStorageUncapped) crystal.getCapability(CapabilityEnergy.ENERGY, null);
							uncapped.setEnergyStored(power);
							
							((ItemStackHandler) world.getTileEntity(dhdPos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)).setStackInSlot(0, crystal);
						}
					}
					
					world.setBlockToAir(dataPos);
				}
			}
			
			if (gatePos != null && dhdPos != null) {				
				StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(gatePos);
				DHDTile dhdTile = (DHDTile) world.getTileEntity(dhdPos);
				
				if (gateTile != null) {
					gateTile.gateAddress = null;
					List<EnumSymbol> address = gateTile.generateAddress();
					
					if (dhdTile != null) {
						dhdTile.setLinkedGate(gateTile.getPos());
						gateTile.setLinkedDHD(dhdTile.getPos());
					}
					
					return new GeneratedStargate(address, biome.getRegistryName().getPath());
				}
			}
		}
		
		else {
			Aunis.info("template null");
		}
		
		return null;
	}
	
	public static class GeneratedStargate {

		public List<EnumSymbol> address;
		public String path;
		
		public GeneratedStargate(List<EnumSymbol> address, String path) {
			this.address = address;
			this.path = path;
		}
		
	}
}
