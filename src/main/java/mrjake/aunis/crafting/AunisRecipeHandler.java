package mrjake.aunis.crafting;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class AunisRecipeHandler {
	
	@SubscribeEvent
	public static void onRecipeRegister(Register<IRecipe> event) {
		event.getRegistry().register(new NotebookRecipe());
		event.getRegistry().register(new NotebookPageCloneRecipe());
		event.getRegistry().register(new UniverseDialerCloneRecipe());
	}
}
