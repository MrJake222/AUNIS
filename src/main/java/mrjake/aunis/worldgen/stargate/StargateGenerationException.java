package mrjake.aunis.worldgen.stargate;

public final class StargateGenerationException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4075451344986277587L;

	public StargateGenerationException(String msg, Object ...args) {
        super(String.format(msg, args));
    }
}
