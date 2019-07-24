package mrjake.aunis.integration.opencomputers;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.tileentity.stargate.StargateBaseTileSG1;

/**
 * Interface used to call OpenComputers specific functions.
 * If OC is loaded, it will call functions {@link OCWrapperLoaded}.
 * If not, it'll call empty methods in {@link OCWrapperNotLoaded}.
 * 
 * @author MrJake
 */
public interface OCWrapperInterface {
	
	/**
	 * Registers appropriate class.
	 * 
	 * Called from {@link AunisBlocks#onRegisterBlocks(net.minecraftforge.event.RegistryEvent.Register)}
	 */
	public void registerStargateBaseTile();
	
	/**
	 * Create appropriate tile entity.
	 * 
	 * @return {@link TileEntity}
	 */
	public StargateBaseTileSG1 createStargateBaseTile();
}
