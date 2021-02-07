package mrjake.aunis.state;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.beamer.BeamerStatusEnum;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class BeamerRendererUpdate extends State {
	public BeamerRendererUpdate() {}
	
	public BeamerStatusEnum beamerStatus;
	public Fluid fluidContained;

	public BeamerRendererUpdate(BeamerStatusEnum beamerStatus, Fluid fluid) {
		this.beamerStatus = beamerStatus;
		fluidContained = fluid;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(beamerStatus.getKey());
		buf.writeBoolean(fluidContained != null);
		
		if (fluidContained != null) { 
			String string = FluidRegistry.getFluidName(fluidContained);
			buf.writeInt(string.length());
			buf.writeCharSequence(string, StandardCharsets.UTF_8);
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		beamerStatus = BeamerStatusEnum.valueOf(buf.readInt());
		
		if (buf.readBoolean()) {
			// Had fluid stack
			
			int size = buf.readInt();
			fluidContained = FluidRegistry.getFluid(buf.readCharSequence(size, StandardCharsets.UTF_8).toString());
		}
	}

}
