package mrjake.aunis.datafixer;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.IFixableData;

/**
 * {@link TileEntity} data fixer for transition of block names
 * between AUNIS 1.5 and 1.6.
 * Data version 5 to 6.
 * 
 * @author MrJake222
 *
 */
public class TileNamesFixer implements IFixableData {
	@Override
	public int getFixVersion() {
		return 6;
	}

	@Override
	public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
		Block newBlock = AunisBlocks.remapBlock(compound.getString("id"));
		
		if (newBlock != null) {
			Aunis.logger.debug("Fixing block id " + compound.getString("id") + ", now: " + newBlock.getRegistryName().toString());
			
			compound.setString("id", newBlock.getRegistryName().toString());
			compound.setInteger("DataVersion", getFixVersion());
		}
		
		return compound;
	}
}
