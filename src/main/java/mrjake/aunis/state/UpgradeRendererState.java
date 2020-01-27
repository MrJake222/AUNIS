package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class UpgradeRendererState extends State {
	public boolean doInsertAnimation;
	public boolean doRemovalAnimation;
	public boolean doUpgradeRender;
	public long insertionTime;
	
	public UpgradeRendererState() {
		this(false, false, false, 0);
	}
	
	/**
	 * Creates Renderer's state with given parameters.
	 *  
	 * @param pos - Tile's position.
	 * @param doInsertAnimation - Doing animation?
	 * @param doRemovalAnimation - Is the animation removal animation?
	 * @param doUpgradeRender - Do the upgrade's render code need to run?
	 * @param insertionTime - World's time of the animation start
	 */
	public UpgradeRendererState(boolean doInsertAnimation, boolean doRemovalAnimation, boolean doUpgradeRender, long insertionTime) {
		this.doInsertAnimation = doInsertAnimation;
		this.doRemovalAnimation = doRemovalAnimation;
		this.doUpgradeRender = doUpgradeRender;
		this.insertionTime = insertionTime;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(doInsertAnimation);
		buf.writeBoolean(doRemovalAnimation);
		buf.writeBoolean(doUpgradeRender);
		buf.writeLong(insertionTime);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.doInsertAnimation = buf.readBoolean();		
		this.doRemovalAnimation = buf.readBoolean();		
		this.doUpgradeRender = buf.readBoolean();		
		this.insertionTime = buf.readLong();	
	}
}
