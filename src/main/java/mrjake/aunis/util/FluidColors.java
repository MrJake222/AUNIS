package mrjake.aunis.util;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class FluidColors {
	
	private static Map<Fluid, FloatColors> fluidColorMap = new HashMap<>();
	private static Set<Fluid> failedSet = new HashSet<>();
	
	@Nullable
	public static FloatColors getAverageColor(Fluid fluid) {
		if (!fluidColorMap.containsKey(fluid))
			load(fluid);
		
		return fluidColorMap.get(fluid);
	}
	
	private static void load(Fluid fluid) {
		if (failedSet.contains(fluid))
			return;
		
		try {
			ResourceLocation resourceLocation = new ResourceLocation(fluid.getStill().getResourceDomain(), "textures/" + fluid.getStill().getResourcePath() + ".png");
			IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
			BufferedImage bufferedImage = TextureUtil.readBufferedImage(resource.getInputStream());
			Raster raster = bufferedImage.getRaster();
			
			float[] colorsOut = new float[3];
			float[] clr = new float[4];
			
			final int size = Math.min(raster.getWidth(), raster.getHeight());

			// Get the average color of the texture
			for (int xp=0; xp<size; xp++) {
				for (int yp=0; yp<size; yp++) {
					raster.getPixel(xp, yp, clr);
										
					for (int i=0; i<3; i++) {
						colorsOut[i] += clr[i];
					}
				}
			}
			
			// Divide by pixels
			for (int i=0; i<3; i++) {
				colorsOut[i] /= size*size;
				colorsOut[i] /= 256.0f;
			}
			
			Aunis.logger.debug("Loaded fluid color for " + fluid);
			
			fluidColorMap.put(fluid, new FloatColors(colorsOut));
		}
		
		catch (IOException e) {
			Aunis.logger.error("Failed to get average fluid color for " + fluid);
			e.printStackTrace();
			
			failedSet.add(fluid);
		}
	}
	
	public static class FloatColors {
		public final float[] colors;
	
		public FloatColors(float[] colors) {
			this.colors = colors;
		}
	}
}
