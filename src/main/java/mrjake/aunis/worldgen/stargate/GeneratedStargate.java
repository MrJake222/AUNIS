package mrjake.aunis.worldgen.stargate;

import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

/**
 * Generated stargate data
 * @param <T> stargate type
 */
public final class GeneratedStargate {
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

    public StargateAddress getAddress(){
        return getAddress(tile.getSymbolType());
    }

    public StargateAddress getAddress(SymbolTypeEnum type){
        return tile.getStargateAddress(type);
    }
}
