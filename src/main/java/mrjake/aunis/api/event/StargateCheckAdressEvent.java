package mrjake.aunis.api.event;

import mrjake.aunis.stargate.StargateOpenResult;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;

/**
 * Event that posted on address check
 * Use {@link #setOpenResult(StargateOpenResult)} to set custom open result (for example {@link StargateOpenResult#ABORTED} or {@link StargateOpenResult#ADDRESS_MALFORMED})
 */
public final class StargateCheckAdressEvent extends StargateConnectedAbstractEvent {
    private StargateOpenResult openResult = StargateOpenResult.OK;

    public StargateCheckAdressEvent(StargateAbstractBaseTile tile, StargateAbstractBaseTile targetTile) {
        super(tile, targetTile, true);
    }

    public StargateOpenResult getOpenResult() {
        return openResult;
    }

    public void setOpenResult(StargateOpenResult res) {
        this.openResult = res;
    }
}
