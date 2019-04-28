package mrjake.aunis.tileentity;

import mrjake.aunis.AunisProps;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.packet.state.StateUpdateRequestToServer;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.state.CamoState;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.ITileEntityStateProvider;
import mrjake.aunis.state.LightState;
import mrjake.aunis.state.State;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * TileEntity for ring blocks and chevron blocks
 * 
 * Holds the camouflage block in {@link ItemStackHandler}, providing it into {@link Block#getExtendedState()}
 * 
 * @author MrJake
 */
public class StargateMemberTile extends TileEntity implements ITickable, ITileEntityStateProvider {
	
	boolean firstTick = true;
	private boolean waitForClear = false;
	private long clearWaitStarted;
	
	@Override
	public void update() {

		if (firstTick) {
			firstTick = false;
			
			if (world.isRemote) {				
				AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, EnumStateType.CAMO_STATE));
				AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, EnumStateType.LIGHT_STATE));
			}
		}
		
		
		if (!world.isRemote) {
			if (waitForClear) {
				if (world.getTotalWorldTime() - clearWaitStarted >= 40) {
					waitForClear = false;
					
					syncLightUp();
				}
			}
		}
	}
	
	// ---------------------------------------------------------------------------------
	
	/**
	 * Is chevron block emitting light
	 */
	private boolean isLitUp;
	
	public void syncLightUp() {
		TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, EnumStateType.LIGHT_STATE, getState(EnumStateType.LIGHT_STATE)), point);
	}
	
	public void setLitUp(boolean isLitUp) {
		this.isLitUp = isLitUp;

		
		if (isLitUp)
			syncLightUp();
		
		else {
			clearWaitStarted = world.getTotalWorldTime();
			waitForClear = true;
		}
		
//		world.notifyLightSet(pos);
//		world.setBlockState(pos, world.getBlockState(pos).withProperty(AunisProps.LIT_UP, isLitUp), 3);
		
		markDirty();
	}
	
	public boolean isLitUp(IBlockState state) {
		return state.getValue(AunisProps.MEMBER_VARIANT) == EnumMemberVariant.CHEVRON && isLitUp;
	}
	
	
	// ---------------------------------------------------------------------------------
	/**
	 * {@link ItemStackHandler} for storing block that will be placed as a camouflage
	 */
	private ItemStackHandler itemStackHandler = new ItemStackHandler(1) {
		protected void onContentsChanged(int slot) {
			markDirty();
		}
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null)
			return (T) itemStackHandler;
		
		return super.getCapability(capability, facing);
	}
	
	
	// ---------------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", itemStackHandler.serializeNBT());
		compound.setBoolean("isLitUp", isLitUp);		
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {		
		if (compound.hasKey("inventory"))
			itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("inventory"));
		
		isLitUp = compound.getBoolean("isLitUp");
		
		super.readFromNBT(compound);
	}
	
	
	// ---------------------------------------------------------------------------------
	// States
	
	@Override
	public State getState(EnumStateType stateType) {
		switch (stateType) {
			case CAMO_STATE:				
				return new CamoState(itemStackHandler.getStackInSlot(0));
				
			case LIGHT_STATE:
				return new LightState(isLitUp);
				
			default:
				return null;
		}		
	}
	@Override
	public State createState(EnumStateType stateType) {
		switch (stateType) {
			case CAMO_STATE:
				return new CamoState();
				
			case LIGHT_STATE:
				return new LightState();
				
			default:
				return null;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(EnumStateType stateType, State state) {
		switch (stateType) {
			case CAMO_STATE:
				CamoState memberState = (CamoState) state;
				
				ItemStack stack = memberState.getItemStack();
				itemStackHandler.setStackInSlot(0, stack);
				
				world.markBlockRangeForRenderUpdate(pos, pos);
				break;
				
			case LIGHT_STATE:
				isLitUp = ((LightState) state).isLitUp();
				world.notifyLightSet(pos);
				world.checkLightFor(EnumSkyBlock.BLOCK, pos);
				
				break;
				
			default:
				break;
		}
	}	
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
}
