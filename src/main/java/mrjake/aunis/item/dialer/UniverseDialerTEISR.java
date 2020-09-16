package mrjake.aunis.item.dialer;

import org.lwjgl.opengl.GL11;

import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.renderer.AunisFontRenderer;
import mrjake.aunis.item.renderer.ItemRenderHelper;
import mrjake.aunis.loader.ElementEnum;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
import mrjake.aunis.transportrings.TransportRings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.common.util.Constants.NBT;

public class UniverseDialerTEISR extends TileEntityItemStackRenderer {
	
	@Override
	public void renderByItem(ItemStack stack) {
		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		
		boolean mainhand = AunisItems.UNIVERSE_DIALER.getLastTransform() == TransformType.FIRST_PERSON_RIGHT_HAND;
		EnumHandSide handSide = mainhand ? EnumHandSide.RIGHT : EnumHandSide.LEFT;
		
		EntityPlayer player = Minecraft.getMinecraft().player;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        float angle = ItemRenderHelper.getMapAngleFromPitch(pitch);
          
        renderArms(handSide, angle, partialTicks);
		angle = 1 - angle;
		
		GlStateManager.pushMatrix();
		
		if (handSide == EnumHandSide.RIGHT) {
			GlStateManager.translate(0.8, 0, -0.5);
			GlStateManager.rotate(35, 1, 0, 0);
			GlStateManager.rotate(15, 0, 0, 1);
					
			GlStateManager.translate(0, 0.3*angle, -0.1*angle);
			GlStateManager.rotate(25*angle, 1, 0, 0);
		}
		
		else {
			GlStateManager.translate(-0.2, 0, -0.55);
			GlStateManager.rotate(30, 1, 0, 0);
			GlStateManager.rotate(-20, 0, 0, 1);
			GlStateManager.rotate(25, 0, 1, 0);
			
			GlStateManager.translate(0, 0.3*angle, -0.0*angle);
			GlStateManager.rotate(25*angle, 1, 0, 0);
		}
		
		GlStateManager.scale(0.3f, 0.3f, 0.3f);
		ElementEnum.UNIVERSE_DIALER.bindTextureAndRender();
		
		GlStateManager.translate(0, 0.20f, 0.1f);
		GlStateManager.rotate(-90, 1, 0, 0);
				
		// ---------------------------------------------------------------------------------------------
		// List rendering
		
		GlStateManager.enableBlend();
		
		if (stack.hasTagCompound()) {		
			NBTTagCompound compound = stack.getTagCompound();
			UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
			
			drawStringWithShadow(-0.47f, 0.916f, mode.localize(), true);
			drawStringWithShadow(0.22f, 0.916f, mode.next().localize(), false);
			
			if (mode.linkable && !compound.hasKey(mode.tagPosName)) {
				drawStringWithShadow(0.22f, 0.71f, I18n.format("item.aunis.universe_dialer.not_linked"), false);
			}
				
			else {
				int selected = compound.getByte("selected");
				NBTTagList tagList = compound.getTagList(mode.tagListName, NBT.TAG_COMPOUND);
				
				for (int offset=-1; offset<=1; offset++) {
					int index = selected + offset;
					if (index >= 0 && index < tagList.tagCount()) {
						
						boolean active = offset == 0;					
						NBTTagCompound entryCompound = (NBTTagCompound) tagList.getCompoundTagAt(index);
						
						switch (mode) {
							case MEMORY:
							case NEARBY:
								drawStringWithShadow(-0.32f, 0.32f - 0.32f*offset, (index+1) + ".", active);
								StargateAddress address = new StargateAddress(entryCompound);
								int symbolCount = SymbolUniverseEnum.getMaxSymbolsDisplay(entryCompound.getBoolean("hasUpgrade")); 
								
								for (int i=0; i<symbolCount; i++)
									renderSymbol(offset, i, address.get(i), active, symbolCount == 8);
								
								renderSymbol(offset, symbolCount, SymbolUniverseEnum.getOrigin(), active, symbolCount == 8);
								break;
								
							case RINGS:
								TransportRings rings = new TransportRings(entryCompound);
								drawStringWithShadow(-0.32f, 0.32f - 0.32f*offset, rings.getAddress() + ".", active);
								drawStringWithShadow(-0.10f, 0.32f - 0.32f*offset, rings.getName(), active);
								break;
								
							case OC:
								UniverseDialerOCMessage message = new UniverseDialerOCMessage(entryCompound);
								drawStringWithShadow(-0.32f, 0.32f - 0.32f*offset, (index+1) + ".", active);
								drawStringWithShadow(-0.10f, 0.32f - 0.32f*offset, message.name, active);
								break;
						}
					}
				}
			}
		}
		
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private static void drawStringWithShadow(float x, float y, String text, boolean active) {		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.rotate(180, 0,0,1);
		GlStateManager.scale(0.015f, 0.015f, 0.015f);
		
		AunisFontRenderer.getFontRenderer().drawString(text, -6, 19, active ? 0xFFFFFF : 0x006060, false);
		
		if (active) {
			GlStateManager.translate(-0.4, 0.6, -0.1);
			AunisFontRenderer.getFontRenderer().drawString(text, -6, 19, 0x606060, false);
		}
		
		GlStateManager.popMatrix();
	}
	
	private static void renderSymbol(int row, int col, SymbolInterface symbol, boolean isActive, boolean is9Chevron) {
		float x = col * 0.09f - 0.05f;
		float y = -row * 0.32f - 0.16f;
		float scale = 0.7f;
		float w = 0.19f * scale;
		float h = 0.40f * scale;
		
		if (!is9Chevron)
			x += 0.09f;
				
		Minecraft.getMinecraft().getTextureManager().bindTexture(symbol.getIconResource());
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GL11.glBegin(GL11.GL_QUADS);
		
		if (isActive)
			GlStateManager.color(0.91f, 1, 1, 1);
		else
			GlStateManager.color(0.0f, 0.38f, 0.40f, 1f);
		
		GL11.glTexCoord2f(1, 1); GL11.glVertex3f(x,   y,   0);
		GL11.glTexCoord2f(0, 1); GL11.glVertex3f(x+w, y,   0);
		GL11.glTexCoord2f(0, 0); GL11.glVertex3f(x+w, y+h, 0);
		GL11.glTexCoord2f(1, 0); GL11.glVertex3f(x,   y+h, 0);
		
		float shadow = 0.008f;
		x += shadow;
		y -= shadow;
		GlStateManager.color(0, 0, 0, 0.15f);
		GL11.glTexCoord2f(1, 1); GL11.glVertex3f(x,   y,   -0.01f);
		GL11.glTexCoord2f(0, 1); GL11.glVertex3f(x+w, y,   -0.01f);
		GL11.glTexCoord2f(0, 0); GL11.glVertex3f(x+w, y+h, -0.01f);
		GL11.glTexCoord2f(1, 0); GL11.glVertex3f(x,   y+h, -0.01f);
		
		GL11.glEnd();
	}
	
	private static void renderArms(EnumHandSide handSide, float angle, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(20, 20, 20);
		ItemRenderHelper.applyBobbing(partialTicks);

		if (handSide == EnumHandSide.RIGHT) {
			GlStateManager.translate(-0.3, -0.4, 0.0);
			GlStateManager.rotate(25, 0, 0, 1);
			
			GlStateManager.translate(-0.15*angle, -0.5*angle, 0.0);
			GlStateManager.rotate(10*angle, 0, 0, 1);
		}
		
		else {
			GlStateManager.translate(0.3, -0.4, 0.0);
			GlStateManager.rotate(-25, 0, 0, 1);
			
			GlStateManager.translate(0.15*angle, -0.5*angle, 0.0);
			GlStateManager.rotate(-10*angle, 0, 0, 1);
		}
		
		ItemRenderHelper.renderArmFirstPersonSide(0, handSide, 0, null);
		GlStateManager.popMatrix();
	}
}
