package mrjake.aunis.gui.container;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.gui.element.Diode;
import mrjake.aunis.gui.element.Diode.DiodeStatus;
import mrjake.aunis.tileentity.util.ReactorStateEnum;
import mrjake.aunis.gui.element.FluidTankElement;
import mrjake.aunis.gui.element.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class DHDContainerGui extends GuiContainer {

	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/gui/container_dhd.png");
	
	private DHDContainer container;
	private FluidTankElement tank;
	
	private List<Diode> diodes = new ArrayList<Diode>(3);
	
	public DHDContainerGui(DHDContainer container) {
		super(container);
		
		this.xSize = 176;
		this.ySize = 168;
				
		this.container = container;
		container.tankNaquadah.setFluid(new FluidStack(AunisFluids.moltenNaquadahRefined, 0));
		this.tank = new FluidTankElement(this, 151, 18, 16, 54, container.tankNaquadah);
		
		diodes.add(new Diode(this, 8, 55, I18n.format("gui.dhd.crystalStatus")).setDiodeStatus(DiodeStatus.OFF)
				.putStatus(DiodeStatus.OFF, I18n.format("gui.dhd.no_crystal"))
				.putStatus(DiodeStatus.ON, I18n.format("gui.dhd.crystal_ok"))
				.setStatusMapper(() -> {
					return container.slotCrystal.getHasStack() ? DiodeStatus.ON : DiodeStatus.OFF;
				}));
		
		diodes.add(new Diode(this, 17, 55, I18n.format("gui.dhd.linkStatus")).setDiodeStatus(DiodeStatus.OFF)
				.putStatus(DiodeStatus.OFF, I18n.format("gui.dhd.not_linked"))
				.putStatus(DiodeStatus.ON, I18n.format("gui.dhd.linked"))
				.setStatusMapper(() -> {
					return container.dhdTile.isLinkedClient ? DiodeStatus.ON : DiodeStatus.OFF;
				}));
		
		diodes.add(new Diode(this, 26, 55, I18n.format("gui.dhd.reactorStatus"))
				.putStatus(DiodeStatus.OFF, I18n.format("gui.dhd.no_fuel"))
				.putStatus(DiodeStatus.WARN, I18n.format("gui.dhd.standby"))
				.putStatus(DiodeStatus.ON, I18n.format("gui.dhd.running"))
				.setStatusMapper(() -> {
					switch (container.dhdTile.getReactorState()) {
						case NOT_LINKED:
						case NO_FUEL:
						case NO_CRYSTAL:
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
					switch (container.dhdTile.getReactorState()) {
						case NOT_LINKED: return I18n.format("gui.dhd.not_linked");
						case NO_CRYSTAL: return I18n.format("gui.dhd.no_crystal");
						default: return null;
					}	
				}));
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
        
		// Crystal background
		if (container.slotCrystal.getHasStack()) {
			GlStateManager.enableBlend();
			drawTexturedModalRect(guiLeft+76, guiTop+16, 176, 0, 24, 32);
			GlStateManager.disableBlend();
		}
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		
		if (container.dhdTile.getReactorState() == ReactorStateEnum.ONLINE) {
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(AunisFluids.moltenNaquadahRefined.getStill().toString());
			
			// Top duct Naquadah
			for (int i=0; i<3; i++)
				drawTexturedModalRect(guiLeft+102+16*i, guiTop+55, sprite, 16, 16);
			
			// Bottom duct Naquadah
			for (int i=0; i<5; i++)
				GuiHelper.drawTexturedRectScaled(guiLeft+86+16*i, guiTop+82, sprite, 16, 16, 10.0f/16);
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
		fontRenderer.drawString(fusion, 168-fontRenderer.getStringWidth(fusion)+2, 6, 4210752);
		fontRenderer.drawString(I18n.format("gui.upgrades"), 7, 6, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        
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
		
		tank.renderTooltip(mouseX, mouseY);
	}
}
