package mrjake.aunis.tileentity.stargate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class StargateAbstractMemberTile extends TileEntity {

    // ---------------------------------------------------------------------------------
    // Base position

    protected BlockPos basePos;

    public boolean isMerged() {
        return basePos != null;
    }

    @Nullable
    public BlockPos getBasePos() {
        return basePos;
    }

    @Nullable
    public StargateAbstractBaseTile getBaseTile(World world) {
        if (basePos != null)
            return (StargateAbstractBaseTile) world.getTileEntity(basePos);

        return null;
    }

    public void setBasePos(BlockPos basePos) {
        this.basePos = basePos;

        markDirty();
    }


    // ---------------------------------------------------------------------------------
    // NBT

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (basePos != null)
            compound.setLong("basePos", basePos.toLong());

        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("basePos"))
            basePos = BlockPos.fromLong(compound.getLong("basePos"));

        super.readFromNBT(compound);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
