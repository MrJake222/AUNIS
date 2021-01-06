package mrjake.aunis.item.renderer;

import mrjake.aunis.Aunis;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This interface allows for automatic registration of
 * TEISR.
 * 
 * @author MrJake222
 *
 */
public interface CustomModelItemInterface {
		
	default public void registerCustomModel(IRegistry<ModelResourceLocation, IBakedModel> registry) {
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(((Item) this).getRegistryName(), "inventory");
		
		IBakedModel defaultModel = registry.getObject(modelResourceLocation);
		CustomModel customModel = new CustomModel(defaultModel);
		setCustomModel(customModel);
		
		registry.putObject(modelResourceLocation, customModel);
	}
	
	default public void setCustomModelLocation() {
		ModelLoader.setCustomModelResourceLocation((Item) this, 0, new ModelResourceLocation(((Item) this).getRegistryName(), "inventory"));
	}
	
	default public void setTEISR() {
		Aunis.proxy.setTileEntityItemStackRenderer((Item) this);
	}
	
	abstract void setCustomModel(CustomModel customModel);
	
	/**
	 * 
	 * @return New TEISR instance.
	 */
	@SideOnly(Side.CLIENT)
	public abstract TileEntityItemStackRenderer createTEISR();
}
