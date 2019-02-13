package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class UpgradeRendererState extends RendererState {
	public boolean doInsertAnimation;
	public boolean doRemovalAnimation;
	public boolean doUpgradeRender;
	public long insertionTime;
	
	public UpgradeRendererState(BlockPos pos) {
		this(pos, false, false, false, 0);
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
	public UpgradeRendererState(BlockPos pos, boolean doInsertAnimation, boolean doRemovalAnimation, boolean doUpgradeRender, long insertionTime) {
		super(pos);
		
		this.doInsertAnimation = doInsertAnimation;
		this.doRemovalAnimation = doRemovalAnimation;
		this.doUpgradeRender = doUpgradeRender;
		this.insertionTime = insertionTime;
	}
	
	/**
	 * Generate state from buffer
	 * 
	 * @param buf
	 */
	public UpgradeRendererState(ByteBuf buf) {
		super(buf);
	}
	
	/**
	 * Generate state from NBT Compound
	 * 
	 * @param buf
	 */
	public UpgradeRendererState(NBTTagCompound compound) {
		super(compound);
	}

	@Override
	protected String getKeyName() {
		return "upgradeRendererState";
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
