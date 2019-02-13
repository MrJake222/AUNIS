package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;

public class CrystalInfuserRendererState extends RendererState {

	public CrystalInfuserRendererState(BlockPos pos) {
		super(pos);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void toBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getKeyName() {
		return "rendererState";
	}

}
