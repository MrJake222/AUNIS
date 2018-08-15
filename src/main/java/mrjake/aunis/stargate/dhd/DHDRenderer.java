package mrjake.aunis.stargate.dhd;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisSoundEvents;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.block.BlockRotated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDRenderer {
	//private DHDTile te;
	private World world;
	private BlockPos pos;
	
	private float rotation;
	
	private Map<String, String> buttonTexture = new HashMap<String, String>();
	
	private int activation = -1;
	private long activationStateChange = 0;
	private String textureTemplate;
	
	private boolean clearingButtons = false;
	
	public DHDRenderer(DHDTile te) {
		this.rotation = te.getWorld().getBlockState(te.getPos()).getValue(BlockRotated.ROTATE) * -22.5f;
		//this.te = te;
		this.world = te.getWorld();
		this.pos = te.getPos();
		
		// Load button textures
		for (int k=0; k<2; k++) {
			String tex;
			
			if (k == 0)
				tex = "symbol";
			else
				tex = "brb";
			
			for (int i=0; i<=5; i++) {
				ResourceLocation resource = new ResourceLocation( "aunis:stargate/textures/dhd/"+tex+"/"+tex+i+".png" );
					
				ITextureObject itextureobject = new SimpleTexture(resource);
				Minecraft.getMinecraft().getTextureManager().loadTexture(resource, itextureobject);
			}
		}
		
		for (int i=0; i<38; i++)
			buttonTexture.put("b"+i, "dhd/symbol/symbol0.png");
			
		buttonTexture.put("b38", "dhd/brb/brb0.png");
	}
	
	public List<Boolean> getActiveButtonList() {
		List<Boolean> out = new ArrayList<Boolean>();
		
		for (int i=0; i<=38; i++) {
			String val = buttonTexture.get("b"+i);
			
			if ( val.equals("dhd/symbol/symbol0.png") || val.equals("dhd/brb/brb0.png") )
				out.add(false);
			else
				out.add(true);
		}
		
		return out;
	}
	
	public void setActiveButtons(List<Boolean> list) {
		buttonTexture.clear();
		String template;
		
		for (int i=0; i<=38; i++) {
			if (i == 38)
				template = "dhd/brb/brb";
			else
				template = "dhd/symbol/symbol";
			
			if ( list.get(i) )
				template += "5.png";
			else
				template += "0.png";
			
			buttonTexture.put("b"+i, template);
		}
	}
	
	public void activateButton(int buttonID) {
		if ( buttonID == 38 )
			AunisSoundEvents.playSound(world, pos, AunisSoundEvents.dhdPressBRB);
		else
			AunisSoundEvents.playSound(world, pos, AunisSoundEvents.dhdPress);
		
		if (activation == -1) {
			if ( buttonID == 38 )
				textureTemplate = "dhd/brb/brb";
			else
				textureTemplate = "dhd/symbol/symbol";
			
			activationStateChange = world.getTotalWorldTime();
			activation = buttonID;
		}
	}
	
	public void clearButtons() {		
		clearingButtons = true;
		activationStateChange = world.getTotalWorldTime();
		
		activation = 0;
	}
	
	public void render(double x, double y, double z, double partialTicks) {
		Model dhdModel = Aunis.modelLoader.getModel(EnumModel.DHD_MODEL);
		Model brbModel = Aunis.modelLoader.getModel(EnumModel.BRB);
		
		if (dhdModel != null && brbModel != null) {	
			glPushMatrix();
			
			glTranslated(x+0.5, y, z+0.5);
			glRotatef(rotation, 0, 1, 0);
						
			ModelLoader.bindTexture( EnumModel.DHD_MODEL );
			dhdModel.render();
			
			ModelLoader.bindTexture( buttonTexture.get( EnumModel.BRB.getName() ) );
			brbModel.render();
			
			EnumModel[] buttons = EnumModel.values();	
			for (int i=0; i<38; i++) {
				Model b = Aunis.modelLoader.getModel(buttons[i]);
				
				if (b != null) {
					ModelLoader.bindTexture( buttonTexture.get( buttons[i].getName() ) );
					b.render();
				}
			}
			
			glPopMatrix();
			
			if (activation != -1) {
				int stage;
				
				float tick = (float) (world.getTotalWorldTime() - activationStateChange + partialTicks);
				
				if (activation < 38)
					stage =  (int) (tick * 2);
				else
					stage = (int) tick;
								
				if (stage < 6) {
					if (stage < 0)
						stage = 0;
					
					if (clearingButtons) {
						for (int i=0; i<=38; i++) {
							if (i < 38) textureTemplate = "dhd/symbol/symbol";
							else textureTemplate = "dhd/brb/brb";
							
							if (!buttonTexture.get("b"+i).contains("0"))
								buttonTexture.put("b"+i, textureTemplate+(5-stage)+".png");
						}
					}
					
					else {
						buttonTexture.put("b"+activation, textureTemplate+stage+".png");
					}
				}
				
				else {
					if (clearingButtons) {
						clearingButtons = false;
					}
					
					activation = -1;
				}
			}
		}
	}
}
