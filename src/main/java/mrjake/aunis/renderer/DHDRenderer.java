package mrjake.aunis.renderer;

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
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.upgrade.UpgradeTileUpdateToServer;
import mrjake.aunis.renderer.state.DHDRendererState;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public class DHDRenderer implements Renderer<DHDRendererState> {
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
				ResourceLocation resource = new ResourceLocation( "aunis:textures/tesr/dhd/"+tex+"/"+tex+i+".png" );
					
				ITextureObject itextureobject = new SimpleTexture(resource);
				Minecraft.getMinecraft().getTextureManager().loadTexture(resource, itextureobject);
			}
		}
		
		initTextureList();
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
	
	public List<Integer> getActiveButtons() {
		List<Integer> out = new ArrayList<Integer>();
		
		for (int i=0; i<=38; i++) {
			String val = buttonTexture.get("b"+i);
			
			if ( !val.equals("dhd/symbol/symbol0.png") && !val.equals("dhd/brb/brb0.png") )
				out.add(i);
			
		}
		
		return out;
	}
	
	public void setActiveButtons(List<Integer> list) {
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
		changeButtons();
	}
	
	private void changeButtons() {		
		changingButtons = true;
		
		activationStateChange = world.getTotalWorldTime();
		
		activation = 0;
	}
	
	private boolean doInsertAnimation = false;
	private boolean doRemovalAnimation = false;
	private boolean doUpgradeRender = false;
	private long insertionTime;
	
	@Override
	public void upgradeInteract(boolean hasUpgrade, boolean isHoldingUpgrade) {
		if (hasUpgrade) {
			if (doUpgradeRender) {
				// Removing upgrade from slot				
				doUpgradeRender = false;
				AunisPacketHandler.INSTANCE.sendToServer( new UpgradeTileUpdateToServer(pos, false) );
			}
			
			else {
				// Sliding out upgrade
				if (!doRemovalAnimation) {
					insertionTime = world.getTotalWorldTime();
					doRemovalAnimation = true;
					doUpgradeRender = true;
				}
			}
		}
		
		else {
			if (doUpgradeRender) {
				// Inserting upgrade into DHD
				if (!doInsertAnimation) {
					insertionTime = world.getTotalWorldTime();
					doInsertAnimation = true;
				}
			}
			
			else {
				// Putting upgrade in slot
				if (isHoldingUpgrade) {
					doUpgradeRender = true;
				}
			}
		}
	}
	
	/*public boolean upgradeInSlot() {
		return doUpgradeRender;
	}*/
	
	@Override
	public void render(double x, double y, double z, double partialTicks) {
		Model dhdModel = Aunis.modelLoader.getModel(EnumModel.DHD_MODEL);
		Model brbModel = Aunis.modelLoader.getModel(EnumModel.BRB);
		
		if (dhdModel != null && brbModel != null) {	
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x+0.5, y, z+0.5);
			GlStateManager.rotate(rotation, 0, 1, 0);
						
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
			
			if (doUpgradeRender) {
				float arg = (float) ((world.getTotalWorldTime() - insertionTime + partialTicks) / 60.0);
				float mul = 1;
				
				if (doInsertAnimation)
					mul = MathHelper.cos(arg+0.31f)+0.048f;
				else if (doRemovalAnimation)
					mul = MathHelper.sin(arg) + 0.53f;
				
				GlStateManager.translate(0, 0.5, 0.5*mul);
				GlStateManager.rotate(-90, 0, 1, 0);	
				GlStateManager.rotate(45, 0, 0, 1);	
					
				ItemStack stack = new ItemStack(AunisItems.dhdControlCrystal);
					
				IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, world, null);
				model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);
			
				GlStateManager.enableBlend();
				GlStateManager.color(1, 1, 1, 0.7f);
				
				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
				
				GlStateManager.disableBlend();
				
				if (doInsertAnimation && mul < 0.53f) {
					doUpgradeRender = false;
					doInsertAnimation = false;
					
					// Upgrade inserted, send to server
					AunisPacketHandler.INSTANCE.sendToServer( new UpgradeTileUpdateToServer(pos, true) );
				}
				
				else if (doRemovalAnimation && mul > 1) {
					doRemovalAnimation = false;
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
						
						if (!clearingButtons) {
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
