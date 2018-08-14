package mrjake.aunis.OBJLoader;

public class ModelLoaderThread extends Thread {

	private String modelPath;
	private Model model;
	public boolean finished;
	
	public ModelLoaderThread(String modelPath) {
		model = null;
		finished = false;
		
		this.modelPath = modelPath;
	}
	
	public Model getModel() {
		if (finished)
			return model;
		else
			return null;
	}
	
	@Override
	public void run() {
		//Aunis.info("Started thread for modelPath: " + modelPath);
		Loader loader = new Loader();
		
		model = loader.loadModel(modelPath);
		//Aunis.info("Finished thread for modelPath: " + modelPath);
		finished = true;
	}

}
