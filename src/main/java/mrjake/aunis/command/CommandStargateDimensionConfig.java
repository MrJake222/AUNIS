package mrjake.aunis.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandStargateDimensionConfig extends CommandBase {
	
	@Override
	public String getName() {
		return "sgdimconfig";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/sgdimconfig";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//		for (int i=0; i<args.length; i++) {
//			if (args[i].startsWith("dim=")) {
//				checkDim = true;
//				String sub = args[i].substring(4);
//				
//				if (sub.equals("current"))
//					dimId = sender.getEntityWorld().provider.getDimension();
//				else
//					dimId = Integer.valueOf(sub);
//				
//				break;
//			}
//			
//			else if (args[i].startsWith("force=")) {
//				if (Boolean.valueOf(args[i].substring(6))) {
//					force = true;
//				}
//			}
//		}
	}
}
