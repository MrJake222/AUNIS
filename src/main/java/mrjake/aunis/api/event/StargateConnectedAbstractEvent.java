package mrjake.aunis.api.event;

import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;

/**
 * Parent event for all stargate events with connected stargates
 */
public abstract class StargateConnectedAbstractEvent extends StargateAbstractEvent {
    private final StargateAbstractBaseTile targetTile;
    private final boolean initiating;

    public StargateConnectedAbstractEvent(StargateAbstractBaseTile tile, StargateAbstractBaseTile targetTile, boolean initiating) {
        super(tile);
        this.targetTile = targetTile;
        this.initiating = initiating;
    }

    /**
     * Get target stargate
     * @return target stargate tileentity
     */
    public StargateAbstractBaseTile getTargetTile() {
        return targetTile;
    }

    /**
     * Get address of target stargate
     * @return stargate address by stargate type
     */
    public StargateAddress getTargetAddress(){
        return targetTile.getStargateAddress(targetTile.getSymbolType());
    }

    /**
     * Get address of target stargate
     * @param type address type
     * @return stargate address
     */
    public StargateAddress getTargetAddress(SymbolTypeEnum type){
        return targetTile.getStargateAddress(type);
    }

    /**
     * Is {@link #getTile()} initiating stargate or not
     * @return true if yes, false if no
     */
    public boolean isInitiating() {
        return initiating;
    }
}
