package mrjake.aunis.item;

import org.lwjgl.opengl.GL11;

import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class PageNotebookTEISR extends TileEntityItemStackRenderer {
	
	/**
	 * Copied from {@link ItemRenderer}
	 * 
	 */
	private void renderArmFirstPerson(float p_187456_1_, float p_187456_2_, EnumHandSide handSide) {
		Minecraft mc = Minecraft.getMinecraft();

        boolean flag = handSide != EnumHandSide.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = MathHelper.sqrt(p_187456_2_);
        float f2 = -0.3F * MathHelper.sin(f1 * (float)Math.PI);
        float f3 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
        float f4 = -0.4F * MathHelper.sin(p_187456_2_ * (float)Math.PI);
        GlStateManager.translate(f * (f2 + 0.64000005F), f3 + -0.6F + p_187456_1_ * -0.6F, f4 + -0.71999997F);
        GlStateManager.rotate(f * 45.0F, 0.0F, 1.0F, 0.0F);
        float f5 = MathHelper.sin(p_187456_2_ * p_187456_2_ * (float)Math.PI);
        float f6 = MathHelper.sin(f1 * (float)Math.PI);
        GlStateManager.rotate(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayer abstractclientplayer = mc.player;
        mc.getTextureManager().bindTexture(abstractclientplayer.getLocationSkin());
        GlStateManager.translate(f * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(f * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(f * 5.6F, 0.0F, 0.0F);
        RenderPlayer renderplayer = (RenderPlayer) mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(abstractclientplayer);
        GlStateManager.disableCull();

        if (flag)
        {
            renderplayer.renderRightArm(abstractclientplayer);
        }
        else
        {
            renderplayer.renderLeftArm(abstractclientplayer);	
        }

        GlStateManager.enableCull();
    }
	
	/**
	 * Copied from {@link ItemRenderer#renderMapFirstPersonSide}
	 * 
	 */
	private void renderArmFirstPersonSide(float p_187465_1_, EnumHandSide hand, float p_187465_3_, ItemStack stack) {
		Minecraft mc = Minecraft.getMinecraft();
		
        float f = hand == EnumHandSide.RIGHT ? 1.0F : -1.0F;
        GlStateManager.translate(f * 0.125F, -0.125F, 0.0F);

        if (!mc.player.isInvisible())
        {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(f * 10.0F, 0.0F, 0.0F, 1.0F);
            this.renderArmFirstPerson(p_187465_1_, p_187465_3_, hand);
            GlStateManager.popMatrix();
        }
    }
	
	/**
	 * Copied from {@link EntityRenderer}
	 * 
	 * @param partialTicks
	 */
	private void applyBobbing(float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		
        if (mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer)mc.getRenderViewEntity();
            float f = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f1 = -(entityplayer.distanceWalkedModified + f * partialTicks);
            float f2 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
            float f3 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
            GlStateManager.translate(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F, -Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2), 0.0F);
            GlStateManager.rotate(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
        }
    }
	
	private void renderSymbol(float x, float y, float w, float h, SymbolInterface symbol) {
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.color(1, 1, 1, 0.8f);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(symbol.getIconResource());		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(1, 1); GL11.glVertex3f(0.04f + x, 0.79f - y, 0.01f);
		GL11.glTexCoord2f(0, 1); GL11.glVertex3f(0.04f + x + w, 0.79f - y, 0.01f);
		GL11.glTexCoord2f(0, 0); GL11.glVertex3f(0.04f + x + w, 0.79f - y + h, 0.01f); // 0.2
		GL11.glTexCoord2f(1, 0); GL11.glVertex3f(0.04f + x, 0.79f - y + h, 0.01f);
		
	    GL11.glEnd();
	    
 		GlStateManager.color(1, 1, 1, 0.2f);
	    GL11.glBegin(GL11.GL_QUADS);
		
		x += symbol.getSymbolType() == SymbolTypeEnum.PEGASUS ?  0.008f : 0.01f;
		y += symbol.getSymbolType() == SymbolTypeEnum.PEGASUS ?  0.008f : 0.01f;
		
		GL11.glTexCoord2f(1, 1); GL11.glVertex3f(0.04f + x, 0.79f - y, 0.01f);
		GL11.glTexCoord2f(0, 1); GL11.glVertex3f(0.04f + x + w, 0.79f - y, 0.01f);
		GL11.glTexCoord2f(0, 0); GL11.glVertex3f(0.04f + x + w, 0.79f - y + h, 0.01f); // 0.2
		GL11.glTexCoord2f(1, 0); GL11.glVertex3f(0.04f + x, 0.79f - y + h, 0.01f);
		
	    GL11.glEnd();
	}
	
	@Override
	public void renderByItem(ItemStack stack, float partialTicks) {	
		partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		boolean mainhand = PageNotebookBakedModel.lastTransform == TransformType.FIRST_PERSON_RIGHT_HAND;
		
		EnumHandSide handSide = mainhand ? EnumHandSide.RIGHT : EnumHandSide.LEFT;
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(20,20,20);
				
		applyBobbing(partialTicks);		
		renderArmFirstPersonSide(0, handSide, 0, null);
	    GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(mainhand ? 0.5f : -0.25f, 0.2f, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("aunis:textures/gui/notebook_background.png"));		
        GlStateManager.disableLighting();
	    GL11.glBegin(GL11.GL_QUADS);
		
	    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(0.0f, 0.0f, 0.0f);
	    GL11.glTexCoord2f(0.5f, 0); GL11.glVertex3f(0.7f, 0.0f, 0.0f);
	    GL11.glTexCoord2f(0.5f, 0.71875f); GL11.glVertex3f(0.7f, 1.0f, 0.0f);
	    GL11.glTexCoord2f(0, 0.71875f); GL11.glVertex3f(0.0f, 1.0f, 0.0f);
		
	    GL11.glEnd();
	    
	    if (stack.hasTagCompound()) {
			NBTTagCompound compound = stack.getTagCompound();
			
			SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(compound.getInteger("symbolType"));
			StargateAddress stargateAddress = new StargateAddress(compound.getCompoundTag("address"));
			int maxSymbols = symbolType.getMaxSymbolsDisplay(compound.getBoolean("hasUpgrade"));
			
			for (int i=0; i<maxSymbols; i++) {
				if (symbolType == SymbolTypeEnum.UNIVERSE) {
					float x = 0.10f*(i%6);
					float y = 0.20f*(i/6) + 0.04f;
					
					renderSymbol(x, y, 0.095f, 0.2f, stargateAddress.get(i));
				}
				
				else {
					float x = 0.21f*(i%3);
					float y = 0.20f*(i/3);
					
					renderSymbol(x, y, 0.2f, 0.2f, stargateAddress.get(i));
				}
			}
			
			if (symbolType.hasOrigin())
				renderSymbol(0.21f, 0.74f, 0.2f, 0.2f, symbolType.getOrigin());
	    }
	    
        GlStateManager.enableLighting();
	    GlStateManager.popMatrix();
	}
}
