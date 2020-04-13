package mrjake.aunis.gui.container;

import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;

public class CapacitorContainerGui extends GuiContainer {

	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/gui/container_capacitor.png");
	
	private CapacitorContainer container;
	
	public CapacitorContainerGui(CapacitorContainer container) {
		super(container);
		
		this.container = container;
		this.xSize = 176;
		this.ySize = 168;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		StargateAbstractEnergyStorage energyStorage = (StargateAbstractEnergyStorage) container.capTile.getCapability(CapabilityEnergy.ENERGY, null);

		int width = Math.round((energyStorage.getEnergyStored()/((float)AunisConfig.powerConfig.stargateEnergyStorage/4) * 156));
		drawGradientRect(guiLeft+10, guiTop+61, guiLeft+10+width, guiTop+61+6, 0xffcc2828, 0xff731616);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(I18n.format("gui.capacitor.name"), 7, 6, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        
		StargateAbstractEnergyStorage energyStorage = (StargateAbstractEnergyStorage) container.capTile.getCapability(CapabilityEnergy.ENERGY, null);
        
		int energyStored = energyStorage.getEnergyStored();
		int maxEnergyStored = energyStorage.getMaxEnergyStored();
		
		String energyPercent = String.format("%.2f", energyStored/(float)maxEnergyStored * 100) + " %";
		fontRenderer.drawString(energyPercent, 170-fontRenderer.getStringWidth(energyPercent), 71, 4210752);
		
		int transferred = container.capTile.getEnergyTransferedLastTick();
		TextFormatting transferredFormatting = TextFormatting.GRAY;
		String transferredSign = "";
		
		if (transferred > 0) {
			transferredFormatting = TextFormatting.GREEN;
			transferredSign = "+";
		} else if (transferred < 0) {
			transferredFormatting = TextFormatting.RED;
		}
		
//		String energyString = String.format("%,d / %,d RF", energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored());
//		fontRenderer.drawString(energyString, 169-fontRenderer.getStringWidth(energyString), 49, 4210752);
		
        if (isPointInRegion(10, 61, 156, 6, mouseX, mouseY)) {
			List<String> power = Arrays.asList(
					I18n.format("gui.stargate.energyBuffer"),
					TextFormatting.GRAY + String.format("%,d / %,d RF", energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored()),
					transferredFormatting + transferredSign + String.format("%,d RF/t", transferred));
			drawHoveringText(power, mouseX-guiLeft, mouseY-guiTop);
		}
	}
}
