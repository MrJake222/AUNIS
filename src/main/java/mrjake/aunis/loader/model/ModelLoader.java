package mrjake.aunis.loader.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.loader.FolderLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public class ModelLoader {
	
	public static final String MODELS_PATH = "assets/aunis/models/tesr";
	private static final Map<ResourceLocation, OBJModel> LOADED_MODELS = new HashMap<>();
	
	public static OBJModel getModel(ResourceLocation resourceLocation) {
		return LOADED_MODELS.get(resourceLocation);
	}

	public static void reloadModels() throws IOException {
		LOADED_MODELS.clear();
		
		List<String> modelPaths = FolderLoader.getAllFiles(MODELS_PATH, ".obj");
		ProgressBar progressBar = ProgressManager.push("Aunis - Loading models", modelPaths.size());
		
		for (String modelPath : modelPaths) {
			String modelResourcePath = modelPath.replaceFirst("assets/aunis/", "");
			progressBar.step(modelResourcePath);
			LOADED_MODELS.put(new ResourceLocation(Aunis.ModID, modelResourcePath), OBJLoader.loadModel(modelPath));
		}
		
		ProgressManager.pop(progressBar);
	}
	
	public static ResourceLocation getModelResource(String model) {
		return new ResourceLocation(Aunis.ModID, "models/tesr/" + model);
	}
}
