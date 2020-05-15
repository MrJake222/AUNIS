package mrjake.aunis.loader.texture;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;

public class Texture {
	
	private int textureId;
	
	public Texture(BufferedImage bufferedImage, boolean desaturate) {
		this.textureId = TextureUtil.glGenTextures();
		
		if (desaturate) {
			Aunis.info("Started desaturation of the event horizon texture");
			
        	WritableRaster raster = bufferedImage.getRaster();
        	
        	int w = raster.getWidth();
        	int h = raster.getHeight();
        	
        	List<RasterThread> th = Arrays.asList(
        			new RasterThread(0, 0, w/2, h/2, raster),
        			new RasterThread(w/2, 0, w/2, h/2, raster),
        			new RasterThread(w/2, h/2, w/2, h/2, raster),
        			new RasterThread(0, h/2, w/2, h/2, raster));
        	
        	for (Thread t : th)
        		t.start();
        	
        	try {
	        	th.get(0).join();
	        	th.get(1).join();
	        	th.get(2).join();
	        	th.get(3).join();
        	}
        	
        	catch (InterruptedException e) {
        		e.printStackTrace();
        	}
        	
			Aunis.info("Finished desaturation of the event horizon texture");

			// Single threaded code
//        	float[] pixel = new float[3];
//        	float gray;
//        	
//        	for (int w=0; w<raster.getWidth(); w++) {
//        		for (int h=0; h<raster.getHeight(); h++) {
//        			raster.getPixel(w, h, pixel);
//        			gray = 0;
//        			gray += pixel[0] * 0.299;
//        			gray += pixel[1] * 0.587;
//        			gray += pixel[2] * 0.114;
//        			
//        			pixel[0] = gray;
//        			pixel[1] = gray;
//        			pixel[2] = gray;
//        			
//        			raster.setPixel(w, h, pixel);
//        		}	            		
//        	}
		}
		
		TextureUtil.uploadTextureImageAllocate(textureId, bufferedImage, false, false);
	}
	
	public void deleteTexture() {
		TextureUtil.deleteTexture(textureId);
	}

	public void bindTexture() {
		GlStateManager.bindTexture(textureId);
	}
	
	private static class RasterThread extends Thread {
		
		private int x;
		private int y;
		private int w;
		private int h;
		private WritableRaster raster;

		public RasterThread(int x, int y, int w, int h, WritableRaster raster) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.raster = raster;
		}
		
		@Override
		public void run() {
			Aunis.info(String.format("Starting thread (%d, %d) to (%d, %d)", x, y, x+w, y+h));
			
			float[] pixel = new float[3];
        	float gray;
			
			for (int w=x; w<x+this.w; w++) {
        		for (int h=y; h<y+this.h; h++) {
        			raster.getPixel(w, h, pixel);
        			if (pixel[0] == 0 && pixel[1] == 0 && pixel[2] == 0)
        				continue;
        			
        			gray = 0;
        			gray += pixel[0] * 0.299;
        			gray += pixel[1] * 0.587;
        			gray += pixel[2] * 0.114;
        			
        			pixel[0] = gray;
        			pixel[1] = gray;
        			pixel[2] = gray;
        			
        			raster.setPixel(w, h, pixel);
        		}	            		
        	}
						
			Aunis.info(String.format("Finished thread (%d, %d) to (%d, %d)", x, y, x+w, y+h));
		}
	}
}