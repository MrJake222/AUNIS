package mrjake.aunis.gui.state;

import io.netty.buffer.ByteBuf;

/**
 * Should be implemented by tile entities with GUI.
 * 
 * Provides basic method for getting the state(Server-side only)
 * and setting the state to the client-side tile entity.
 * 
 * @author MrJake
 * 
 */
public interface IGuiStateProvider {
	
	/**
	 * Server-side only. Called by 
	 * 
	 * @return implementation of {@link IGuiState} containing all gui-related variables
	 */
	public abstract IGuiState getGuiState();
	
	
	/**
	 * Instantiates tile-specific implementation of {@link IGuiState}
	 * by calling new and then {@link IGuiState#fromBytes(ByteBuf)}. 
	 * 
	 * @param buf - Byte buffer from the packet
	 * @return tile-specific implementation of {@link IGuiState}
	 */
	public abstract IGuiState createGuiState(ByteBuf buf);
}
