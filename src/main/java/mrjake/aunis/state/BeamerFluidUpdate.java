package mrjake.aunis.state;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class BeamerFluidUpdate extends State {
	public BeamerFluidUpdate() {}
	
	public Fluid fluidContained;

	public BeamerFluidUpdate(Fluid fluid) {
		fluidContained = fluid;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(fluidContained != null);
		
		if (fluidContained != null) { 
			String string = FluidRegistry.getFluidName(fluidContained);
			buf.writeInt(string.length());
			buf.writeCharSequence(string, StandardCharsets.UTF_8);
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {		
		if (buf.readBoolean()) {
			// Had fluid stack
			
			int size = buf.readInt();
			fluidContained = FluidRegistry.getFluid(buf.readCharSequence(size, StandardCharsets.UTF_8).toString());
		}
	}

}
