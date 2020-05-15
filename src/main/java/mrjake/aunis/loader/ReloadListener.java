package mrjake.aunis.loader;

import java.io.IOException;
import java.util.function.Predicate;

import mrjake.aunis.Aunis;
import mrjake.aunis.loader.model.ModelLoader;
import mrjake.aunis.loader.texture.TextureLoader;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;

public class ReloadListener implements ISelectiveResourceReloadListener {

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {		
    	try {
			if (resourcePredicate.test(VanillaResourceType.MODELS)) {
				ModelLoader.reloadModels();
			}
			
			if (resourcePredicate.test(VanillaResourceType.TEXTURES)) {
				TextureLoader.reloadTextures(resourceManager);
			}
    	}
    	
    	catch (IOException e) {
    		Aunis.logger.error("Failed reloading resources");
    		e.printStackTrace();
    	}
	}

}
