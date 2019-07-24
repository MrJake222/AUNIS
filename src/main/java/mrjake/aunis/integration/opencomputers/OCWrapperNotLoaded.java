package mrjake.aunis.integration.opencomputers;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.tileentity.stargate.StargateBaseTileSG1;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class OCWrapperNotLoaded implements OCWrapperInterface {
	public OCWrapperNotLoaded() {}
	
	@Override
	public void registerStargateBaseTile() {
		GameRegistry.registerTileEntity(StargateBaseTileSG1.class, AunisBlocks.stargateBaseBlock.getRegistryName());
	}

	@Override
	public StargateBaseTileSG1 createStargateBaseTile() {
		return new StargateBaseTileSG1();
	}

}
