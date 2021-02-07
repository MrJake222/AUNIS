package mrjake.aunis.state;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.beamer.BeamerStatusEnum;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class BeamerRendererUpdate extends State {
	public BeamerRendererUpdate() {}
	
	public BeamerStatusEnum beamerStatus;

	public BeamerRendererUpdate(BeamerStatusEnum beamerStatus) {
		this.beamerStatus = beamerStatus;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(beamerStatus.getKey());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		beamerStatus = BeamerStatusEnum.valueOf(buf.readInt());
	}

}
