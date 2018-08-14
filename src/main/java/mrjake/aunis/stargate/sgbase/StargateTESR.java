package mrjake.aunis.stargate.sgbase;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class StargateTESR extends TileEntitySpecialRenderer<StargateBaseTile> {
	
	private float gateDiameter = 10.1815f;
	
	@Override
	public void render(StargateBaseTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {	
		x += 0.50;
		y += gateDiameter/2;
		z += 0.50;
		
		te.getRenderer().render(x, y, z, partialTicks);
		
		/*double offset = (Math.sin((te.getWorld().getTotalWorldTime() + partialTicks) / 8) + 1) / 2;
		
		renderGate(x, y, z, horizontalRotation);
		renderRing(x, y, z, horizontalRotation);
		renderChevrons(x, y, z, horizontalRotation, offset);*/
	}
	
	
	/*private float ringAngularRotation = 0;
	
	private Vec3d ringLoc = new Vec3d(0.0, -0.122333, -0.000597);
	
	public enum EnumChevron {
		C1(1),
		C2(2),
		C3(3),
		
		C4(6),
		C5(7),
		C6(8),
		
		C7(4),
		C8(5),
		
		C9(0);
		
		public int index;
		public int rotation;
		
		EnumChevron(int index) {
			this.index = index;
			this.rotation = 40*index;
		}
		
		public static int toGlobal(int index) {
			return values()[index].index;
		}
		
		public EnumChevron fromOrderedIndex(int orderedIndex) {
			if (orderedIndex > 0 && orderedIndex < 4) return values()[orderedIndex];
			else if (orderedIndex >= 4 && orderedIndex < 7) return values()[orderedIndex+2];
			else if (orderedIndex >= 7 && orderedIndex < 9) return values()[orderedIndex-3];
			else if (orderedIndex == 9) return values()[0];
		}
	}
	
	private void renderGate(double x, double y, double z, int horizontalRotation) {
		Model gateModel = Aunis.modelLoader.getModel( EnumModel.GATE_MODEL );
		
		if ( gateModel != null ) {
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x, y, z);
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			ModelLoader.bindTexture( EnumModel.GATE_MODEL.getTexturePath() );
			gateModel.render();
			
			GlStateManager.popMatrix();
		}
	}
	
	private void renderRing(double x, double y, double z, int horizontalRotation) {
		Model ringModel = Aunis.modelLoader.getModel(EnumModel.RING_MODEL);
		
		if (ringModel != null) {
			GlStateManager.pushMatrix();
			
			
			if (horizontalRotation == 90 || horizontalRotation == 270) {
				GlStateManager.translate(x+ringLoc.y, y+ringLoc.z, z+ringLoc.x);
				GlStateManager.rotate(ringAngularRotation, 1, 0, 0);
				GlStateManager.translate(-ringLoc.y, -ringLoc.z, -ringLoc.x);
			}
			else {
				GlStateManager.translate(x+ringLoc.x, y+ringLoc.z, z+ringLoc.y);
				GlStateManager.rotate(ringAngularRotation, 0, 0, 1);
				GlStateManager.translate(-ringLoc.x, -ringLoc.z, -ringLoc.y);
			}
			
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			ModelLoader.bindTexture( EnumModel.RING_MODEL.getTexturePath() );
			ringModel.render();
			
			GlStateManager.popMatrix();
		}
		
		ringAngularRotation += 0.3;
	}
	
	private void renderChevron(double x, double y, double z, int horizontalRotation, int angularPosition, boolean engaged, String resourceName) {
		Model ChevronLight = Aunis.modelLoader.getModel( EnumModel.ChevronLight );
		Model ChevronFrame = Aunis.modelLoader.getModel( EnumModel.ChevronFrame );
		Model ChevronMoving = Aunis.modelLoader.getModel( EnumModel.ChevronMoving );
		
		if ( ChevronLight != null && ChevronFrame != null && ChevronMoving != null ) {
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x, y, z);
			
			if (horizontalRotation == 0 || horizontalRotation == 90)
				angularPosition *= -1;
			
			if (horizontalRotation == 90 || horizontalRotation == 270)
				GlStateManager.rotate(angularPosition, 1, 0, 0);
			else 
				GlStateManager.rotate(angularPosition, 0, 0, 1);
			
			GlStateManager.rotate(horizontalRotation, 0, 1, 0);
			
			if (resourceName == null) {
				if (engaged)
					ModelLoader.bindTexture( EnumModel.ChevronLight.getTexturePath() );
				else
					ModelLoader.bindTexture( EnumModel.ChevronFrame.getTexturePath() );
			}
			
			else {
				ModelLoader.bindTexture( resourceName );
			}

			ChevronLight.render();
			ChevronFrame.render();
			ChevronMoving.render();
			
			GlStateManager.popMatrix();
		}
	}
	
	int counter = 20;
	int counter2 = 0;
	
	private void renderChevrons(double x, double y, double z, int horizontalRotation, double time) {
		for (int i=0; i<8; i++) {
			renderChevron(x, y, z, horizontalRotation, EnumChevron.values()[i].rotation, false, null);
		}
		
		int mod = 2;
		
		String texture = "chevron/chevmap";
		texture += String.valueOf(counter/mod % 11);
		texture += ".png";
		
		renderChevron(x, y, z, horizontalRotation, EnumChevron.values()[8].rotation, false, texture);
		
		if (counter > 0)
			counter--;
		
		counter2++;
		if (counter2 > 120) {
			counter=20;
			counter2=0;
		}
	}*/
}

