package mrjake.aunis.renderer.stargate;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class StargateClassicRendererState extends StargateAbstractRendererState {
	public StargateClassicRendererState() {}
	
	public StargateClassicRendererState(StargateClassicRendererStateBuilder builder) {
		super(builder);
		
		this.chevronTextureList = new ChevronTextureList(getChevronTextureBase(), builder.activeChevrons, builder.isFinalActive);
	}
	
	@Override
	public StargateAbstractRendererState initClient(BlockPos pos, EnumFacing facing) {
		chevronTextureList.initClient();
		
		return super.initClient(pos, facing);
	}
	
	protected abstract String getChevronTextureBase();
	
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
		chevronTextureList = new ChevronTextureList(getChevronTextureBase());
		chevronTextureList.fromBytes(buf);
				
		super.fromBytes(buf);
	}
	
	
	// ------------------------------------------------------------------------
	// Builder
	
	public static StargateClassicRendererStateBuilder builder() {
		return new StargateClassicRendererStateBuilder();
	}
	
	public static class StargateClassicRendererStateBuilder extends StargateAbstractRendererStateBuilder {
		public StargateClassicRendererStateBuilder() {}
		
		// Chevrons
		protected int activeChevrons;
		protected boolean isFinalActive;
		
		public StargateClassicRendererStateBuilder(StargateAbstractRendererStateBuilder superBuilder) {
			setStargateState(superBuilder.stargateState);
		}
		
		public StargateClassicRendererStateBuilder setActiveChevrons(int activeChevrons) {
			this.activeChevrons = activeChevrons;
			return this;
		}
		
		public StargateClassicRendererStateBuilder setFinalActive(boolean isFinalActive) {
			this.isFinalActive = isFinalActive;
			return this;
		}
	}
}
