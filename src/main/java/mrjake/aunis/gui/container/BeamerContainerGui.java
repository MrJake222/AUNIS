package mrjake.aunis.gui.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.beamer.BeamerModeEnum;
import mrjake.aunis.beamer.BeamerStatusEnum;
import mrjake.aunis.gui.element.Diode;
import mrjake.aunis.gui.element.Diode.DiodeStatus;
import mrjake.aunis.gui.element.FluidTankElement;
import mrjake.aunis.gui.element.Tab;
import mrjake.aunis.gui.element.TabRedstone;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.BeamerChangeRoleToServer;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.SlotItemHandler;

public class BeamerContainerGui extends GuiContainer {

	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/gui/container_beamer.png");
	
	private BeamerContainer container;
	private FluidTankElement tank;
	
	private List<Diode> diodes = new ArrayList<Diode>();
	private List<Tab> tabs = new ArrayList<Tab>();
	
	private TabRedstone tabRedstone;
		
	public BeamerContainerGui(BeamerContainer container) {
		super(container);
		
		this.container = container;
		this.xSize = 176;
		this.ySize = 168;
		
		this.tank = new FluidTankElement(this, guiLeft+11, guiTop+17, 16, 54, container.tank);
		
		for (int i=1; i<5; i++) {
			container.inventorySlots.set(i, new BeamerSlot((SlotItemHandler) container.getSlot(i)));
		}

		int x = 125;
		
		diodes.add(new Diode(this, x, 40, I18n.format("gui.dhd.crystalStatus"))
				.putStatus(DiodeStatus.OFF, I18n.format("gui.dhd.no_crystal"))
				.putStatus(DiodeStatus.ON, I18n.format("gui.dhd.crystal_ok"))
				.setStatusMapper(() -> {
					return container.beamerTile.getStatus() != BeamerStatusEnum.NO_CRYSTAL ? DiodeStatus.ON : DiodeStatus.OFF;
				})); x += 9;
		
		diodes.add(new Diode(this, x, 40, I18n.format("gui.dhd.linkStatus"))
				.putStatus(DiodeStatus.OFF, I18n.format("gui.dhd.not_linked"))
				.putStatus(DiodeStatus.ON, I18n.format("gui.dhd.linked"))
				.setStatusMapper(() -> {
					return container.beamerTile.getStatus() != BeamerStatusEnum.NOT_LINKED ? DiodeStatus.ON : DiodeStatus.OFF;
				})); x += 9;
		
		diodes.add(new Diode(this, x, 40, I18n.format("gui.beamer.beamer_status"))
				.putStatus(DiodeStatus.ON, I18n.format("gui.beamer.beamer_ok"))
				.setStatusMapper(() -> {
					switch (container.beamerTile.getStatus()) {
						case OBSTRUCTED:
						case NOT_LINKED:
						case NO_CRYSTAL:
							return DiodeStatus.OFF;
							
						case CLOSED:
							return DiodeStatus.WARN;
							
						default:
							return DiodeStatus.ON;	
					}
				})
				.setStatusStringMapper(() -> {
					switch (container.beamerTile.getStatus()) {
						case NOT_LINKED: return I18n.format("gui.dhd.not_linked");
						case NO_CRYSTAL: return I18n.format("gui.beamer.no_crystal");
						case CLOSED: return I18n.format("gui.beamer.closed");
						case OBSTRUCTED: return I18n.format("gui.beamer.obstructed");
						default: return null;
					}
				})); x += 9;
		
		diodes.add(new Diode(this, x, 40, I18n.format("gui.beamer.beamer_link_status"))
				.putStatus(DiodeStatus.ON, I18n.format("gui.dhd.running"))
				.setStatusMapper(() -> {
					switch (container.beamerTile.getStatus()) {
						case NO_BEAMER:
						case NOT_LINKED:
						case NO_CRYSTAL:
						case OBSTRUCTED:
						case OBSTRUCTED_TARGET:
						case CLOSED:
							return DiodeStatus.OFF;
							
						default:
							return DiodeStatus.ON;	
					}
				})
				.setStatusStringMapper(() -> {
					switch (container.beamerTile.getStatus()) {
						case NOT_LINKED: return I18n.format("gui.dhd.not_linked");
						case NO_CRYSTAL: return I18n.format("gui.beamer.no_crystal");
						case NO_BEAMER: return I18n.format("gui.beamer.no_beamer");
						case CLOSED: return I18n.format("gui.beamer.closed");
						case OBSTRUCTED: return I18n.format("gui.beamer.obstructed");
						case OBSTRUCTED_TARGET: return I18n.format("gui.beamer.obstructed_target");
						default: return null;
					}
				})); x += 9;
		
		diodes.add(new Diode(this, x, 40, I18n.format("gui.beamer.role_status"))
				.putStatus(DiodeStatus.ON, I18n.format("gui.dhd.role_ok"))
				.setStatusMapper(() -> {
					switch (container.beamerTile.getStatus()) {
						case NO_BEAMER:
						case NOT_LINKED:
						case NO_CRYSTAL:
						case OBSTRUCTED:
						case OBSTRUCTED_TARGET:
						case CLOSED:
						case BEAMER_DISABLED:
						case BEAMER_DISABLED_TARGET:
						case TWO_RECEIVERS:
						case TWO_TRANSMITTERS:
						case MODE_MISMATCH:
							return DiodeStatus.OFF;
					
						case INCOMING:
						case BEAMER_DISABLED_BY_LOGIC:
						case BEAMER_DISABLED_BY_LOGIC_TARGET:
							return DiodeStatus.WARN;
							
						case OK:
							return DiodeStatus.ON;	
					}
					
					return DiodeStatus.OFF;
				})
				.setStatusStringMapper(() -> {
					switch (container.beamerTile.getStatus()) {
						case OBSTRUCTED: return I18n.format("gui.beamer.obstructed");
						case OBSTRUCTED_TARGET: return I18n.format("gui.beamer.obstructed_target");
						case TWO_TRANSMITTERS: return I18n.format("gui.beamer.two_transmitters");
						case TWO_RECEIVERS: return I18n.format("gui.beamer.two_receivers");
						case MODE_MISMATCH: return I18n.format("gui.beamer.mode_mismatch");
						case BEAMER_DISABLED: return I18n.format("gui.beamer.disabled");
						case BEAMER_DISABLED_TARGET: return I18n.format("gui.beamer.disabled_target");
						case NO_BEAMER: return I18n.format("gui.beamer.no_beamer");
						case NOT_LINKED: return I18n.format("gui.dhd.not_linked");
						case NO_CRYSTAL: return I18n.format("gui.beamer.no_crystal");

						case CLOSED: return I18n.format("gui.beamer.closed");
						case INCOMING: return I18n.format("gui.beamer.incoming");
						case BEAMER_DISABLED_BY_LOGIC: return I18n.format("gui.beamer.disabled_by_logic");
						case BEAMER_DISABLED_BY_LOGIC_TARGET: return I18n.format("gui.beamer.disabled_by_logic_target");
							
						case OK: return I18n.format("gui.beamer.running");
					}
					
					return null;
				})); x += 9;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(0, guiLeft+124, guiTop+18, 46, 20, "Change"));
				
		tabRedstone = (TabRedstone) TabRedstone.builder()
				.setFontRenderer(fontRenderer)
				.setRedstoneModeGetter(() -> container.beamerTile.getRedstoneMode())
				.setBeamerModeGetter(() -> container.beamerTile.getMode())
				.setBlockPos(container.pos)
				.setGuiPosition(guiLeft, guiTop)
				.setTabPosition(-21, 2)
				.setOpenPosition(-128)
				.setTabSize(128, 80)
				.setTabTitle(I18n.format("gui.beamer.activation"))
				.setTexture(BACKGROUND_TEXTURE, 256)
				.setBackgroundTextureLocation(0, 176)
				.setIconRenderPos(1, 7)
				.setIconSize(20, 18)
				.setIconTextureLocation(128, 0).build();
		
		updated = false;
		
		tabs.clear();
		tabs.add(tabRedstone);
	}
	
	private boolean updated = false;
	
	public void updateStartStopInactivity() {
		if (updated)
			return;
		
		updated = true;
		
		tabRedstone.setText(0, container.beamerTile.getStart());
		tabRedstone.setText(1, container.beamerTile.getStop());
		tabRedstone.setText(2, container.beamerTile.getInactivity());
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
		
		Tab.updatePositions(tabs);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		for (Tab tab : tabs) {
			tab.render(fontRenderer, mouseX, mouseY);
		}
		
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		switch (container.beamerTile.getMode()) {
			case POWER:
				drawTexturedModalRect(guiLeft+81, guiTop+15, 176, 0, 27, 24);
				drawTexturedModalRect(guiLeft+9, guiTop+60, 0, 168, 158, 8);
				
				StargateAbstractEnergyStorage energyStorage = (StargateAbstractEnergyStorage) container.beamerTile.getCapability(CapabilityEnergy.ENERGY, null);
				int width = Math.round((energyStorage.getEnergyStored()/(float)energyStorage.getMaxEnergyStored() * 156));
				drawGradientRect(guiLeft+10, guiTop+61, guiLeft+10+width, guiTop+61+6, 0xffcc2828, 0xff731616);
				
				break;
				
			case FLUID:
				drawTexturedModalRect(guiLeft+81, guiTop+15, 203, 0, 27, 24);
				drawTexturedModalRect(guiLeft+10, guiTop+16, 176, 86, 18, 56);
				
				break;
				
			case ITEMS:
				drawTexturedModalRect(guiLeft+81, guiTop+15, 203, 24, 27, 24);
				drawTexturedModalRect(guiLeft+8, guiTop+17, 193, 48, 36, 36);
				
				break;
			
			default:
				break;
		}		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {		
		String role = I18n.format(container.beamerTile.getRole().translationKey);
		fontRenderer.drawString(role, 168-fontRenderer.getStringWidth(role)+2, 8, 4210752);
		fontRenderer.drawString(I18n.format("gui.beamer.name"), 7, 6, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        
        boolean[] statuses = new boolean[diodes.size()];
		
        GlStateManager.color(1, 1, 1);
		GlStateManager.disableLighting();
		for (int i=0; i<diodes.size(); i++) {
			statuses[i] = diodes.get(i).render(mouseX-guiLeft, mouseY-guiTop);
		}
		
		for (int i=0; i<diodes.size(); i++) {
			if (statuses[i])
				diodes.get(i).renderTooltip(mouseX-guiLeft, mouseY-guiTop);
		}
		
		GlStateManager.disableLighting();
        
        switch (container.beamerTile.getMode()) {
	        case POWER:
				StargateAbstractEnergyStorage energyStorage = (StargateAbstractEnergyStorage) container.beamerTile.getCapability(CapabilityEnergy.ENERGY, null);
		        
				int energyStored = energyStorage.getEnergyStored();
				int maxEnergyStored = energyStorage.getMaxEnergyStored();
				
				String energyPercent = String.format("%.2f", energyStored/(float)maxEnergyStored * 100) + " %";
				fontRenderer.drawString(energyPercent, 170-fontRenderer.getStringWidth(energyPercent), 71, 4210752);
				
				int transferred = container.beamerTile.getEnergyTransferredLastTick();
				TextFormatting transferredFormatting = TextFormatting.GRAY;
				String transferredSign = "";
				
				if (transferred > 0) {
					transferredFormatting = TextFormatting.GREEN;
					transferredSign = "+";
				} else if (transferred < 0) {
					transferredFormatting = TextFormatting.RED;
				}
				
		        if (isPointInRegion(10, 61, 156, 6, mouseX, mouseY)) {
					List<String> power = Arrays.asList(
							I18n.format("gui.stargate.energyBuffer"),
							TextFormatting.GRAY + String.format("%,d / %,d RF", energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored()),
							transferredFormatting + transferredSign + String.format("%,d RF/t", transferred));
					drawHoveringText(power, mouseX-guiLeft, mouseY-guiTop);
				}
		        
		        break;
	        
	        case FLUID:
				tank.renderTank();
				
				// Tank's gauge
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.enableBlend();
				mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
				drawTexturedModalRect(11, 18, 176, 32, 16, 54);
				GlStateManager.disableBlend();
				
	        	tank.renderTooltip(mouseX, mouseY);
	        	break;
	        	
	        case ITEMS:
	        	break;
	        	
	        default:
	        	break;
        }
        
		for (Tab tab : tabs) {
			tab.renderFg(this, fontRenderer, mouseX, mouseY);
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		AunisPacketHandler.INSTANCE.sendToServer(new BeamerChangeRoleToServer(container.pos));
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		tabRedstone.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		tabRedstone.updateScreen();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		for (int i=0; i<tabs.size(); i++) {
			Tab tab = tabs.get(i);
			
			if (tab.isCursorOnTab(mouseX, mouseY)) {
				Tab.tabsInteract(tabs, i);
				
				break;
			}
		}
		
		tabRedstone.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	private class BeamerSlot extends SlotItemHandler {
		
		public BeamerSlot(SlotItemHandler slot) {
			super(slot.getItemHandler(), slot.getSlotIndex(), slot.xPos, slot.yPos);
			this.slotNumber = slot.slotNumber;
		}

		@Override
		public boolean isEnabled() {
			return container.beamerTile.getMode() == BeamerModeEnum.ITEMS;
		}
	}
}
