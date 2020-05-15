package mrjake.aunis.renderer.stargate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.loader.texture.TextureLoader;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.StargateActivation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ChevronTextureList {
	
//	private String chevronTextureBase;
	
	// Saved
	private List<ChevronEnum> activeChevrons = new ArrayList<>(9);
//	private int activeChevrons;
//	private boolean isFinalActive;
	
	// Not saved
	private Map<ChevronEnum, Integer> CHEVRON_STATE_MAP = new HashMap<>(9);
	private List<Activation<ChevronEnum>> activationList = new ArrayList<>();
	
	private final Map<Integer, ResourceLocation> CHEVRON_RESOURCE_MAP = new HashMap<>();
	
	public ChevronTextureList(String chevronTextureBase) {		
		for (int i=0; i<=10; i++) {
			CHEVRON_RESOURCE_MAP.put(i, TextureLoader.getTextureResource(chevronTextureBase + i + ".jpg"));
		}
	}
	
	public ChevronTextureList(String chevronTextureBase, int activeChevrons, boolean isFinalActive) {
		this(chevronTextureBase);
		
		if (isFinalActive)
			activeChevrons--;
		
		for (int i=0; i<activeChevrons; i++)
			this.activeChevrons.add(ChevronEnum.valueOf(i));
		
		if (isFinalActive)
			this.activeChevrons.add(ChevronEnum.getFinal());
	}	
	
	public void initClient() {
		for (ChevronEnum chevron : ChevronEnum.values()) {
			CHEVRON_STATE_MAP.put(chevron, activeChevrons.contains(chevron) ? 10 : 0);
		}
	}

	private ChevronEnum getNextChevron() {
		if (activeChevrons.size() > 0)
			return activeChevrons.get(activeChevrons.size()-1).getNext();
		
		return ChevronEnum.C1;
	}
	
	public void activateNextChevron(long totalWorldTime) {
		ChevronEnum next = getNextChevron();
		
		activationList.add(new StargateActivation(next, totalWorldTime, false));
		activeChevrons.add(next);
	}
	
	public void activateFinalChevron(long totalWorldTime) {
		activationList.add(new StargateActivation(ChevronEnum.getFinal(), totalWorldTime, false));
		activeChevrons.add(ChevronEnum.getFinal());
	}
	
	public void deactivateFinalChevron(long totalWorldTime) {
		activationList.add(new StargateActivation(ChevronEnum.getFinal(), totalWorldTime, true));
		activeChevrons.remove(ChevronEnum.getFinal());
	}
	
	public void clearChevrons(long totalWorldTime) {
		for (ChevronEnum chevron : activeChevrons) {
			activationList.add(new StargateActivation(chevron, totalWorldTime, true));
		}
		
		activeChevrons.clear();
	}
	
	public void lightUpChevrons(long totalWorldTime, int incomingAddressSize) {	
		for (ChevronEnum chevron : Arrays.asList(ChevronEnum.C7, ChevronEnum.C8)) {
			if (activeChevrons.contains(chevron) && chevron.index >= incomingAddressSize-1) {
				activationList.add(new StargateActivation(chevron, totalWorldTime, true));
			}
		}
		
		activeChevrons.clear();
		
		while (activeChevrons.size() < incomingAddressSize-1) {
			activateNextChevron(totalWorldTime);
		}
		
		activateFinalChevron(totalWorldTime);
	}
	
	public void iterate(World world, double partialTicks) {
		Activation.iterate(activationList, world.getTotalWorldTime(), partialTicks, (index, stage) -> {
			CHEVRON_STATE_MAP.put(index, Math.round(stage));
		});
	}
	
	public ResourceLocation get(ChevronEnum chevron) {
		return CHEVRON_RESOURCE_MAP.get(CHEVRON_STATE_MAP.get(chevron));
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeInt(activeChevrons.size());
		
		for (ChevronEnum chevron : activeChevrons) {
			buf.writeInt(chevron.index);
		}
	}
	
	public void fromBytes(ByteBuf buf) {
		int size = buf.readInt();
		activeChevrons.clear();
		
		for (int i=0; i<size; i++) {
			activeChevrons.add(ChevronEnum.valueOf(buf.readInt()));
		}
	}
}
