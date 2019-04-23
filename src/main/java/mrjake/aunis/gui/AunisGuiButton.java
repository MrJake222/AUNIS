package mrjake.aunis.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class AunisGuiButton extends GuiButton {	
	public AunisGuiButton(int id, int x, int y, int w, int h, String string) {
		super(id, x, y, w, h, string);
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            
//            drawRect(x+1, y+1, x+width-1, y+height-1, 0xFFFFFFFF);
//            drawRect(x, y, x+width, y+height, GuiBase.FRAME_COLOR);
            
            
            this.mouseDragged(mc, mouseX, mouseY);
            
            int fgcolor = 0xCCCCCC;
            int bgcolor = 0xFF1D2026;
            
            if (!this.enabled) {
            	fgcolor = 10526880;
            }
            
            else if (this.hovered) {
            	fgcolor = 0xFFFFFF;
            	bgcolor = 0xFF313640;
            }
            
            drawRect(x, y, x+width, y+height, GuiBase.FRAME_COLOR);
            drawRect(x+1, y+1, x+width-1, y+height-1, bgcolor);

            this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, fgcolor);
        }
	}
}
