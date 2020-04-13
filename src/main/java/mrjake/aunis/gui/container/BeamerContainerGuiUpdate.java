package mrjake.aunis.gui.container;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.beamer.BeamerRoleEnum;
import mrjake.aunis.state.State;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BeamerContainerGuiUpdate extends State {
	public BeamerContainerGuiUpdate() {}
	
	public int energyStored;
	public int transferredLastTick;
	public FluidStack fluidStack;
	public BeamerRoleEnum beamerRole;

	public BeamerContainerGuiUpdate(int energyStored, int transferedLastTick, FluidStack fluidStack, BeamerRoleEnum beamerRole) {
		this.energyStored = energyStored;
		this.transferredLastTick = transferedLastTick;
		this.fluidStack = fluidStack;
		this.beamerRole = beamerRole;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energyStored);
		buf.writeInt(transferredLastTick);
		buf.writeInt(beamerRole.id);
		
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
		
		if (buf.readBoolean()) {
			int size = buf.readInt();
			fluidStack = FluidRegistry.getFluidStack(buf.readCharSequence(size, StandardCharsets.UTF_8).toString(), buf.readInt());
		}
	}
}
