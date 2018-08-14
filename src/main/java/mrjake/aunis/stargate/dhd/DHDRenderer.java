package mrjake.aunis.stargate.dhd;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisSoundEvents;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.block.BlockRotated;
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
		
		for (int i=0; i<38; i++)
			buttonTexture.put("b"+i, "dhd/symbol/symbol0.png");
			
		buttonTexture.put("b38", "dhd/brb/brb0.png");
	}
	
	public void activateButton(int buttonID) {
		if ( buttonID == 38 )
			AunisSoundEvents.playSound(world, pos, AunisSoundEvents.dhdPressBRB);
		else
			AunisSoundEvents.playSound(world, pos, AunisSoundEvents.dhdPress);
		
		if (activation == -1) {
				
			/*if ( buttonID == 38 && !buttonTexture.get(EnumModel.BRB.getName()).contains("0") ) {
				Aunis.info(buttonTexture.get(EnumModel.BRB.getName()));
				clearButtons();
			}
			
			else {*/
				if ( buttonID == 38 )
					textureTemplate = "dhd/brb/brb";
				else
					textureTemplate = "dhd/symbol/symbol";
				
				activationStateChange = world.getTotalWorldTime();
				activation = buttonID;
			// }
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
			if (activation != -1) {
				int stage;
				
				if (activation < 38)
					stage = (int) ((world.getTotalWorldTime() - activationStateChange + partialTicks) * 2);
				else
					stage = (int) (world.getTotalWorldTime() - activationStateChange + partialTicks);
				
				if (stage < 6) {
					if (clearingButtons) {
						for (int i=0; i<39; i++) {
							if (i==0) textureTemplate = "dhd/symbol/symbol";
							if (i==38) textureTemplate = "dhd/brb/brb";
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
		}
	}
}
