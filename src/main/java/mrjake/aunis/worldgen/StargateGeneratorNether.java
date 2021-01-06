package mrjake.aunis.worldgen;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateDimensionConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.worldgen.StargateGenerationHelper.Direction;
import mrjake.aunis.worldgen.StargateGenerationHelper.DirectionResult;
import mrjake.aunis.worldgen.StargateGenerationHelper.FreeSpace;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

/**
 * Handles generating the Stargate structure in the Nether.
 * 
 * @author MrJake222
 */
public final class StargateGeneratorNether extends AbstractStargateGenerator<StargateMilkyWayBaseTile> {
	public static StargateGeneratorNether INSTANCE = new StargateGeneratorNether();

	/**
	 * Directions check definitions
	 */
	private static final List<Direction> DIRECTIONS = Arrays.asList(
			new Direction(EnumFacing.UP, false, 0).setRequiredMinimum(12).setIgnoreInMaximum(),
			new Direction(EnumFacing.NORTH, true, 2),
			new Direction(EnumFacing.SOUTH, true, 2),
			new Direction(EnumFacing.WEST, true, 2),
			new Direction(EnumFacing.EAST, true, 2));
	
	/**
	 * Allowed blocks
	 */
	private static final List<BlockMatcher> ALLOWED_BLOCKS_BELOW = Arrays.asList(
			BlockMatcher.forBlock(Blocks.NETHERRACK),
			BlockMatcher.forBlock(Blocks.QUARTZ_ORE),
			BlockMatcher.forBlock(Blocks.SOUL_SAND));
	
	private static final int MINIMAL_FRONT_SPACE = 16;
	private static final int MINIMAL_SIDE_SPACE = 5;

	@Override
	protected OptimalStargatePlace findOptimalPlace(WorldServer world, BlockPos posIn) {
		BlockPos start = posIn;
		BlockPos current = start;
		int count = 0;
		int pass = 1;

		BlockPos found = null;
		Entry<EnumFacing, DirectionResult> frontResult = null;

		while (found == null) {
			Aunis.logger.debug("StargateGeneratorNether: count: " + count + ", pass: " + pass + ", current: " + current);

			for (MutableBlockPos pos : MutableBlockPos.getAllInBoxMutable(current, current.add(16, 16, 16))) {
				if (world.isAirBlock(pos.down()))
					continue;

				FreeSpace freeSpace = StargateGenerationHelper.getFreeSpaceInDirections(world, pos, DIRECTIONS, 16, ALLOWED_BLOCKS_BELOW);

				if (freeSpace != null) {
					if (freeSpace.getMaxDistance().getValue().distance >= MINIMAL_FRONT_SPACE) {
						int left = freeSpace.getDistance(freeSpace.getMaxDistance().getKey().rotateYCCW()).distance;
						int right = freeSpace.getDistance(freeSpace.getMaxDistance().getKey().rotateY()).distance;

						if (left >= MINIMAL_SIDE_SPACE && right >= MINIMAL_SIDE_SPACE) {
							found = pos.toImmutable();
							frontResult = freeSpace.getMaxDistance();

							break;
						}
					}
				}
			}

			if (count == 0)
				current = start.add(16, 0, 0);
			else if (count == 1)
				current = start.add(0, 0, 16);
			else if (count == 2)
				current = start.add(16, 0, 16);
			else if (count == 3) {
				if (start.getY() > 100) {
					start = posIn.add(32*pass, 0, 32*pass);
					pass++;
				}

				else {
					start = start.add(0, 16, 0);
				}

				current = start;
			}

			count++;

			if (count > 3)
				count = 0;
		}

		Aunis.logger.debug("StargateGeneratorNether: /tp " + found.getX() + " " + found.getY() + " " + found.getZ());

		Rotation rotation;

		switch (frontResult.getKey()) {
			case SOUTH: rotation = Rotation.CLOCKWISE_180; break;
			case WEST:  rotation = Rotation.COUNTERCLOCKWISE_90; break;
			case NORTH: rotation = Rotation.NONE; break;
			case EAST:  rotation = Rotation.CLOCKWISE_90; break;
			default:    rotation = Rotation.NONE; break;
		}

		int y = Math.min(frontResult.getValue().ydiff, 0);
		y -= 2;

		BlockPos translate = new BlockPos(-7, y, -16).rotate(rotation);
		found = found.add(translate);
		return new OptimalStargatePlace(found, rotation);
	}

	@Override
	protected Template getTemplate(WorldServer world, TemplateManager templateManager, BlockPos pos) {
		return templateManager.getTemplate(world.getMinecraftServer(), new ResourceLocation(Aunis.ModID, "sg_nether_" + (AunisConfig.stargateSize == StargateSizeEnum.LARGE ? "large" : "small")));
	}

	@Override
	protected ITemplateProcessor getTemplateProcessor() {
		return new StargateNetherTemplateProcessor();
	}

	@Override
	protected BlockPos placeStargateBase(WorldServer world, BlockPos dataPos) {
		world.setBlockState(dataPos, Blocks.NETHER_BRICK.getDefaultState());

		StargateMilkyWayBaseTile tile = (StargateMilkyWayBaseTile) world.getTileEntity(dataPos.down());
		tile.getMergeHelper().updateMembersMergeStatus(tile.getWorld(), tile.getPos(), tile.getFacing(), true);
		IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		itemHandler.insertItem(4, new ItemStack(AunisBlocks.CAPACITOR_BLOCK), false);
		itemHandler.insertItem(0, new ItemStack(AunisItems.CRYSTAL_GLYPH_MILKYWAY), false);

		if (StargateDimensionConfig.netherOverworld8thSymbol())
			itemHandler.insertItem(1, new ItemStack(AunisItems.CRYSTAL_GLYPH_STARGATE), false);
		return tile.getPos();
	}

	@Override
	protected BlockPos placeDHD(WorldServer world, BlockPos dataPos) {
		world.setBlockToAir(dataPos);

		DHDTile tile = (DHDTile) world.getTileEntity(dataPos.down());

		int fluid = (int) (6000 + Math.random() * 3000);
		((FluidTank) tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)).fillInternal(new FluidStack(AunisFluids.moltenNaquadahRefined, fluid), true);

		IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		itemHandler.insertItem(0, new ItemStack(AunisItems.CRYSTAL_CONTROL_DHD), false);

		if (StargateDimensionConfig.netherOverworld8thSymbol())
			itemHandler.insertItem(1, new ItemStack(AunisItems.CRYSTAL_GLYPH_DHD), false);
		return tile.getPos();
	}
}
