package mrjake.aunis.integration.opencomputers;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class OCWrapperNotLoaded implements OCWrapperInterface {
	public OCWrapperNotLoaded() {}
	
	@Override
	public void registerStargateBaseTile() {
		GameRegistry.registerTileEntity(StargateBaseTile.class, AunisBlocks.stargateBaseBlock.getRegistryName());
	}

	@Override
	public StargateBaseTile createStargateBaseTile() {
		return new StargateBaseTile();
	}

}
