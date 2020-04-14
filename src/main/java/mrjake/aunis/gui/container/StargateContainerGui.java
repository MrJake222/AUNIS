package mrjake.aunis.gui.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.gui.element.Tab;
import mrjake.aunis.gui.element.Tab.SlotTab;
import mrjake.aunis.gui.element.TabAddress;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.SetOpenTabToServer;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import mrjake.aunis.stargate.network.SymbolPegasusEnum;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
import mrjake.aunis.stargate.power.StargateClassicEnergyStorage;
import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile.StargateUpgradeEnum;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.SlotItemHandler;

public class StargateContainerGui extends GuiContainer {
	
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/gui/container_stargate.png");
	
	private StargateContainer container;
	private List<Tab> tabs;
	
	private TabAddress milkyWayAddressTab;
	private TabAddress pegasusAddressTab;
	private TabAddress universeAddressTab;
	
	private int energyStored;
	private int maxEnergyStored;
		
	public StargateContainerGui(StargateContainer container) {
		super(container);
		this.container = container;
		
		this.xSize = 176;
		this.ySize = 168;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		tabs = new ArrayList<Tab>();
				
		milkyWayAddressTab = (TabAddress) TabAddress.builder()
				.setGateTile(container.gateTile)
				.setSymbolType(SymbolTypeEnum.MILKYWAY)
				.setGuiPosition(guiLeft, guiTop)
				.setTabPosition(-21, 2)
				.setOpenPosition(-128)
				.setTabSize(128, 113)
				.setTabTitle(I18n.format("gui.stargate.milky_way_address"))
				.setTexture(BACKGROUND_TEXTURE, 512)
				.setBackgroundTextureLocation(176, 0)
				.setIconRenderPos(1, 7)
				.setIconSize(20, 18)
				.setIconTextureLocation(128, 0).build();
		
		pegasusAddressTab = (TabAddress) TabAddress.builder()
				.setGateTile(container.gateTile)
				.setSymbolType(SymbolTypeEnum.PEGASUS)
				.setGuiPosition(guiLeft, guiTop)
				.setTabPosition(-21, 2+22)
				.setOpenPosition(-128)
				.setTabSize(128, 113)
				.setTabTitle(I18n.format("gui.stargate.pegasus_address"))
				.setTexture(BACKGROUND_TEXTURE, 512)
				.setBackgroundTextureLocation(176, 0)
				.setIconRenderPos(1, 7)
				.setIconSize(20, 18)
				.setIconTextureLocation(128, 18).build();
		
		universeAddressTab = (TabAddress) TabAddress.builder()
				.setGateTile(container.gateTile)
				.setSymbolType(SymbolTypeEnum.UNIVERSE)
				.setGuiPosition(guiLeft, guiTop)
				.setTabPosition(-21, 2+22*2)
				.setOpenPosition(-128)
				.setTabSize(128, 113)
				.setTabTitle(I18n.format("gui.stargate.universe_address"))
				.setTexture(BACKGROUND_TEXTURE, 512)
				.setBackgroundTextureLocation(176, 0)
				.setIconRenderPos(1, 7)
				.setIconSize(20, 18)
				.setIconTextureLocation(128, 18*2).build();
		
		tabs.add(milkyWayAddressTab);
		tabs.add(pegasusAddressTab);
		tabs.add(universeAddressTab);
		
		container.inventorySlots.set(7, milkyWayAddressTab.new SlotTab((SlotItemHandler) container.getSlot(7)));
		container.inventorySlots.set(8, pegasusAddressTab.new SlotTab((SlotItemHandler) container.getSlot(8)));
		container.inventorySlots.set(9, universeAddressTab.new SlotTab((SlotItemHandler) container.getSlot(9)));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
				
		boolean hasAddressUpgrade = false;
		boolean hasMilkyWayUpgrade = false;
		boolean hasAtlantisUpgrade = false;
		boolean hasUniverseUpgrade = false;
		
		for (int i=0; i<4; i++) {
			ItemStack itemStack = container.getSlot(i).getStack();
			
			if (!itemStack.isEmpty()) {
				switch (StargateUpgradeEnum.valueOf(itemStack.getItem())) {
					case CHEVRON_UPGRADE:
						hasAddressUpgrade = true;
						break;
						
					case MILKYWAY_GLYPHS:
						hasMilkyWayUpgrade = true;
						break;
						
					case PEGASUS_GLYPHS:
						hasAtlantisUpgrade = true;
						break;
						
					case UNIVERSE_GLYPHS:
						hasUniverseUpgrade = true;
						break;
				}
			}
		}
		
		milkyWayAddressTab.setMaxSymbols(SymbolMilkyWayEnum.getMaxSymbolsDisplay(hasAddressUpgrade));
		pegasusAddressTab.setMaxSymbols(SymbolPegasusEnum.getMaxSymbolsDisplay(hasAddressUpgrade));
		universeAddressTab.setMaxSymbols(SymbolUniverseEnum.getMaxSymbolsDisplay(hasAddressUpgrade));
		
		milkyWayAddressTab.setVisible(hasMilkyWayUpgrade);
		pegasusAddressTab.setVisible(hasAtlantisUpgrade);
		universeAddressTab.setVisible(hasUniverseUpgrade);
		
		Tab.updatePositions(tabs);
		
		StargateClassicEnergyStorage energyStorageInternal = (StargateClassicEnergyStorage) container.gateTile.getCapability(CapabilityEnergy.ENERGY, null);
		energyStored = energyStorageInternal.getEnergyStoredInternally();
		maxEnergyStored = energyStorageInternal.getMaxEnergyStoredInternally();
		
		for (int i=4; i<7; i++) {
			IEnergyStorage energyStorage = container.getSlot(i).getStack().getCapability(CapabilityEnergy.ENERGY, null);
			
			if (energyStorage == null)
				continue;
			
			energyStored += energyStorage.getEnergyStored();
			maxEnergyStored += energyStorage.getMaxEnergyStored();
		}
		
		for (int i=7; i<10; i++)
			((SlotTab) container.getSlot(i)).updatePos();
				
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {		
		for (Tab tab : tabs) {
			tab.render(fontRenderer, mouseX, mouseY);
		}
		
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		GlStateManager.color(1,1,1, 1);
		drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
		
		for (int i=container.gateTile.getPowerTier(); i<4; i++)
			drawModalRectWithCustomSizedTexture(guiLeft+10+39*i, guiTop+61, 0, 168, 39, 6, 512, 512);
		
		int width = Math.round((energyStored/(float)AunisConfig.powerConfig.stargateEnergyStorage * 156));
		drawGradientRect(guiLeft+10, guiTop+61, guiLeft+10+width, guiTop+61+6, 0xffcc2828, 0xff731616);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(I18n.format("gui.stargate.capacitors"), 112, 29, 4210752);
				
		String energyPercent = String.format("%.2f", energyStored/(float)maxEnergyStored * 100) + " %";
		fontRenderer.drawString(energyPercent, 168-fontRenderer.getStringWidth(energyPercent)+2, 71, 4210752);

		fontRenderer.drawString(I18n.format("gui.upgrades"), 7, 6, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
		
		for (Tab tab : tabs) {
			tab.renderFg(this, fontRenderer, mouseX, mouseY);
		}
		
		int transferred = container.gateTile.getEnergyTransferedLastTick();
		TextFormatting transferredFormatting = TextFormatting.GRAY;
		String transferredSign = "";
		
		if (transferred > 0) {
			transferredFormatting = TextFormatting.GREEN;
			transferredSign = "+";
		} else if (transferred < 0) {
			transferredFormatting = TextFormatting.RED;
		}
		
		float toClose = container.gateTile.getEnergySecondsToClose();
		TextFormatting toCloseFormatting = TextFormatting.GRAY;
		
		if (toClose > 0) {
			if (toClose < AunisConfig.powerConfig.instabilitySeconds)
				toCloseFormatting = TextFormatting.DARK_RED;
			else
				toCloseFormatting = TextFormatting.GREEN;
		}
		
		if (isPointInRegion(10, 61, 156, 6, mouseX, mouseY)) {
			List<String> power = Arrays.asList(
					I18n.format("gui.stargate.energyBuffer"),
					TextFormatting.GRAY + String.format("%,d / %,d RF", energyStored, maxEnergyStored),
					transferredFormatting + transferredSign + String.format("%,d RF/t", transferred),
					toCloseFormatting + String.format("%.2f s", toClose));
			drawHoveringText(power, mouseX-guiLeft, mouseY-guiTop);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		for (int i=0; i<tabs.size(); i++) {
			Tab tab = tabs.get(i);
			
			if (tab.isCursorOnTab(mouseX, mouseY)) {
				if (Tab.tabsInteract(tabs, i))
					container.openTabId = i;
				else
					container.openTabId = -1;
				
				AunisPacketHandler.INSTANCE.sendToServer(new SetOpenTabToServer(container.openTabId));
				
				break;
			}
		}
	}
}
