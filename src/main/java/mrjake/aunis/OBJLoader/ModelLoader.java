package mrjake.aunis.OBJLoader;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class ModelLoader {
	
	public enum EnumModel {
		B0("b0", "dhd/0.obj", null),
		B1("b1", "dhd/1.obj", null),
		B2("b2", "dhd/2.obj", null),
		B3("b3", "dhd/3.obj", null),
		B4("b4", "dhd/4.obj", null),
		B5("b5", "dhd/5.obj", null),
		B6("b6", "dhd/6.obj", null),
		B7("b7", "dhd/7.obj", null),
		B8("b8", "dhd/8.obj", null),
		B9("b9", "dhd/9.obj", null),
		B10("b10", "dhd/10.obj", null),
		B11("b11", "dhd/11.obj", null),
		B12("b12", "dhd/12.obj", null),
		B13("b13", "dhd/13.obj", null),
		B14("b14", "dhd/14.obj", null),
		B15("b15", "dhd/15.obj", null),
		B16("b16", "dhd/16.obj", null),
		B17("b17", "dhd/17.obj", null),
		B18("b18", "dhd/18.obj", null),
		B19("b19", "dhd/19.obj", null),
		B20("b20", "dhd/20.obj", null),
		B21("b21", "dhd/21.obj", null),
		B22("b22", "dhd/22.obj", null),
		B23("b23", "dhd/23.obj", null),
		B24("b24", "dhd/24.obj", null),
		B25("b25", "dhd/25.obj", null),
		B26("b26", "dhd/26.obj", null),
		B27("b27", "dhd/27.obj", null),
		B28("b28", "dhd/28.obj", null),
		B29("b29", "dhd/29.obj", null),
		B30("b30", "dhd/30.obj", null),
		B31("b31", "dhd/31.obj", null),
		B32("b32", "dhd/32.obj", null),
		B33("b33", "dhd/33.obj", null),
		B34("b34", "dhd/34.obj", null),
		B35("b35", "dhd/35.obj", null),
		B36("b36", "dhd/36.obj", null),
		B37("b37", "dhd/37.obj", null),
		
		BRB("b38", "dhd/BRB.obj", null),
		
		DHD_MODEL("DHDModel", "dhd/DHD.obj", "dhd/dhd.png"),
		
		GATE_MODEL("GateModel", "stargate/gate.obj", "stargate/darkmetal2048.png"),
		RING_MODEL("RingModel", "stargate/ring.obj", "stargate/texturering.png"),
		
		ChevronLight("ChevronLight", "stargate/chevron/chevronLight.obj", "stargate/chevron/chevmap0.png"),
		ChevronFrame("ChevronFrame", "stargate/chevron/chevronFrame.obj", "stargate/chevron/chevmap10.png"),
		ChevronMoving("ChevronMoving", "stargate/chevron/chevronMoving.obj", "stargate/chevron/chevmap10.png"),
		
		CrystalInfuser("CrystalInfuser", "crystalinfuser/stand1.obj", "stargate/darkmetal2048.png");
		
		private String name;
		private String modelPath;
		private String texturePath;
		
		private EnumModel(String name, String path, String texturePath) {
			this.name = name;
			this.modelPath = "assets/aunis/models/" + path;
			this.texturePath = texturePath;
		}
		
		public String getName() {
			return name;
		}
		
		public String getPath() {
			return modelPath;
		}
		
		public String getTexturePath() {
			return texturePath;
		}
	}
	
	public static void bindTexture(EnumModel model) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation( "aunis:textures/tesr/" + model.getTexturePath() ));
	}
	
	public static void bindTexture(String texture) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation( "aunis:textures/tesr/" + texture ));
	}
	
	private Map<String, Boolean> loadAttempted = new HashMap<>();
	private Map<String, ModelLoaderThread> threads = new HashMap<>();
		
	public Model getModel(EnumModel model) {
		String name = model.getName();

		try {
			return threads.get(name).getModel();
		}
		catch (NullPointerException e) {
			boolean loadAtt;
			
			try { loadAtt = loadAttempted.get(name); }
			catch (NullPointerException e2) { loadAtt = false; }
			
			if ( !loadAtt ) {
				loadModel(model);
				loadAttempted.put( name, true );
			}
			return null;
		}
	}
	
	public void loadModel(EnumModel model) {
		String name = model.getName();
		
		threads.put(name, new ModelLoaderThread( model.getPath() ));

		threads.get(name).setPriority(Thread.MIN_PRIORITY);
		threads.get(name).start();
	}
}
