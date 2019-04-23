package mrjake.aunis.event;

import mrjake.aunis.block.AunisBlocks;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class ModelBakeHandler {

	@SubscribeEvent
	public static void onModelBakeEvent(ModelBakeEvent event) {		
		IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
		
		AunisBlocks.stargateMemberBlock.registerCustomModel(registry);
	}
}
