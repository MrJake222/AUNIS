package mrjake.aunis.gui;

import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.state.StargateOrlinGuiState;
import mrjake.aunis.state.StateTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

public class StargateOrlinGui extends GuiBase {
		
	private BlockPos pos;
	public StargateOrlinGuiState state;
	
	public StargateOrlinGui(BlockPos pos, StargateOrlinGuiState state) {
		super(674, 20, 4, FRAME_COLOR, BG_COLOR, TEXT_COLOR, 0);
		
		this.state = state;
		this.pos = pos;
	}	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();		
		if (width < imageWidth) {
			GlStateManager.translate((width - imageWidth/2f)/2f, (height - imageHeight/2f)/2f, 0);
			GlStateManager.scale( 0.5, 0.5, 0 );
		}
		
		else {
			translateToCenter();
		}
		
		// width: width minus double x-axis translate
		// height: 20
		// frame: 4
		frame(imageWidth, 20, 4, frameColor, true);
		
		// x: frame
		// y: frame
		// w: frame width(above) - 2*frame
		// h: height(above) - frame
		double energyLevel = state.energy / ((double) state.maxEnergy);
		int width = (int) (4 + (imageWidth-28) * energyLevel);
		drawRect(4, 4, width, 16, 0xB0E01F09);
		
		String energy = String.format("%,d", state.energy);
		String maxEnergy = String.format("%,d", state.maxEnergy);
		
		String energyString = energy + " / " + maxEnergy + " uI";
		
		// x: frame width
		// y: frame height + 4
		fontRenderer.drawStringWithShadow(energyString, imageWidth - fontRenderer.getStringWidth(energyString), 24, 0x8896B3);
		
		GlStateManager.popMatrix();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void updateScreen() {
		AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Minecraft.getMinecraft().player, StateTypeEnum.GUI_STATE));
	}
}
