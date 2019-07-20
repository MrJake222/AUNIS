package mrjake.aunis.integration.opencomputers;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.StargateBaseTileOC;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class OCWrapperLoaded implements OCWrapperInterface {
	public OCWrapperLoaded() {}
	
	@Override
	public void registerStargateBaseTile() {
		GameRegistry.registerTileEntity(StargateBaseTileOC.class, AunisBlocks.stargateBaseBlock.getRegistryName());
	}

	@Override
	public StargateBaseTile createStargateBaseTile() {
		return new StargateBaseTileOC();
	}

}
