package mrjake.aunis.gui.element;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.SlotItemHandler;

public abstract class Tab {
	
	// Container info (position and size)
	protected int guiLeft;
	protected int guiTop;
	protected int xSize;
	protected int ySize;
	
	// Tab info (location & position)
	protected int defaultX;
	protected int defaultY;
	private int openX;
	private int hiddenX;
	protected int width;
	protected int height;
	protected String tabTitle;
	protected TabSideEnum side;
	
	// Background texture
	protected ResourceLocation bgTexLocation;
	protected int textureSize;
	private int bgTexX;
	private int bgTexY;
	
	// Icon texture
	private int iconX;
	private int iconY;
	private int iconWidth;
	private int iconHeight;
	private int iconTexX;
	private int iconTexY;
	
	protected Tab(TabBuilder builder) {
		this.guiLeft = builder.guiLeft;
		this.guiTop = builder.guiTop;
		this.xSize = builder.xSize;
		this.ySize = builder.ySize;

		this.defaultX = builder.defaultX;
		this.defaultY = builder.defaultY;
		this.openX = builder.openX;
		this.hiddenX = builder.hiddenX;
		this.width = builder.width;
		this.height = builder.height;
		this.tabTitle = builder.tabTitle;
		this.side = builder.side;

		this.bgTexLocation = builder.bgTexLocation;
		this.textureSize = builder.textureSize;
		this.bgTexX = builder.bgTexX;
		this.bgTexY = builder.bgTexY;

		this.iconX = builder.iconX;
		this.iconY = builder.iconY;
		this.iconWidth = builder.iconWidth;
		this.iconHeight = builder.iconHeight;
		this.iconTexX = builder.iconTexX;
		this.iconTexY = builder.iconTexY;
		
		startingOffsetX = defaultX;
	}
	
	private boolean isVisible = true;
	
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	
	private boolean animate = false;
	private int startingOffsetX = 0;
	private boolean isTabOpen = false;
	private boolean isTabHidden = false;
	
	public boolean isOpen() {
		return isTabOpen;
	}
	
	public boolean isVisible() {
		return isVisible;
	}
	
	public boolean isHidden() {
		return isTabHidden;
	}
	
	private float offsetPerTick;
	private long animationStart;
	private int animationTime;
	
	protected int currentOffsetX = 0;
	
	public void render(FontRenderer fontRenderer, int mouseX, int mouseY) {
		if (!isVisible)
			return;
		
		Minecraft mc = Minecraft.getMinecraft();
		updateAnimation(mc);
		
		GlStateManager.enableBlend();
		GlStateManager.color(1, 1, 1, 1);
				
		mc.getTextureManager().bindTexture(bgTexLocation);
		Gui.drawModalRectWithCustomSizedTexture(guiLeft+currentOffsetX, guiTop+defaultY, bgTexX, bgTexY, width, height, textureSize, textureSize);
		Gui.drawModalRectWithCustomSizedTexture(guiLeft+iconX+currentOffsetX, guiTop+defaultY+iconY, iconTexX, iconTexY, iconWidth, iconHeight, textureSize, textureSize);
		
		fontRenderer.drawString(tabTitle, guiLeft+currentOffsetX+(side.left() ? 24 : 0)+4, guiTop+defaultY+10, 4210752);
		
		GlStateManager.disableBlend();
	}
	
	public void renderFg(GuiScreen screen, FontRenderer fontRenderer, int mouseX, int mouseY) {
		if (!isVisible)
			return;
		
		if (!isTabOpen && isCursorOnTab(mouseX, mouseY)) {
			screen.drawHoveringText(tabTitle, mouseX-guiLeft, mouseY-guiTop);
		}
	}
	
	public boolean isCursorOnTab(int mouseX, int mouseY) {
		int xOffset = 0;
		if (isTabHidden && hiddenX < defaultX) {
			// hide - slides to the left
			xOffset = 15;
		}
	
		return isVisible && GuiHelper.isPointInRegion(guiLeft+iconX+currentOffsetX+xOffset, guiTop+defaultY+iconY, iconWidth - (isTabHidden ? 15 : 0), iconHeight, mouseX, mouseY);
	}
	
	public void openTab() {		
		animateTo(openX, 10);
		
		isTabHidden = false;
		isTabOpen = true;
	}
	
	public void closeTab() {
		animateTo(defaultX, isTabOpen ? 10 : 5);
		
		isTabHidden = false;
		isTabOpen = false;
	}
	
	public void hideTab() {
		animateTo(hiddenX, isTabOpen ? 10 : 5);
		
		isTabHidden = true;
		isTabOpen = false;
	}
	
	public void animateTo(int targetOffsetX, int animationTime) {
		this.animationTime = animationTime;
		startingOffsetX = currentOffsetX;

		offsetPerTick = (float)(targetOffsetX - startingOffsetX) / animationTime;
		animationStart = Minecraft.getMinecraft().world.getTotalWorldTime();
		
		animate = true;
	}
	
	public void updateAnimation(Minecraft mc) {
		currentOffsetX = startingOffsetX;
		
		if (animate) { 
			float effTick = mc.world.getTotalWorldTime() - animationStart + mc.getRenderPartialTicks();
			
			if (effTick < animationTime) {
				currentOffsetX += Math.round(offsetPerTick * effTick);
			} else {
				animate = false;
				currentOffsetX += (int) (offsetPerTick * animationTime);
				startingOffsetX = currentOffsetX;
			}
		}
	}
	
	public Rectangle getArea() {
		int tabHeight = (isTabOpen | animate) ? height : 34;
		
		if (side.left()) {
			return new Rectangle(guiLeft+currentOffsetX, guiTop+defaultY, Math.abs(currentOffsetX), tabHeight);
		}
		
		// right
		return new Rectangle(guiLeft+xSize, guiTop+defaultY, width+currentOffsetX-xSize, tabHeight);
	}
		
	// ------------------------------------------------------------------------------------------------
	// Interaction
	
	/**
	 * Interact with tab (clicked).
	 * 
	 * @return {@code true} if the Tab will be opening, {@code false} if closing.
	 */
	public static boolean tabsInteract(List<Tab> tabs, int tabIndex) {
		Tab tab = tabs.get(tabIndex);
		
		// Tabs higher than clicked one
		for (Tab tab2 : tabs.subList(0, tabIndex)) {
			if (tab.side == tab2.side) {
				tab2.closeTab();
			}
		}
		
		// Tabs lower than clicked one
		for (Tab tab2 : tabs.subList(tabIndex+1, tabs.size())) {
			if (tab.side == tab2.side) {
				if (tab.isOpen())
					tab2.closeTab();
				else
					tab2.hideTab();
			}
		}
		
		if (tab.isOpen()) {
			tab.closeTab();
			return false;
		} else {
			tab.openTab();
			return true;
		}
	}
	
	/**
	 * Sorts tabs so only visible (enabled by some upgrade) occupy space.
	 */
	public static void updatePositions(List<Tab> tabs) {
		int yPosLeft = 2;
		int yPosRight = 2;
		
		for (Tab tab : tabs) {
			if (tab.isVisible()) {
				if (tab.side.left()) {
					tab.defaultY = yPosLeft;
					yPosLeft += 22;
				}
				
				else {
					// right side
					tab.defaultY = yPosRight;
					yPosRight += 22;
				}
			}
		}
	}
	
	
	// ------------------------------------------------------------------------------------------------
	// Builder
		
	public static abstract class TabBuilder {
		
		// Container info (position) and ID
		private int guiLeft;
		private int guiTop;
		private int xSize;
		private int ySize;
		
		// Tab info (location & position)
		private int defaultX;
		private int defaultY;
		private int openX;
		private int hiddenX;
		private int width;
		private int height;
		private String tabTitle;
		private TabSideEnum side;
		
		// Background texture
		private ResourceLocation bgTexLocation;
		private int textureSize;
		private int bgTexX;
		private int bgTexY;
		
		// Icon texture
		private int iconX;
		private int iconY;
		private int iconWidth;
		private int iconHeight;
		private int iconTexX;
		private int iconTexY;
		
		public TabBuilder setGuiSize(int xSize, int ySize) {
			this.xSize = xSize;
			this.ySize = ySize;
			
			return this;
		}
		
		public TabBuilder setGuiPosition(int guiLeft, int guiTop) {
			this.guiLeft = guiLeft;
			this.guiTop = guiTop;
			
			return this;
		}
		
		public TabBuilder setTabPosition(int defaultX, int defaultY) {
			this.defaultX = defaultX;
			this.defaultY = defaultY;
			
			return this;
		}
		
		public TabBuilder setOpenX(int openX) {
			this.openX = openX;
			
			return this;
		}
		
		public TabBuilder setHiddenX(int hiddenX) {
			this.hiddenX = hiddenX;
			
			return this;
		}
				
		public TabBuilder setTabSize(int width, int height) {
			this.width = width;
			this.height = height;
			
			return this;
		}
		
		public TabBuilder setTabTitle(String tabTitle) {
			this.tabTitle = tabTitle;
			
			return this;
		}
		
		public TabBuilder setTabSide(TabSideEnum side) {
			this.side = side;
			
			return this;
		}
				
		public TabBuilder setTexture(ResourceLocation bgTexLocation, int texureSize) {
			this.bgTexLocation = bgTexLocation;
			this.textureSize = texureSize;
			
			return this;
		}
			
		public TabBuilder setBackgroundTextureLocation(int bgTexX, int bgTexY) {
			this.bgTexX = bgTexX;
			this.bgTexY = bgTexY;
			
			return this;
		}
		
		public TabBuilder setIconRenderPos(int iconX, int iconY) {
			this.iconX = iconX;
			this.iconY = iconY;
			
			return this;
		}
		
		public TabBuilder setIconSize(int iconWidth, int iconHeight) {
			this.iconWidth = iconWidth;
			this.iconHeight = iconHeight;
			
			return this;
		}
		
		public TabBuilder setIconTextureLocation(int iconTexX, int iconTexY) {
			this.iconTexX = iconTexX;
			this.iconTexY = iconTexY;
			
			return this;
		}
		
		public abstract Tab build();
	}
	
	
	// ------------------------------------------------------------------------------------------------
	// Tab slot
		
	public class SlotTab extends SlotItemHandler {
		
		private UpdateSlotPositionInterface updateSlotPosition;

		public SlotTab(SlotItemHandler slot, UpdateSlotPositionInterface updateSlotPosition) {
			super(slot.getItemHandler(), slot.getSlotIndex(), slot.xPos, slot.yPos);
			this.slotNumber = slot.slotNumber;
			
			this.updateSlotPosition = updateSlotPosition;
		}

		@Override
		public boolean isEnabled() {
			return isTabOpen && !animate;
		}
		
		public void updatePos() {
//			this.xPos = currentOffsetX + 106;
//			this.yPos = defaultY + 87;
			updateSlotPosition.updatePos(this);
		}
	}
	
	public static interface UpdateSlotPositionInterface {
		public void updatePos(SlotTab slotTab);
	}
}
