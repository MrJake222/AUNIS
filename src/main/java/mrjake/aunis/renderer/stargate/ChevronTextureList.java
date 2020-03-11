package mrjake.aunis.renderer.stargate;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.activation.Activation;
import mrjake.aunis.renderer.activation.StargateActivation;
import net.minecraft.world.World;

public class ChevronTextureList {
	public ChevronTextureList() {}
	
	private static final String CHEVRON_TEXTURE_BASE = "stargate/chevron/chevron";

	// Saved
	private int activeChevrons;
	private boolean isFinalActive;
	
	// Not saved
	private List<String> chevronTextureList = new ArrayList<String>(9);
	private List<Activation> activationList = new ArrayList<>();
	
	public ChevronTextureList(int activeChevrons, boolean isFinalActive) {
		this.activeChevrons = activeChevrons;
		this.isFinalActive = isFinalActive;
	}	
	
	public void initClient() {
		for (int i=0; i<9; i++) {
			chevronTextureList.add(CHEVRON_TEXTURE_BASE + (isChevronActive(i) ? "10.png" : "0.png"));
		}
	}

	private boolean isChevronActive(int index) {
		if (index == 8)
			return isFinalActive;
		
		return index < (isFinalActive ? activeChevrons-1 : activeChevrons);
	}
	
	public void activateNextChevron(long totalWorldTime) {
		activationList.add(new StargateActivation(activeChevrons, totalWorldTime));
		activeChevrons++;
	}
	
	public void activateFinalChevron(long totalWorldTime) {
		activationList.add(new StargateActivation(8, totalWorldTime));
		activeChevrons++;
		isFinalActive = true;
	}
	
	public void deactivateFinalChevron(long totalWorldTime) {
		activationList.add(new StargateActivation(8, totalWorldTime, true));
		activeChevrons--;
		isFinalActive = false;
	}
	
	public void clearChevrons(long totalWorldTime) {
		for (int i=0; i<9; i++) {
			if (isChevronActive(i)) {
				activationList.add(new StargateActivation(i, totalWorldTime, true));
			}
		}
		
		activeChevrons = 0;
		isFinalActive = false;
	}
	
	public void lightUpChevrons(long totalWorldTime, int incomingAddressSize) {
		for (int i=0; i<9; i++) {
			if (!isChevronActive(i) && i < incomingAddressSize-1) {
				activationList.add(new StargateActivation(i, totalWorldTime));
			}
			
			activationList.add(new StargateActivation(8, totalWorldTime));
		}
		
		activeChevrons = incomingAddressSize;
		isFinalActive = true;
	}
	
	public void iterate(World world, double partialTicks) {
		Activation.iterate(activationList, world.getTotalWorldTime(), partialTicks, (index, stage) -> {
			chevronTextureList.set(index, CHEVRON_TEXTURE_BASE + stage + ".png");
		});
	}
	
	public String get(int index) {
		return chevronTextureList.get(index);
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeInt(activeChevrons);
		buf.writeBoolean(isFinalActive);
	}
	
	public void fromBytes(ByteBuf buf) {
		activeChevrons = buf.readInt();
		isFinalActive = buf.readBoolean();
	}
}
