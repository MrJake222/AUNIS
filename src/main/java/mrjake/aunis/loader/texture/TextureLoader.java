package mrjake.aunis.loader.texture;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import mrjake.aunis.Aunis;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.loader.FolderLoader;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public class TextureLoader {

	public static final String TEXTURES_PATH = "assets/aunis/textures/tesr";
	private static final Map<ResourceLocation, Texture> LOADED_TEXTURES = new HashMap<>();
	
	public static Texture getTexture(ResourceLocation resourceLocation) {
		return LOADED_TEXTURES.get(resourceLocation);
	}
	
	public static void reloadTextures(IResourceManager resourceManager) throws IOException {		
		for (Texture texture : LOADED_TEXTURES.values())
			texture.deleteTexture();
		
		List<String> texturePaths = FolderLoader.getAllFiles(TEXTURES_PATH, ".png", ".jpg");
		ProgressBar progressBar = ProgressManager.push("Aunis - Loading textures", texturePaths.size());
		
		for (String texturePath : texturePaths) {
			texturePath = texturePath.replaceFirst("assets/aunis/", "");
			progressBar.step(texturePath);
			
			if (AunisConfig.stargateConfig.disableAnimatedEventHorizon && texturePath.equals("textures/tesr/event_horizon_animated.jpg"))
				continue;
						
			ResourceLocation resourceLocation = new ResourceLocation(Aunis.ModID, texturePath);
			IResource resource = null;
			
			try {
				resource = resourceManager.getResource(resourceLocation);
				BufferedImage bufferedImage = TextureUtil.readBufferedImage(resource.getInputStream());
				LOADED_TEXTURES.put(resourceLocation, new Texture(bufferedImage, false));
				
				if (texturePath.equals("textures/tesr/event_horizon_animated.jpg")) {
					LOADED_TEXTURES.put(new ResourceLocation(Aunis.ModID, texturePath+"_desaturated"), new Texture(bufferedImage, true));
				}
			}
			
			catch (IOException e) {
				Aunis.logger.error("Failed to load texture " + texturePath);
				e.printStackTrace();
			}
			
			finally {
	            IOUtils.closeQuietly((Closeable)resource);
			}
		}
		
		ProgressManager.pop(progressBar);
	}

	public static ResourceLocation getTextureResource(String texture) {
		return new ResourceLocation(Aunis.ModID, "textures/tesr/" + texture);
	}	
}
