package mrjake.aunis.gui;

import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.state.StargateAbstractGuiState;
import mrjake.aunis.state.StateTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

public class StargateOrlinGui extends GuiBase {
		
	private BlockPos pos;
	public StargateAbstractGuiState state;
	
	public StargateOrlinGui(BlockPos pos, StargateAbstractGuiState state) {
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
		frame(0, 0, imageWidth, 20, 4, frameColor, true);
		
		// x: frame
		// y: frame
		// w: frame width(above) - 2*frame
		// h: height(above) - frame
		double energyLevel = state.energy / ((double) state.maxEnergy);
		int width = (int) (4 + (imageWidth-8) * energyLevel);
		drawRect(4, 4, width, 16, 0xB0E01F09);
				
		String energy = String.format("%,d", state.energy);
		String maxEnergy = String.format("%,d", state.maxEnergy);
		String transferedLastTick = String.format("%,d", state.transferedLastTick);
		String secondsToClose = String.format("%.2f", state.secondsToClose);
		
		String energyString = energy + " / " + maxEnergy + " uI";
		String rateString = transferedLastTick + " uI";
		String secondsString = secondsToClose + " s";
		
		int rateColor = 0x616B80;
		
		if (state.transferedLastTick > 0)
			rateColor = 0x19B307;
		else if (state.transferedLastTick < 0)
			rateColor = 0xE01F09;
				
		// x: frame width
		// y: frame height + 4
		frame(imageWidth-fontRenderer.getStringWidth(energyString)-25, 24, fontRenderer.getStringWidth(energyString)+13+8+4, 35+8+4, 4, frameColor, true);
		
		fontRenderer.drawStringWithShadow(energyString, imageWidth-fontRenderer.getStringWidth(energyString)+8-25, 24+8, 0x616B80);
		fontRenderer.drawStringWithShadow(rateString+"/t", imageWidth-fontRenderer.getStringWidth(rateString)+8-25, 36+8, rateColor);
		fontRenderer.drawStringWithShadow(secondsString, imageWidth-fontRenderer.getStringWidth(rateString)+8-25, 48+8, rateColor);
				
		GlStateManager.popMatrix();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void updateScreen() {
		AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Minecraft.getMinecraft().player, StateTypeEnum.GUI_STATE));
	}
}
