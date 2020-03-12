package mrjake.aunis.command;

import mrjake.aunis.stargate.DimensionPowerMap;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;

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
		DimensionPowerMap.getCost(DimensionType.OVERWORLD, DimensionType.NETHER);
	}
}
