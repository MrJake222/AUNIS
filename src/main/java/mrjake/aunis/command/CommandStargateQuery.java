package mrjake.aunis.command;

import java.util.Map;

import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
		AxisAlignedBB queryBox = null;
		SymbolTypeEnum symbolType = null;
		
		boolean checkDim = false;
		int dimId = 0;
		int idCheck = -1;
		
		try {
			if (args.length >= 6) {
				BlockPos sPos = new BlockPos(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]));
				BlockPos tPos = new BlockPos(Integer.valueOf(args[3]), Integer.valueOf(args[4]), Integer.valueOf(args[5]));
				
				queryBox = new AxisAlignedBB(sPos, tPos);
			
			}
			
			for (int i=0; i<args.length; i++) {
				if (args[i].startsWith("dim=")) {
					checkDim = true;
					dimId = Integer.valueOf(args[i].substring(4));
				}
				
				else if (args[i].startsWith("map=")) {
					symbolType = SymbolTypeEnum.valueOf(args[i].substring(4).toUpperCase());
				}
				
				else if (args[i].startsWith("id=")) {
					idCheck = Integer.valueOf(args[i].substring(3));
				}
			}
			
		}
		
		catch (NumberFormatException e) {
			throw new WrongUsageException("Number expected");
		}
		
		catch (IllegalArgumentException e) {
			throw new WrongUsageException("No such map");
		}
		
		String infoString = " [dim=" + (checkDim ? dimId : "any") + ", ";
		infoString += "map=" + (symbolType != null ? symbolType.toString() : "no") + ", ";
		infoString += "id=" + (idCheck != -1 ? idCheck : "any") + ", ";
		infoString += "box=" + (queryBox != null ? queryBox.toString() : "any") + "]:";
		notifyCommandListener(sender, this, TextFormatting.AQUA + "Stargates" + infoString);
		
		StargateNetwork network = StargateNetwork.get(sender.getEntityWorld());
		Map<StargateAddress, StargatePos> map = network.getMap().get(symbolType != null ? symbolType : SymbolTypeEnum.MILKYWAY);
		
		int id = 1;
		
		for (StargateAddress address : map.keySet()) {
			if (checkDim && map.get(address).dimensionID != dimId)
				continue;
			
			BlockPos pos = map.get(address).gatePos;
			
			if (queryBox != null && !queryBox.contains(new Vec3d(pos)))
				continue;
			
			if (idCheck == -1 || id == idCheck) {
				String gateString = " " + id + ". [";
				gateString += "x=" + pos.getX() + ", ";
				gateString += "y=" + pos.getY() + ", ";
				gateString += "z=" + pos.getZ() + "]";
				
				if (symbolType != null) {			
					gateString += ": " + TextFormatting.DARK_GREEN;
					
					for (int i=0; i<8; i++) {
						if (i >= 6)
							gateString += TextFormatting.DARK_PURPLE;
						
						if (symbolType == SymbolTypeEnum.UNIVERSE)
							gateString += address.get(i).toString();
						else
							gateString += address.get(i).localize();
						
						if (i < 7)
							gateString += ", ";
					}
				}
				
				notifyCommandListener(sender, this, gateString);
				
				if (symbolType != null && symbolType != SymbolTypeEnum.UNIVERSE)
					notifyCommandListener(sender, this, "");
			}
			
			id++;
		}
	}

}
