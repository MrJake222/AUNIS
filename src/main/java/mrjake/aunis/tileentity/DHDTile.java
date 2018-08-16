package mrjake.aunis.tileentity;

import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.DHDRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDTile extends TileEntity {
	
	private DHDRenderer renderer;
	private BlockPos linkedGate = null;
		
	public void establishLinkToStargate(BlockPos gate) {
		Aunis.log("Linking to gate at " + gate.toString());
		
		this.linkedGate = gate;
		markDirty();
	}
	
	public BlockPos getLinkedGate() {
		return linkedGate;
	}
	
	public StargateBaseTile getLinkedGate(World world) {
		if (linkedGate == null)
			return null;
		else
			return (StargateBaseTile) world.getTileEntity(linkedGate);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		BlockPos gate;
		
		if (linkedGate == null) {
			Aunis.log("linkedGate is null!");
			gate = new BlockPos(0,0,0);
		}
		else
			gate = linkedGate;
		
		compound.setInteger("linkedGateX", gate.getX());
		compound.setInteger("linkedGateY", gate.getY());
		compound.setInteger("linkedGateZ", gate.getZ());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		int x = compound.getInteger("linkedGateX");
		int y = compound.getInteger("linkedGateY");
		int z = compound.getInteger("linkedGateZ");
		
		BlockPos pos = new BlockPos(x,y,z);

		Aunis.log("Relinking to Stargate at " + pos.toString());
		linkedGate = pos;
	}
	
	public DHDRenderer getRenderer() {
		if (renderer == null)
			renderer = new DHDRenderer(this);
		
		return renderer;
	}
    @Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}
}
