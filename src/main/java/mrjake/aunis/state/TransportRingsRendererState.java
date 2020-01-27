package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

/**
 * Rings state.
 * Used to communicate the state of rings to clients loading the block.
 * 
 * DO NOT use this to start animations. It will send too much unnecessary parameters to the clients.
 */
public class TransportRingsRendererState extends State {
	
	/**
	 * Indicates if the block should render rings at all
	 */
	public boolean isAnimationActive;

	/**
	 * It should be useful when the player comes around in ring's mid-transfer state
	 */
	public long animationStart;
	
	/**
	 * True: Rings going up
	 * False: Rings going back into ground
	 */
	public boolean ringsUprising;

	
	public TransportRingsRendererState() {
		this.isAnimationActive = false;
		
		this.animationStart = 0;
		this.ringsUprising = true;
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(isAnimationActive);
		buf.writeLong(animationStart);
		buf.writeBoolean(ringsUprising);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		isAnimationActive = buf.readBoolean();
		animationStart = buf.readLong();
		ringsUprising = buf.readBoolean();
	}
}
