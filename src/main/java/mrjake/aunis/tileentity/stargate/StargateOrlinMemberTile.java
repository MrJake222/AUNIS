package mrjake.aunis.tileentity.stargate;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateOrlinMemberTile extends TileEntity {
	
	// ---------------------------------------------------------------------------------
	// Base position
	
	private BlockPos basePos;
	
	public boolean isMerged() {
		return basePos != null;
	}
	
	@Nullable
	public BlockPos getBasePos() {
		return basePos;
	}
	
	@Nullable
	public StargateOrlinBaseTile getBaseTile(World world) {
		if (basePos != null)
			return (StargateOrlinBaseTile) world.getTileEntity(basePos);
		
		return null;
	}
	
	public void setBasePos(BlockPos basePos) {
		this.basePos = basePos;
		
		markDirty();
	}
	
	// ---------------------------------------------------------------------------------
	// Broken state
	
	private boolean broken = false;
	
	public boolean isBroken() {
		return broken;
	}
	
	public void setBroken(boolean broken) {
		this.broken = broken;
		markDirty();
	}
	
	// ---------------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {		
		if (basePos != null)
			compound.setLong("basePos", basePos.toLong());
		
		compound.setBoolean("broken", broken);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {				
		if (compound.hasKey("basePos"))
			basePos = BlockPos.fromLong(compound.getLong("basePos"));
		
		broken = compound.getBoolean("broken");
		
		super.readFromNBT(compound);
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
}
