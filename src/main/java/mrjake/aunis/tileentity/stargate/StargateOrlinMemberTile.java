package mrjake.aunis.tileentity.stargate;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
	
	private int openCount = 0;
	
	public boolean isBroken() {
		return openCount == AunisConfig.stargateConfig.stargateOrlinMaxOpenCount;
	}
	
	public void incrementOpenCount() {
		openCount++;
		markDirty();
	}
	
	public int getOpenCount() {
		return openCount;
	}
	
	public void addDrops(List<ItemStack> drops) {
		
		if (isBroken()) {
			Random rand = new Random();
			
			drops.add(new ItemStack(Items.IRON_INGOT, 1 + rand.nextInt(2)));
			drops.add(new ItemStack(Items.REDSTONE, 2 + rand.nextInt(3)));
		}
			
		else {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("openCount", openCount);
			
			ItemStack stack = new ItemStack(Item.getItemFromBlock(AunisBlocks.STARGATE_ORLIN_MEMBER_BLOCK));
			stack.setTagCompound(compound);
			
			drops.add(stack);
		}
	}
	
	public void initializeFromItemStack(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound compound = stack.getTagCompound();
			
			if (compound.hasKey("openCount")) {
				openCount = compound.getInteger("openCount");
			}
		}
	}
	
	// ---------------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {		
		if (basePos != null)
			compound.setLong("basePos", basePos.toLong());
		
		compound.setInteger("openCount", openCount);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {				
		if (compound.hasKey("basePos"))
			basePos = BlockPos.fromLong(compound.getLong("basePos"));
		
		openCount = compound.getInteger("openCount");
		
		super.readFromNBT(compound);
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
}
