package mrjake.aunis.command;

import java.util.List;
import java.util.Map;

import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

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
		
		notifyCommandListener(sender, this, checkDim ? ("Stargates[dim="+dimId+"]: ") : "Stargates:");
		Map<Long, StargatePos> stargates = StargateNetwork.get(sender.getEntityWorld()).queryStargates();
		
		for (long serialized : stargates.keySet()) {
			List<EnumSymbol> address = EnumSymbol.toSymbolList(EnumSymbol.fromLong(serialized));
			StargatePos stargatePos = stargates.get(serialized);
			BlockPos pos = stargatePos.getPos();
			
			if (!checkDim || stargatePos.getDimension() == dimId) {
				String addr = address.toString().replace("[", "").replace("]", "");
				String posStr = "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ", dim="+stargatePos.getDimension()+"]";
				
				notifyCommandListener(sender, this, posStr + ": " + TextFormatting.AQUA + addr + ", " + TextFormatting.DARK_PURPLE + stargatePos.get7thSymbol().localize() + "\n");
			}
		}
	}

}
