package mrjake.aunis.gui.element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class Diode {
	
	public static final ResourceLocation DIODE_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/gui/diodes.png");
	
	private GuiScreen screen;

	private int x;
	private int y;
	
	private String description;
	private Map<DiodeStatus, String> statusStringMap;
	private DiodeStatus status;
	private StatusMapperInterface statusMapper;
	private StatusStringMapperInterface statusStringMapper;

	public Diode(GuiScreen screen, int x, int y, String description) {
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.description = description;
		this.statusStringMap = new HashMap<DiodeStatus, String>(3);
	}
	
	public Diode putStatus(DiodeStatus status, String statusString) {
		statusStringMap.put(status, statusString);
		return this;
	}
	
	public Diode setStatusMapper(StatusMapperInterface statusMapper) {
		this.statusMapper = statusMapper;
		return this;
	}
	
	public Diode setStatusStringMapper(StatusStringMapperInterface statusStringMapper) {
		this.statusStringMapper = statusStringMapper;
		return this;
	}
	
	public Diode setDiodeStatus(DiodeStatus status) {
		this.status = status;
		return this;
	}
	
	public boolean render(int mouseX, int mouseY) {
		status = statusMapper.get();
				
		GlStateManager.enableBlend();
		screen.mc.getTextureManager().bindTexture(DIODE_TEXTURE);
		Gui.drawModalRectWithCustomSizedTexture(x, y, status.xTex, status.yTex, 8, 7, 16, 16);
		GlStateManager.disableBlend();
		
		return GuiHelper.isPointInRegion(x, y, 8, 8, mouseX, mouseY);
	}
	
	public void renderTooltip(int mouseX, int mouseY) {
		String statusString = null;
		
		if (statusStringMapper != null)
			statusString = statusStringMapper.get();
		
		if (statusString == null)
			statusString = statusStringMap.get(status);
		
		TextComponentString textComponent = new TextComponentString(statusString);
		textComponent.getStyle().setItalic(true);
		textComponent.getStyle().setColor(status.color);
				
		screen.drawHoveringText(Arrays.asList(
				description,
				textComponent.getFormattedText()), mouseX, mouseY);
	}
	
	public static enum DiodeStatus {
		OFF(0, 0, TextFormatting.DARK_RED),
		WARN(8, 0, TextFormatting.YELLOW),
		ON(0, 7, TextFormatting.GREEN);
		
		public int xTex;
		public int yTex;
		public TextFormatting color;

		private DiodeStatus(int xTex, int yTex, TextFormatting color) {
			this.xTex = xTex;
			this.yTex = yTex;
			this.color = color;
		}
	}
	
	public static interface StatusMapperInterface {
		public DiodeStatus get();
	}
	
	public static interface StatusStringMapperInterface {
		
		/**
		 * @return Custom status string or {@code null} to use {@link Map} one.
		 */
		@Nullable
		public String get();
	}
}
