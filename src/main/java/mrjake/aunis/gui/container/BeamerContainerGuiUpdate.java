package mrjake.aunis.gui.container;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.beamer.BeamerRoleEnum;
import mrjake.aunis.state.State;
import mrjake.aunis.tileentity.util.RedstoneModeEnum;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BeamerContainerGuiUpdate extends State {
	public BeamerContainerGuiUpdate() {}
	
	public int energyStored;
	public int transferredLastTick;
	public FluidStack fluidStack;
	public BeamerRoleEnum beamerRole;
	public RedstoneModeEnum mode;
	public int start;
	public int stop;
	public int inactivity;

	public BeamerContainerGuiUpdate(int energyStored, int transferedLastTick, FluidStack fluidStack, BeamerRoleEnum beamerRole, RedstoneModeEnum redstoneMode, int start, int stop, int inactivity) {
		this.energyStored = energyStored;
		this.transferredLastTick = transferedLastTick;
		this.fluidStack = fluidStack;
		this.beamerRole = beamerRole;
		this.mode = redstoneMode;
		this.start = start;
		this.stop = stop;
		this.inactivity = inactivity;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energyStored);
		buf.writeInt(transferredLastTick);
		buf.writeInt(beamerRole.id);
		buf.writeInt(mode.getKey());
		buf.writeInt(start);
		buf.writeInt(stop);
		buf.writeInt(inactivity);
		
		if (fluidStack != null) {
			buf.writeBoolean(true);
			
			String name = FluidRegistry.getFluidName(fluidStack);
			buf.writeInt(name.length());
			buf.writeCharSequence(name, StandardCharsets.UTF_8);
			buf.writeInt(fluidStack.amount);
		}
		
		else {
			buf.writeBoolean(false);
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		energyStored = buf.readInt();
		transferredLastTick = buf.readInt();
		beamerRole = BeamerRoleEnum.valueOf(buf.readInt());
		mode = RedstoneModeEnum.valueOf(buf.readInt());
		start = buf.readInt();
		stop = buf.readInt();
		inactivity = buf.readInt();
		
		if (buf.readBoolean()) {
			int size = buf.readInt();
			fluidStack = FluidRegistry.getFluidStack(buf.readCharSequence(size, StandardCharsets.UTF_8).toString(), buf.readInt());
		}
	}
}
