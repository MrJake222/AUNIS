package mrjake.aunis.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DHDRenderer implements ISpecialRenderer<DHDRendererState> {
	//private DHDTile te;
	private World world;
	private BlockPos pos;
	
	private float rotation;
	
	private Map<String, String> buttonTexture = new HashMap<String, String>();
	
	private int activation = -1;
	private long activationStateChange = 0;
	private String textureTemplate;
	
	private boolean changingButtons = false;
	private boolean clearingButtons = false;
	public boolean brbToActivate = false;
		
	public DHDRenderer(DHDTile te) {
		this.rotation = te.getWorld().getBlockState(te.getPos()).getValue(AunisProps.ROTATION_HORIZONTAL) * -22.5f;
		//this.te = te;
		this.world = te.getWorld();
		this.pos = te.getPos();
		
		// Load DHD textures
		for (int k=0; k<2; k++) {
			
			String tex = (k == 0 ? "symbol" : "brb");
			
			for (int i=0; i<=5; i++) {
				ModelLoader.getTexture("dhd/" + tex + "/" + tex+i + ".png");
			}
		}
		
		initTextureList();
	}
	
	@Override
	public float getHorizontalRotation() {
		return rotation;
	}
	
	@Override
	public void setState(DHDRendererState rendererState) {
		setActiveButtons(rendererState.activeButtons);
	}
	
	private void initTextureList() {
		for (int i=0; i<38; i++)
			buttonTexture.put("b"+i, "dhd/symbol/symbol0.png");
			
		buttonTexture.put("b38", "dhd/brb/brb0.png");
	}

	
	private void setActiveButtons(List<Integer> list) {
		initTextureList();
		String template;
		
		for (int id : list) {
			if (id == 38)
				template = "dhd/brb/brb5.png";
			else
				template = "dhd/symbol/symbol5.png";
			
			buttonTexture.put("b"+id, template);
		}
	}
	
	private List<Integer> toActivate;
	
	public void smoothlyActivateButtons(List<Integer> toActivate) {
		this.toActivate = toActivate;
		
		clearingButtons = false;
		changeButtons();
	}
	
	public void activateButton(int buttonID, boolean sound) {
		if (sound) {
			if ( buttonID == 38 )
				AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.dhdPressBRB, 0.5f);
			else
				AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.dhdPress, 0.5f);
		}
		
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
		changeButtons();
	}
	
	private void changeButtons() {		
		changingButtons = true;
		
		activationStateChange = world.getTotalWorldTime();
		
		activation = 0;
	}
	
//	private boolean doInsertAnimation = false;
//	private boolean doRemovalAnimation = false;
//	private boolean doUpgradeRender = false;
//	private long insertionTime;
	
//	@Override
//	public void upgradeInteract(boolean hasUpgrade, boolean isHoldingUpgrade) {
//		if (hasUpgrade) {
//			if (doUpgradeRender) {
//				// Removing upgrade from slot				
//				doUpgradeRender = false;
//				AunisPacketHandler.INSTANCE.sendToServer( new UpgradeTileUpdateToServer(pos, false) );
//			}
//			
//			else {
//				// Sliding out upgrade
//				if (!doRemovalAnimation) {
//					insertionTime = world.getTotalWorldTime();
//					doRemovalAnimation = true;
//					doUpgradeRender = true;
//				}
//			}
//		}
//		
//		else {
//			if (doUpgradeRender) {
//				// Inserting upgrade into DHD
//				if (!doInsertAnimation) {
//					insertionTime = world.getTotalWorldTime();
//					doInsertAnimation = true;
//				}
//			}
//			
//			else {
//				// Putting upgrade in slot
//				if (isHoldingUpgrade) {
//					doUpgradeRender = true;
//				}
//			}
//		}
//	}
	
	@Override
	public void render(double x, double y, double z, double partialTicks) {
		Model dhdModel = ModelLoader.getModel(EnumModel.DHD_MODEL);
		Model brbModel = ModelLoader.getModel(EnumModel.BRB);
		
		if (dhdModel != null && brbModel != null) {	
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x+0.5, y, z+0.5);
			GlStateManager.rotate(rotation, 0, 1, 0);
						
			EnumModel.DHD_MODEL.bindTexture();
			dhdModel.render();
			
			ModelLoader.bindTexture( buttonTexture.get( EnumModel.BRB.getName() ) );
			brbModel.render();
			
			EnumModel[] buttons = EnumModel.values();	
			for (int i=0; i<38; i++) {
				Model b = ModelLoader.getModel(buttons[i]);
				
				if (b != null) {
					ModelLoader.bindTexture( buttonTexture.get( buttons[i].getName() ) );
					b.render();
				}
			}
			
			GlStateManager.popMatrix();
			
			// -----------------------------------------------------------------
			// Logic code
			
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
					
					if (changingButtons) {
						for (int i=0; i<=38; i++) {
							if (i < 38) textureTemplate = "dhd/symbol/symbol";
							else textureTemplate = "dhd/brb/brb";
							
							if (clearingButtons) {
								if (!buttonTexture.get("b"+i).contains("0")) {
									buttonTexture.put("b"+i, textureTemplate+(5-stage)+".png");
								}
							}
							
							else {
								if ( toActivate.contains(i) ) {
									buttonTexture.put("b"+i, textureTemplate+stage+".png");
								}
							}	
						}
					}
					
					else {
						buttonTexture.put("b"+activation, textureTemplate+stage+".png");
					}
				}
				
				else {
					if (changingButtons) {
						changingButtons = false;
						
						if (clearingButtons) {
							setActiveButtons(new ArrayList<>());
						}
						
						else {
							setActiveButtons(toActivate);
						}
					}
					
					// When activating remotely, we need to take into account that gate will not be rendered at the time
					// hence manual activation
					if (brbToActivate) {
						brbToActivate = false;
						buttonTexture.put("b38", "dhd/brb/brb5.png");
					}
					
					activation = -1;
				}
			}
		}
	}
}
