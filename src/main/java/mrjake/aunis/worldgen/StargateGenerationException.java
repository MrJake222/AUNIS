package mrjake.aunis.worldgen;

public final class StargateGenerationException extends RuntimeException {

    public StargateGenerationException(String msg, Object ...args) {
        super(String.format(msg, args));
    }
}
