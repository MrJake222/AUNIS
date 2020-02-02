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
		notifyCommandListener(sender, this, "Stargates:");
		Map<Long, StargatePos> stargates = StargateNetwork.get(sender.getEntityWorld()).queryStargates();
		
		for (long serialized : stargates.keySet()) {
			List<EnumSymbol> address = EnumSymbol.toSymbolList(EnumSymbol.fromLong(serialized));
			StargatePos stargatePos = stargates.get(serialized);
			BlockPos pos = stargatePos.getPos();
			
			String addr = address.toString().replace("[", "").replace("]", "");
			String posStr = "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
			
			notifyCommandListener(sender, this, posStr + ": " + TextFormatting.AQUA + addr + ", " + TextFormatting.DARK_PURPLE + stargatePos.get7thSymbol().name + "\n");
		}
	}

}
