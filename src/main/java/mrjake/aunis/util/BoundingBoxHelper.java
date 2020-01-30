package mrjake.aunis.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoundingBoxHelper {
	
	@SideOnly(Side.CLIENT)
	public static void render(double x, double y, double z, AxisAlignedBB boudingBox) {
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 0, 0);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		
		GlStateManager.translate(x, y, z);
		
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,GL11.GL_LINE);
		GlStateManager.glBegin(GL11.GL_QUADS);
		
		GL11.glColor3f(0.0f,1.0f,0.0f);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.maxY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.maxY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.maxY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.maxY, boudingBox.maxZ);
	    
	    GL11.glColor3f(1.0f,0.5f,0.0f);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.minY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.minY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.minY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.minY, boudingBox.minZ);
	    
	    GL11.glColor3f(1.0f,0.0f,0.0f);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.maxY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.maxY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.minY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.minY, boudingBox.maxZ);
	    
	    GL11.glColor3f(1.0f,1.0f,0.0f);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.minY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.minY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.maxY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.maxY, boudingBox.minZ);
	    
	    GL11.glColor3f(0.0f,0.0f,1.0f);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.maxY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.maxY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.minY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.minX, boudingBox.minY, boudingBox.maxZ);
	    
	    GL11.glColor3f(1.0f,0.0f,1.0f);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.maxY, boudingBox.minZ);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.maxY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.minY, boudingBox.maxZ);
	    GL11.glVertex3d(boudingBox.maxX, boudingBox.minY, boudingBox.minZ);
		
		GlStateManager.glEnd();
		
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,GL11.GL_FILL);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
	
	public static AxisAlignedBB rotate(AxisAlignedBB in, int angle) {
		switch (angle) {
			case 0:
				return in;
				
			case 90:
				return new AxisAlignedBB(-in.minZ, in.minY, in.minX, -in.maxZ, in.maxY, in.maxX);
			
			case 180:
				return new AxisAlignedBB(-in.minX, in.minY, -in.minZ, -in.maxX, in.maxY, -in.maxZ);
				
			case 270:
			case -90:
				return new AxisAlignedBB(in.minZ, in.minY, -in.minX, in.maxZ, in.maxY, -in.maxX);
				
			default:
				throw new IllegalArgumentException("Angle not one of [0, 90, 180, 270, -90]");
		}
	}
}
