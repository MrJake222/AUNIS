package mrjake.aunis.gui;

import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.state.StargateMilkyWayGuiState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class StargateMilkyWayGui extends GuiBase {
		
	private BlockPos pos;
	public StargateMilkyWayGuiState state;
	
	private final static int sectionSize = 90;
	private final static int frameThickness = 8;
	
	private final static int bgHeight = sectionSize + 2*frameThickness;
	private final static int imageHeight = bgHeight + 2*frameThickness;
	
	private int sections;
	
	private int calcWidth(int sections) {
		int bgWidth = sectionSize*sections + (sections+1)*frameThickness;
		
		return bgWidth + 2*frameThickness;
	}
	
	public StargateMilkyWayGui(BlockPos pos, StargateMilkyWayGuiState state) {
		super(0, imageHeight, frameThickness, FRAME_COLOR, BG_COLOR, TEXT_COLOR, 0);
		
		sections = state.hasUpgrade() ? 7 : 6;
		setImageWidth(calcWidth(sections));
		
		this.state = state;
		this.pos = pos;
	}	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//		drawDefaultBackground();
		
		GlStateManager.pushMatrix();		
		if (width < imageWidth) {
			GlStateManager.translate((width - imageWidth/2f)/2f, (height - imageHeight/2f)/2f, 0);
			GlStateManager.scale( 0.5, 0.5, 0 );
		}
	
		else {
			translateToCenter();
		}
		
		
		drawBackground();
				
		for (int i=0; i<sections; i++) {
			String name = state.getGateAddress().get(i).name;
			Minecraft.getMinecraft().getTextureManager().bindTexture( new ResourceLocation("aunis:textures/gui/symbol/" + name.toLowerCase() + ".png") );
			
			int firstPos = frameThickness*2;
			int x = firstPos + (sectionSize+frameThickness)*i;
			
			GlStateManager.enableBlend();
			
			drawModalRectWithCustomSizedTexture(x, firstPos, 0, 0, sectionSize, sectionSize, sectionSize, sectionSize);
			fontRenderer.drawStringWithShadow(name, x + (sectionSize - fontRenderer.getStringWidth(name))/2, firstPos + sectionSize - frameThickness, textColor);		
			
			GlStateManager.disableBlend();
		}
		
		
		// Power
		GlStateManager.translate(10, imageHeight+4, 0);
		
		// width: width minus double x-axis translate
		// height: 20
		// frame: 4
		frame(imageWidth-20, 20, 4, frameColor, true);
		
		// x: frame
		// y: frame
		// w: frame width(above) - 2*frame
		// h: height(above) - frame
		
		double energyLevel = state.energyState.energy / ((double) state.maxEnergy);
		int width = (int) (4 + (imageWidth-28) * energyLevel);
		drawRect(4, 4, width, 16, 0xB0E01F09);
		
		String energy = String.format("%,d", state.energyState.energy);
		String maxEnergy = String.format("%,d", state.maxEnergy);
		
		String energyString = energy + " / " + maxEnergy + " uI";
		
		// x: frame width
		// y: frame height + 4
		fontRenderer.drawStringWithShadow(energyString, imageWidth-20 - fontRenderer.getStringWidth(energyString), 24, 0x8896B3);
		
		GlStateManager.popMatrix();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void updateScreen() {
		AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Minecraft.getMinecraft().player, StateTypeEnum.ENERGY_STATE));
	}
}
