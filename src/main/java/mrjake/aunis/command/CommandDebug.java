package mrjake.aunis.command;

import mrjake.aunis.stargate.StargateNetwork;
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
		notifyCommandListener(sender, this, "Last activated Orlin's gate:");
		StargateNetwork network = StargateNetwork.get(sender.getEntityWorld());
		
		if (network.hasLastActivatedOrlinAddress())
			notifyCommandListener(sender, this, network.getLastActivatedOrlinAddress().toString());
		else
			notifyCommandListener(sender, this, "null");
	}
}
