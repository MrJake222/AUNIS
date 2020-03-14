package mrjake.aunis.gui;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.gui.element.Diode;
import mrjake.aunis.gui.element.Diode.DiodeStatus;
import mrjake.aunis.gui.element.Diode.DiodeStatusString;
import mrjake.aunis.tileentity.util.ReactorStateEnum;
import mrjake.aunis.gui.element.FluidTankElement;
import mrjake.aunis.gui.element.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

public class DHDContainerGui extends GuiContainer {

	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/gui/dhd_container.png");
	
	private DHDContainer container;
	private FluidTankElement tank;
	
	private List<Diode> diodes = new ArrayList<Diode>(2);
	
	private static final String NO_CRYSTAL_STRING = new DiodeStatusString("gui.dhd.no_crystal").setItalic().setColor(TextFormatting.DARK_RED).getFormattedText();
	private static final String NOT_LINKED_STRING = new DiodeStatusString("gui.dhd.not_linked").setItalic().setColor(TextFormatting.DARK_RED).getFormattedText();

	private boolean hasCrystal;
	private boolean isLinked;
	
	public DHDContainerGui(DHDContainer container) {
		super(container);
				
		this.container = container;
		container.tankNaquadah.setFluid(new FluidStack(AunisFluids.moltenNaquadahRefined, 0));
		this.tank = new FluidTankElement(this, 151, 18, 16, 54, AunisFluids.moltenNaquadahRefined, container.tankNaquadah);
		
		diodes.add(new Diode(this, 8, 55, I18n.format("gui.dhd.crystalStatus")).setDiodeStatus(DiodeStatus.OFF)
				.putStatus(DiodeStatus.OFF, NO_CRYSTAL_STRING)
				.putStatus(DiodeStatus.ON, new DiodeStatusString("gui.dhd.crystal_ok").setItalic().setColor(TextFormatting.GREEN))
				.setStatusMapper(() -> {
					return hasCrystal ? DiodeStatus.ON : DiodeStatus.OFF;
				}));
		
		diodes.add(new Diode(this, 17, 55, I18n.format("gui.dhd.linkStatus")).setDiodeStatus(DiodeStatus.OFF)
				.putStatus(DiodeStatus.OFF, NOT_LINKED_STRING)
				.putStatus(DiodeStatus.ON, new DiodeStatusString("gui.dhd.linked").setItalic().setColor(TextFormatting.GREEN))
				.setStatusMapper(() -> {
					return (hasCrystal && isLinked) ? DiodeStatus.ON : DiodeStatus.OFF;
				})
				.setStatusStringMapper(() -> {
					if (!hasCrystal)
						return NO_CRYSTAL_STRING;
					
					return null;
				}));
		
		diodes.add(new Diode(this, 26, 55, I18n.format("gui.dhd.reactorStatus"))
				.putStatus(DiodeStatus.OFF, new DiodeStatusString("gui.dhd.no_fuel").setItalic().setColor(TextFormatting.DARK_RED))
				.putStatus(DiodeStatus.WARN, new DiodeStatusString("gui.dhd.standby").setItalic().setColor(TextFormatting.YELLOW))
				.putStatus(DiodeStatus.ON, new DiodeStatusString("gui.dhd.running").setItalic().setColor(TextFormatting.GREEN))
				.setStatusMapper(() -> {
					switch (container.dhdTile.getReactorState()) {
						case NOT_LINKED:
						case NO_FUEL:
							return DiodeStatus.OFF;
							
						case ONLINE:
							return DiodeStatus.ON;
							
						case STANDBY:
							return DiodeStatus.WARN;
							
						default:
							return DiodeStatus.OFF;
					}
				})
				.setStatusStringMapper(() -> {
					if (!hasCrystal)
						return NO_CRYSTAL_STRING;
					
					else if (!isLinked)
						return NOT_LINKED_STRING;
	
					return null;
				}));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		
		hasCrystal = container.slotCrystal.getHasStack();
		isLinked = container.dhdTile.getReactorState() != ReactorStateEnum.NOT_LINKED;
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        
		// Crystal background
		if (container.slotCrystal.getHasStack())
			drawTexturedModalRect(guiLeft+76, guiTop+16, 176, 0, 24, 32);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		
		if (container.dhdTile.getReactorState() == ReactorStateEnum.ONLINE) {
			// Top duct Naquadah
			for (int i=0; i<3; i++)
				drawTexturedModalRect(guiLeft+102+16*i, guiTop+55, tank.sprite, 16, 16);
			
			// Bottom duct Naquadah
			for (int i=0; i<5; i++)
				GuiHelper.drawTexturedRectScaled(guiLeft+86+16*i, guiTop+82, tank.sprite, 16, 16, 10.0f/16);
		}
		
		// Naquadah ducts
		GlStateManager.enableBlend();
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		drawTexturedModalRect(guiLeft+102, guiTop+55, 0, 168, 48, 16);
		drawTexturedModalRect(guiLeft+83, guiTop+72, 0, 184, 84, 10);
		GlStateManager.disableBlend();
	}
		
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String fusion = I18n.format("gui.dhd.fusion");
		fontRenderer.drawString(fusion, 168-fontRenderer.getStringWidth(fusion)+1, 6, 4210752);
		fontRenderer.drawString(I18n.format("gui.dhd.upgrades"), 8, 6, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 4, 4210752);
        
        tank.renderTank();
        
        // Tank's gauge
		GlStateManager.enableBlend();
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		drawTexturedModalRect(151, 18, 176, 32, 16, 54);
		GlStateManager.disableBlend();
		
		boolean[] statuses = new boolean[diodes.size()];
		
		for (int i=0; i<diodes.size(); i++) {
			statuses[i] = diodes.get(i).render(mouseX-guiLeft, mouseY-guiTop);
		}
		
		for (int i=0; i<diodes.size(); i++) {
			if (statuses[i])
				diodes.get(i).renderTooltip(mouseX-guiLeft, mouseY-guiTop);
		}
		
//		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Aunis.ModID, "textures/gui/diodes.png"));
		
//		boolean linked = container.dhdTile.isLinked();
//		boolean reactorRunning = true;
//		boolean drawLinkedStatus = drawDiode(8,  55, mouseX, mouseY, linked);
//		boolean drawReactorStatus = drawDiode(17,  55, mouseX, mouseY, reactorRunning);
//		
//		if (drawLinkedStatus) {
//			TextComponentTranslation linkedState = new TextComponentTranslation(linked ? "gui.dhd.linked" : "gui.dhd.not_linked");
//			linkedState.getStyle().setItalic(true).setColor(linked ? TextFormatting.GREEN : TextFormatting.DARK_RED);
//			
//			List<String> linkedStatus = Arrays.asList(
//					I18n.format("gui.dhd.linkStatus"),
//					linkedState.getFormattedText());
//			
//			drawHoveringText(linkedStatus, mouseX-guiLeft, mouseY-guiTop);
//		}
//		
//		if (drawReactorStatus) {
//			TextComponentTranslation reactorState = new TextComponentTranslation(reactorRunning ? "gui.dhd.running" : "gui.dhd.not_running");
//			reactorState.getStyle().setItalic(true).setColor(reactorRunning ? TextFormatting.GREEN : TextFormatting.DARK_RED);
//			
//			List<String> reactorStatus = Arrays.asList(
//					I18n.format("gui.dhd.reactorStatus"),
//					reactorState.getFormattedText());
//			
//			drawHoveringText(reactorStatus, mouseX-guiLeft, mouseY-guiTop);
//		}
		
		tank.renderTooltip(mouseX, mouseY);
	}
}
