package mrjake.aunis.tileentity.stargate;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.stargate.EnumMemberVariant;
import mrjake.aunis.state.StargateCamoState;
import mrjake.aunis.state.StargateLightState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

/**
 * TileEntity for ring blocks and chevron blocks
 * 
 * Holds the camouflage block in {@link ItemStackHandler}, providing it into {@link Block#getExtendedState()}
 * 
 * @author MrJake
 */
public abstract class StargateClassicMemberTile extends StargateAbstractMemberTile implements StateProviderInterface {
	
	private TargetPoint targetPoint;
	
	@Override
	public void onLoad() {
		if (!world.isRemote) {
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.CAMO_STATE));
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.LIGHT_STATE));
		}
	}
	
	@Override
	public void rotate(Rotation rotation) {
		IBlockState state = world.getBlockState(pos);
		
		EnumFacing facing = state.getValue(AunisProps.FACING_HORIZONTAL);
		world.setBlockState(pos, state.withProperty(AunisProps.FACING_HORIZONTAL, rotation.rotate(facing)));
	}
	
	// ---------------------------------------------------------------------------------
	
	/**
	 * Is chevron block emitting light
	 */
	private boolean isLitUp;
	
	public void setLitUp(boolean isLitUp) {
		boolean sync = isLitUp != this.isLitUp;
		
		this.isLitUp = isLitUp;
		markDirty();

		if (sync) {
			sendState(StateTypeEnum.LIGHT_STATE, getState(StateTypeEnum.LIGHT_STATE));
		}
	}
	
	public boolean isLitUp(IBlockState state) {
		return state.getValue(AunisProps.MEMBER_VARIANT) == EnumMemberVariant.CHEVRON && isLitUp;
	}
	
	
	// ---------------------------------------------------------------------------------	
	private IBlockState camoBlockState;
	
	/**
	 * Should only be called from server. Updates camoBlockState and
	 * syncs the change to clients.
	 * 
	 * @param camoBlockState Camouflage block state.
	 */
	public void setCamoState(IBlockState camoBlockState) {
		// Aunis.logger.debug("Setting camo for " + pos + " to " + camoBlockState);
		
		this.camoBlockState = camoBlockState;
		markDirty();
		
		if (!world.isRemote) {
			sendState(StateTypeEnum.CAMO_STATE, getState(StateTypeEnum.CAMO_STATE));
		}
		
		else {
			Aunis.logger.warn("Tried to set camoBlockState from client. This won't work!");
		}
	}
	
	public IBlockState getCamoState() {
		return camoBlockState;
	}
	
	public ItemStack getCamoItemStack() {
		if (camoBlockState != null) {
			Block block = camoBlockState.getBlock();
			
			if (block == Blocks.SNOW_LAYER)
				return null;
			
			int quantity = 1;
			int meta;
			
			if (block instanceof BlockSlab && ((BlockSlab) block).isDouble()) {
				quantity = 2;
				meta = block.getMetaFromState(camoBlockState);
				
				if (block == Blocks.DOUBLE_STONE_SLAB)
					block = Blocks.STONE_SLAB;
				
				else if (block == Blocks.DOUBLE_STONE_SLAB2)
					block = Blocks.STONE_SLAB2;
				
				else if (block == Blocks.DOUBLE_WOODEN_SLAB)
					block = Blocks.WOODEN_SLAB;
				
				else if (block == Blocks.PURPUR_DOUBLE_SLAB)
					block = Blocks.PURPUR_SLAB;
			}
			
			else {
				meta = block.getMetaFromState(camoBlockState);
			}
			
			return new ItemStack(block, quantity, meta);
		}
		
		else {
			return null;
		}
	}

	// ---------------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("isLitUp", isLitUp);
		
		if (camoBlockState != null) {
			compound.setString("doubleSlabBlock", camoBlockState.getBlock().getRegistryName().toString());
			compound.setInteger("doubleSlabMeta", camoBlockState.getBlock().getMetaFromState(camoBlockState));
		}
		
		return super.writeToNBT(compound);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void readFromNBT(NBTTagCompound compound) {		
		isLitUp = compound.getBoolean("isLitUp");
		
		if (compound.hasKey("doubleSlabBlock")) {
			Block dblSlabBlock = Block.getBlockFromName(compound.getString("doubleSlabBlock"));
			camoBlockState = dblSlabBlock.getStateFromMeta(compound.getInteger("doubleSlabMeta"));
		}
		
		super.readFromNBT(compound);
	}
	
	
	// ---------------------------------------------------------------------------------
	// States
	
	protected void sendState(StateTypeEnum type, State state) {
		if (world.isRemote)
			return;
		
		if (targetPoint != null) {
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, type, state), targetPoint);
		}
		
		else {
			Aunis.logger.debug("targetPoint was null trying to send " + type + " from " + this.getClass().getCanonicalName());
		}
	}
	
	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case CAMO_STATE:				
				return new StargateCamoState(camoBlockState);
				
			case LIGHT_STATE:
				return new StargateLightState(isLitUp);
				
			default:
				return null;
		}		
	}
	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case CAMO_STATE:
				return new StargateCamoState();
				
			case LIGHT_STATE:
				return new StargateLightState();
				
			default:
				return null;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case CAMO_STATE:
				StargateCamoState memberState = (StargateCamoState) state;				
				camoBlockState = memberState.getState();
				
				world.markBlockRangeForRenderUpdate(pos, pos);
				break;
				
			case LIGHT_STATE:
				isLitUp = ((StargateLightState) state).isLitUp();
				world.notifyLightSet(pos);
				world.checkLightFor(EnumSkyBlock.BLOCK, pos);
				
				break;
				
			default:
				break;
		}
	}
}
