package mrjake.aunis.integration.opencomputers;

import mrjake.aunis.tileentity.stargate.StargateBaseTileSG1;

public class OCWrapperLoaded implements OCWrapperInterface {
	public OCWrapperLoaded() {}
	
	@Override
	public void registerStargateBaseTile() {
//		GameRegistry.registerTileEntity(StargateBaseTileOC.class, AunisBlocks.stargateBaseBlock.getRegistryName());
	}

	@Override
	public StargateBaseTileSG1 createStargateBaseTile() {
		return null;//new StargateBaseTileOC();
	}

}
