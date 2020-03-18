package mrjake.aunis.gui.element;

import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class TabAddress extends Tab {
	
	// Gate's address
	private StargateAbstractBaseTile gateTile;
	
	protected TabAddress(TabAddressBuilder builder) {
		super(builder);
		
		this.gateTile = builder.gateTile;
	}
	
	@Override
	public void render(FontRenderer fontRenderer) {
		super.render(fontRenderer);
		
		if (gateTile.gateAddress != null) {
			for (int i=0; i<6; i++) {
				EnumSymbol symbol = gateTile.gateAddress.get(i);
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("aunis:textures/gui/symbol/" + symbol.iconFile));		
				
				SymbolCoords symbolCoords = getSymbolCoords(i);
				GuiHelper.drawTexturedRectWithShadow(symbolCoords.x, symbolCoords.y, 2, 2, 32);
			}
		}
	}
	
	@Override
	public void renderFg(GuiScreen screen, FontRenderer fontRenderer, int mouseX, int mouseY) {
		super.renderFg(screen, fontRenderer, mouseX, mouseY);
		
		if (isOpen()) {
			if (gateTile.gateAddress != null) {
				for (int i=0; i<6; i++) {
					SymbolCoords symbolCoords = getSymbolCoords(i);
					
					if (GuiHelper.isPointInRegion(symbolCoords.x, symbolCoords.y, 32, 32, mouseX, mouseY)) {
						EnumSymbol symbol = gateTile.gateAddress.get(i);
						
						screen.drawHoveringText(symbol.localize(), mouseX-guiLeft, mouseY-guiTop);
					}
				}
			}
		}
	}
	
	public SymbolCoords getSymbolCoords(int index) {
		if (index < 6)
			return new SymbolCoords(guiLeft+currentOffsetX+29+29*(index%3), guiTop+defaultY+19+28*(index/3));
		
		return new SymbolCoords(0, 0);
	}
	
	public static class SymbolCoords {
		public final int x;
		public final int y;
		
		public SymbolCoords(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	// ------------------------------------------------------------------------------------------------
	// Builder
	
	public static TabAddressBuilder builder() {
		return new TabAddressBuilder();
	}
	
	public static class TabAddressBuilder extends TabBuilder {
		
		// Gate's TileEntity reference
		private StargateAbstractBaseTile gateTile;
		
		public TabAddressBuilder setGateTile(StargateAbstractBaseTile gateTile) {
			this.gateTile = gateTile;
			
			return this;
		}
		
		@Override
		public TabAddress build() {
			return new TabAddress(this);
		}
	}
}
