package mrjake.aunis.command;

import java.util.Map;

import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandStargateQuery extends CommandBase {

	@Override
	public String getName() {
		return "sgquery";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/sgquery";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		boolean checkDim = false;
		int dimId = 0;
		
		if (args.length == 1) {
			checkDim = true;
			dimId = parseInt(args[0]);
		}
		
		StargateNetwork network = StargateNetwork.get(sender.getEntityWorld());
		
		for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {
			Map<StargateAddress, StargatePos> map = network.getMap().get(symbolType);
			
			notifyCommandListener(sender, this, symbolType + " map:");
			for (StargateAddress address : map.keySet()) {
				if (checkDim && network.getStargate(address).dimensionID != dimId)
					continue;
				
				notifyCommandListener(sender, this, " " + address.toString() + ": "+ address.hashCode());
//				notifyCommandListener(sender, this, " " + map.get(address).toString());
			}
		}
	}

}
