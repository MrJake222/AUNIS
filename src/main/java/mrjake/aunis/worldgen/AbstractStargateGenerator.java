package mrjake.aunis.worldgen;

import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Stargate generator is used to place stargate in random places
 *
 * ToDo create registry for stargate generators, add it to the API and use it to get random stargate generator for mystery page
 * ToDo maybe do something with {@link GeneratedStargate#getAddress(SymbolTypeEnum)} to make type configurable in the future
 * ToDo maybe remake as builder and use JSON system to create generators / make implementation for JSON
 * @param <T> stargate type
 */
public abstract class AbstractStargateGenerator<T extends StargateAbstractBaseTile> {

    public GeneratedStargate<T> generateStargate(WorldServer world, BlockPos startPos) {
        return placeStargate(world, findOptimalPlace(world, startPos));
    }

    protected abstract OptimalStargatePlace findOptimalPlace(WorldServer world, BlockPos startPos);

    protected abstract Template getTemplate(WorldServer world, TemplateManager templateManager, BlockPos pos);

    @Nullable
    protected ITemplateProcessor getTemplateProcessor() {
        return null;
    }

    protected DataBlockProcessionResult processDataBlocks(WorldServer world, Map<BlockPos, String> datablocks){
        final DataBlockProcessionResult res = new DataBlockProcessionResult();

        for (Map.Entry<BlockPos, String> datablock : datablocks.entrySet()) {
            switch (datablock.getValue()) {
                case "base":
                    res.basePos = placeStargateBase(world, datablock.getKey());
                    break;

                case "dhd":
                    res.dhdPos = placeDHD(world, datablock.getKey());;
                    break;
            }
        }

        return res;
    }

    protected abstract BlockPos placeStargateBase(WorldServer world, BlockPos dataPos);

    protected abstract BlockPos placeDHD(WorldServer world, BlockPos dataPos);

    protected GeneratedStargate<T> placeStargate(WorldServer world, OptimalStargatePlace place){
        Template template = getTemplate(world, world.getStructureTemplateManager(), place.pos);

        if(template == null)
            throw new StargateGenerationException("Structure template not found in placeStargate");

        PlacementSettings settings = new PlacementSettings()
                .setIgnoreStructureBlock(false)
                .setRotation(place.rotation);

        template.addBlocksToWorld(world, place.pos, getTemplateProcessor(), settings, 3);

        DataBlockProcessionResult processRes = processDataBlocks(world, template.getDataBlocks(place.pos, settings));

        if(processRes.basePos == null)
            throw new StargateGenerationException("BasePos is null in placeStargate after processDataBlocks");

        //ToDo WARNING this will work for MilkyWay stargate only. I should change this probably but it's 7 AM lol. I mean... I need to sleep. Sorry
        if(processRes.dhdPos != null)
            StargateGenerationHelper.updateLinkedGate(world, processRes.basePos, processRes.dhdPos);

        return new GeneratedStargate<>((T) world.getTileEntity(processRes.basePos));
    }

    protected static final class OptimalStargatePlace {
        public final BlockPos pos;
        public final Rotation rotation;

        public OptimalStargatePlace(BlockPos found, Rotation rotation) {
            this.pos = found;
            this.rotation = rotation;
        }
    }

    //ToDo maybe replace with BlockPos[2] (but it will look worse probably)
    protected static final class DataBlockProcessionResult {
        public BlockPos basePos;
        public BlockPos dhdPos;
    }

    /**
     * Generated stargate data
     * @param <T> stargate type
     */
    public static final class GeneratedStargate<T extends StargateAbstractBaseTile> {
        private final StargateAbstractBaseTile tile;

        public GeneratedStargate(final StargateAbstractBaseTile tile){
            this.tile = tile;
        }

        public StargateAbstractBaseTile getStargate() {
            return tile;
        }

        public WorldServer getWorld(){
            return (WorldServer) tile.getWorld();
        }

        public BlockPos getPos(){
            return tile.getPos();
        }

        public StargateAddress getAddress(SymbolTypeEnum type){
            return tile.getStargateAddress(type);
        }
    }
}
