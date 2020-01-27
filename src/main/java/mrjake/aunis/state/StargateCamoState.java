package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

/**
 * Holds {@link IBlockState} of camouflage block to be displayed instead.
 * 
 * @author MrJake
 */
public class StargateCamoState extends State {
	public StargateCamoState() {}
	
	private IBlockState state;
		
	public StargateCamoState(IBlockState state) {
		this.state = state;
	}
	
	public IBlockState getState() {
		return state;
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(state != null);
		if (state != null) {
			buf.writeInt(Block.getIdFromBlock(state.getBlock()));
			buf.writeInt(state.getBlock().getMetaFromState(state));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void fromBytes(ByteBuf buf) {
		if (buf.readBoolean()) {
			Block block = Block.getBlockById(buf.readInt());
			state = block.getStateFromMeta(buf.readInt());
		}
	}	
}
