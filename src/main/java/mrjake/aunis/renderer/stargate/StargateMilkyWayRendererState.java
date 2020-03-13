package mrjake.aunis.renderer.stargate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateSpinHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class StargateMilkyWayRendererState extends StargateAbstractRendererState {
	public StargateMilkyWayRendererState() {}
	
	public StargateMilkyWayRendererState(StargateSizeEnum stargateSize, EnumStargateState stargateState, int activeChevrons, boolean isFinalActive, EnumSymbol currentRingSymbol, EnumSpinDirection spinDirection, boolean isSpinning, EnumSymbol targetRingSymbol, long spinStartTime) {
		super(stargateState);
		
		this.stargateSize = stargateSize;
		this.chevronTextureList = new ChevronTextureList(activeChevrons, isFinalActive);
		this.spinHelper = new StargateSpinHelper(currentRingSymbol, spinDirection, isSpinning, targetRingSymbol, spinStartTime);
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
	// Saved
	public ChevronTextureList chevronTextureList;
	// Not saved
	public boolean chevronOpen;
	public long chevronActionStart;
	public boolean chevronOpening;
	public boolean chevronClosing;
	
	public void openChevron(long totalWorldTime) {
		chevronActionStart = totalWorldTime;
		chevronOpening = true;
	}
	
	public void closeChevron(long totalWorldTime) {
		chevronActionStart = totalWorldTime;
		chevronClosing = true;
	}
	
	// Ring		
	// Saved
	public StargateSpinHelper spinHelper;
		
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(stargateSize.id);
		chevronTextureList.toBytes(buf);
		spinHelper.toBytes(buf);
		
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {	
		stargateSize = StargateSizeEnum.fromId(buf.readInt());
		
		chevronTextureList = new ChevronTextureList();
		chevronTextureList.fromBytes(buf);
		
		spinHelper = new StargateSpinHelper();
		spinHelper.fromBytes(buf);
				
		super.fromBytes(buf);
	}
}