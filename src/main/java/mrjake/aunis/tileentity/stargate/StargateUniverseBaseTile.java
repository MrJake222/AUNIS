package mrjake.aunis.tileentity.stargate;

import java.util.List;

import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.renderer.stargate.StargateUniverseRendererState;
import mrjake.aunis.renderer.stargate.StargateUniverseRendererState.StargateUniverseRendererStateBuilder;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.sound.StargateSoundEventEnum;
import mrjake.aunis.sound.StargateSoundPositionedEnum;
import mrjake.aunis.stargate.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.StargateMilkyWayMergeHelper;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.util.AunisAxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class StargateUniverseBaseTile extends StargateClassicBaseTile {

	@Override
	public SymbolTypeEnum getSymbolType() {
		return SymbolTypeEnum.MILKYWAY;
	}
	
	@Override
	protected AunisAxisAlignedBB getHorizonTeleportBox(boolean server) {
		return StargateSizeEnum.SMALL.teleportBox;
	}

	@Override
	protected SoundPositionedEnum getPositionedSound(StargateSoundPositionedEnum soundEnum) {
		// TODO Auto-generated method stub UNIVERSE
		return null;
	}

	@Override
	protected SoundEventEnum getSoundEvent(StargateSoundEventEnum soundEnum) {
		// TODO Auto-generated method stub UNIVERSE
		return null;
	}

	@Override
	public BlockPos getGateCenterPos() {
		return pos.up(4);
	}

	@Override
	protected AunisAxisAlignedBB getHorizonKillingBox(boolean server) {
		return StargateSizeEnum.SMALL.killingBox;
	}

	@Override
	protected int getHorizonSegmentCount(boolean server) {
		return StargateSizeEnum.SMALL.horizonSegmentCount;
	}

	@Override
	protected List<AunisAxisAlignedBB> getGateVaporizingBoxes(boolean server) {
		return StargateSizeEnum.SMALL.gateVaporizingBoxes;
	}

	@Override
	protected void mergeGate() {
		// TODO Auto-generated method stub UNIVERSE
		
	}

	@Override
	protected void unmergeGate() {
		// TODO Auto-generated method stub UNIVERSE
		
	}

	@Override
	protected StargateAbstractMergeHelper getMergeHelper() {
		return StargateMilkyWayMergeHelper.INSTANCE;
	}

	@Override
	protected StargateUniverseRendererStateBuilder getRendererStateServer() {
		return new StargateUniverseRendererStateBuilder(super.getRendererStateServer());
	}
	
	@Override
	protected StargateUniverseRendererState createRendererStateClient() {
		return new StargateUniverseRendererState();
	}
	
	@Override
	public StargateUniverseRendererState getRendererStateClient() {
		return (StargateUniverseRendererState) super.getRendererStateClient();
	}
}
