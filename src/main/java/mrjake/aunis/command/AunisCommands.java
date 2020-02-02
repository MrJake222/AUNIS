package mrjake.aunis.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class AunisCommands {
	
	private static List<CommandBase> commands = Arrays.asList(
			new CommandStargateQuery(),
			new CommandStargateCloseAll());
	
	public static void registerCommands(FMLServerStartingEvent event) {
		for (CommandBase command : commands) {
			event.registerServerCommand(command);
		}
	}
}
