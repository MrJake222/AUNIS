package mrjake.aunis.api.event;

import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Event that posted on chevron lock
 * This event is cancelable, you can cancel it and chevron will not lock
 */
@Cancelable
public final class StargateChevronEngagedEvent extends StargateAbstractEvent {
    private final SymbolInterface symbol;
    private final boolean lastSymbol;

    public StargateChevronEngagedEvent(StargateAbstractBaseTile tile, SymbolInterface symbol, boolean lastSymbol) {
        super(tile);
        this.symbol = symbol;
        this.lastSymbol = lastSymbol;
    }

    public SymbolInterface getSymbol() {
        return symbol;
    }

    public boolean isLastSymbol() {
        return lastSymbol;
    }
}
