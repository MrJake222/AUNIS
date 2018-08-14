package mrjake.aunis.event.registry;

import mrjake.aunis.init.AunisBlocks;
import mrjake.aunis.init.AunisItems;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class ModelRegistryHandler {
 
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        AunisBlocks.initBlockModels();
        AunisItems.initItemModels();
    }
}
