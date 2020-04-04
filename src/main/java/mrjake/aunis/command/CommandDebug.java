package mrjake.aunis.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandDebug extends CommandBase {

	@Override
	public String getName() {
		return "sgdebug";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/sgdebug";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {	
		
	}
}
