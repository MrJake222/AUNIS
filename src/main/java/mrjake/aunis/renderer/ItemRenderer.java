package mrjake.aunis.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

/**
 * This class helps in rendering casual items
 */
public class ItemRenderer {
	
	private ItemStack stack;
	private IBakedModel model;
	
	/**
	 * Create needed parameters
	 * 
	 * @param stack - ItemStack instance to be rendered
	 */
	public ItemRenderer(ItemStack stack) {		
		this.stack = stack;
		
		model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);
		model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);
	}
	
	public ItemRenderer(Item item) {
		this(new ItemStack(item));
	}

	/**
	 * Render item
	 */
	public void render() {			
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
	}
}
