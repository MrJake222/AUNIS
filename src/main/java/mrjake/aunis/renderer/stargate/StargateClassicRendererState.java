package mrjake.aunis.renderer.stargate;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class StargateClassicRendererState extends StargateAbstractRendererState {
	public StargateClassicRendererState() {}
	
	public StargateClassicRendererState(StargateClassicRendererStateBuilder builder) {
		super(builder);
		
		this.chevronTextureList = new ChevronTextureList(builder.activeChevrons, builder.isFinalActive);
	}
	
	@Override
	public StargateAbstractRendererState initClient(BlockPos pos, EnumFacing facing) {
		chevronTextureList.initClient();
		
		return super.initClient(pos, facing);
	}
	
	// Chevrons
	// Saved
	public ChevronTextureList chevronTextureList;
	
	// ------------------------------------------------------------------------
	// Saving
	
	@Override
	public void toBytes(ByteBuf buf) {
		chevronTextureList.toBytes(buf);
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {			
		chevronTextureList = new ChevronTextureList();
		chevronTextureList.fromBytes(buf);
				
		super.fromBytes(buf);
	}
	
	
	// ------------------------------------------------------------------------
	// Builder
	
	public static StargateClassicRendererStateBuilder builder() {
		return new StargateClassicRendererStateBuilder();
	}
	
	public static class StargateClassicRendererStateBuilder extends StargateAbstractRendererStateBuilder {
		
		// Chevrons
		private int activeChevrons;
		private boolean isFinalActive;
		
		public StargateClassicRendererStateBuilder setActiveChevrons(int activeChevrons) {
			this.activeChevrons = activeChevrons;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setFinalActive(boolean isFinalActive) {
			this.isFinalActive = isFinalActive;
			return this;
		}
		
		@Override
		public StargateClassicRendererState build() {
			return new StargateClassicRendererState(this);
		}
	}
}
