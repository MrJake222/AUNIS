package mrjake.aunis.gui.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.state.RendererState;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Specific implementations hold all GUI variables
 * that matter to client-side rendering of said GUI.
 * 
 * Similar system to {@link RendererState}, except not saved to NBT and synchronized
 * on GUI open, as opposed to first client tick.
 * 
 * And also, it's recommended to instantiate this every time {@link IGuiStateProvider#getGuiState()} is called
 * 
 * @author MrJake
 */
public interface IGuiState {
	
	/**
	 * @see IMessage
	 * 
	 * @param buf - byte buffer
	 */
	public abstract void toBytes(ByteBuf buf);
	public abstract void fromBytes(ByteBuf buf);
}
