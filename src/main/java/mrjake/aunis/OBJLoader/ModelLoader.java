package mrjake.aunis.OBJLoader;

import java.util.HashMap;
import java.util.Map;

public class ModelLoader {
	
	private static final Map<ModelEnum, OBJModel> LOADED_MODELS = new HashMap<>();
	
	public static OBJModel getModel(ModelEnum modelEnum) {
		if (!LOADED_MODELS.containsKey(modelEnum))
			LOADED_MODELS.put(modelEnum, OBJLoader.loadModel(modelEnum.modelPath));
		
		return LOADED_MODELS.get(modelEnum);
	}

	public static void reloadModels() {
		LOADED_MODELS.clear();
	}
}
