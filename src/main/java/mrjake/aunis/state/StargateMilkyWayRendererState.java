package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.renderer.stargate.ChevronTextureList;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class StargateMilkyWayRendererState extends StargateAbstractRendererState {
	public StargateMilkyWayRendererState() {}
	
	public StargateMilkyWayRendererState(StargateSizeEnum stargateSize, EnumStargateState stargateState, int activeChevrons, boolean isFinalActive, EnumSymbol currentSymbol) {
		super(stargateState);
		
		this.stargateSize = stargateSize;
		this.chevronTextureList = new ChevronTextureList(activeChevrons, isFinalActive);
		this.ringCurrentSymbol = currentSymbol;
	}
	
	@Override
	public StargateAbstractRendererState initClient(BlockPos pos, EnumFacing facing) {
		chevronTextureList.initClient();
		
		return super.initClient(pos, facing);
	}
		
	// Gate
	// Saved
	public StargateSizeEnum stargateSize = AunisConfig.stargateSize;
	
	// Chevrons
	public ChevronTextureList chevronTextureList;
	
	// Ring		
	// Saved
	public EnumSymbol ringCurrentSymbol = EnumSymbol.ORIGIN;
		
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(stargateSize.id);
		chevronTextureList.toBytes(buf);
		buf.writeInt(ringCurrentSymbol != null ? ringCurrentSymbol.id : EnumSymbol.ORIGIN.id);
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {	
		stargateSize = StargateSizeEnum.fromId(buf.readInt());
		
		chevronTextureList = new ChevronTextureList();
		chevronTextureList.fromBytes(buf);
		ringCurrentSymbol = EnumSymbol.valueOf(buf.readInt());
				
		super.fromBytes(buf);
	}
}