package mrjake.aunis.event;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.renderer.CustomModelItemInterface;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class ModelBakeHandler {

	@SubscribeEvent
	public static void onModelBakeEvent(ModelBakeEvent event) {		
		IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
		
		AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK.registerCustomModel(registry);
		AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK.registerCustomModel(registry);
		
		for (Item item : AunisItems.getItems()) {
			if (item instanceof CustomModelItemInterface) {
				Aunis.logger.debug("Registering custom model for: " + item);
				
				((CustomModelItemInterface) item).registerCustomModel(registry);
			}
		}
	}
}
