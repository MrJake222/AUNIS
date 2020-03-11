package mrjake.aunis.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;

public class GuiBase extends GuiScreen {
	
	public static final int FRAME_COLOR = 0xFF181A1F;
	public static final int BG_COLOR = 0xF2272B33;
	public static final int TEXT_COLOR = 0x6B768C;	
	
	public boolean isOpen = false;
	
	@Override
	public void initGui() {
		isOpen = true;
	}
	
	@Override
	public void onGuiClosed() {
		isOpen = false;
	}
	
	/**
	 * Rendered image width
	 */
	protected int imageWidth;
	
	/**
	 * Rendered image height
	 */
	protected int imageHeight;
	
	/**
	 * Frame thickness
	 */
	protected int frameThickness;
	
	/**
	 * Color of said frame
	 */
	protected int frameColor;
	
	/**
	 * Color of filler space inside frame
	 */
	protected int bgColor;
	
	/**
	 * Color for drawing text
	 */
	protected int textColor;
	
	/**
	 * Padding of the inside frame
	 * 
	 * Don't worry, no CSS involved ;)
	 */
	protected int padding;
	
	/**
	 * Incremental ID for creating buttons
	 */
	protected int id;
	
	/**
	 * Constructor. Defines basic parameters.
	 * 
	 * @param w Image width with frame
	 * @param h Image height with frame
	 * @param frameThickness Frame thickness
	 * @param frameColor Frame color
	 * @param bgColor Background color
	 * @param textColor Default text color
	 * @param padding Inner padding
	 */
	public GuiBase(int w, int h, int frameThickness, int frameColor, int bgColor, int textColor, int padding) {
		this.imageWidth = w;
		this.imageHeight = h;
		this.frameThickness = frameThickness;
		
		this.frameColor = frameColor;
		this.bgColor = bgColor;
		this.textColor = textColor;
		this.padding = padding;
		
		this.id = 0;
	}
	
	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}
	
	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}
	
	protected int getTopLeftInside() {
		return frameThickness + padding;
	}
	
	protected int getBottomRightInside(boolean returnHeight) {		
		return returnHeight ? (imageHeight - getTopLeftInside()) : (imageWidth - getTopLeftInside());
	}
	
	protected int getTopLeftAbsolute(boolean returnHeight) {
		return (int) (returnHeight ? ((height - imageHeight)/2f) : ((width - imageWidth)/2f));
	}
	
	protected void translateToCenter() {
		GlStateManager.translate((width - imageWidth)/2f, (height - imageHeight)/2f, 0);
	}
	
	
	/**
	 * Draws frame and background in it
	 */
	protected void drawBackground() {
		frame(0, 0, imageWidth, imageHeight, frameThickness, frameColor, true);
	}
	
	
	protected void frame(int x, int y, int w, int h, int thickness, int color, boolean background) {
		// Up
		drawRect(x, y, x+w, y+thickness, color);
		
		// Down
		drawRect(x, y+h-thickness, x+w, y+h, color);
				
		// Left
		drawRect(x, y+thickness, x+thickness, y+h-thickness, color);
		
		// Right
		drawRect(x+w-thickness, y+thickness, x+w, y+h-thickness, color);
		
		if (background)
			drawRect(x+thickness, y+thickness, x+w-thickness, y+h-thickness, bgColor);
	}
	
	
	protected int drawString(String text, int x, int y, int color) {
		x += getTopLeftInside();
		
//		y *= 10;
		y += getTopLeftInside() - 1;
		
		fontRenderer.drawStringWithShadow(text, x, y, color);
		
		return fontRenderer.getStringWidth(text);
	}
	
	protected void drawVerticallCenteredString(String text, int x, int y, int color) {
		int len = fontRenderer.getStringWidth(text);
		x += ((imageWidth - getTopLeftInside()) - len) / 2;
		
		drawString(text, x, y, color);
	}
	
	protected void drawTextBox(GuiTextField tf) {
		int x = tf.x - 2;
		int y = tf.y - 2;
		int x2 = tf.x + tf.width;
		int y2 = tf.y + tf.height;
		
		drawRect(x-1, y-1, x2+1, y2+1, frameColor);
        drawRect(x, y, x2, y2, 0xFF1D2026);
        
        tf.drawTextBox();
	}
	
	protected GuiTextField createTextField(int x, int y, int maxLength, String defaultText) {
		int x1 = x + getTopLeftInside();
		int y1 = y + getTopLeftInside();
		
		GuiTextField tf = new GuiTextField(id++, fontRenderer, x1+3, y1-1, getBottomRightInside(false)-x1-6, 10);
		
		tf.setMaxStringLength(maxLength);
		tf.setEnableBackgroundDrawing(false);
		tf.setText(defaultText);
		
//		tf.x -= 2;
//		tf.y -= 2;
		tf.width  += 2;
		tf.height -= 1;
		
		return tf;
	}
	
//	protected void drawBackgrounded
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
