package mrjake.aunis.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

public class CommandStargateCloseAll extends CommandBase {
	
	@Override
	public String getName() {
		return "sgcloseall";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/sgcloseall";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		boolean limitWorld = false;
		int worldId = 0;
		boolean force = false;
		
		if (args.length == 2) {
			if (args[1].equals("force")) {
				force = true;
			}
			
			else {
				notifyCommandListener(sender, this, "Closing all Stargates in all dimensions");
			}
		}
		
		if (args.length >= 1) {
			if (args[0].equals("force")) {
				force = true;
			}
			
			else if (args[0].equals("world")) {
				limitWorld = true;
				worldId = sender.getEntityWorld().provider.getDimension();
				
				notifyCommandListener(sender, this, "Closing Stargates in current dimension[id="+worldId+"]");
			}
			
			else {
				try {
					worldId = Integer.parseInt(args[0]);
					limitWorld = true;
					
					notifyCommandListener(sender, this, "Closing Stargates in dimension id="+worldId);
				}
				
				catch (NumberFormatException e) {
					throw new WrongUsageException("commands.sgcloseall.usage", new Object[0]);
				}
			}
		}
		
		if (args.length == 0) 		
			notifyCommandListener(sender, this, "Closing all Stargates in all dimensions");
		
		StargateNetwork network = StargateNetwork.get(sender.getEntityWorld());
		
		int closed = 0;
		
		List<StargateAddress> toBeRemoved = new ArrayList<StargateAddress>();
		
		for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {
			Map<StargateAddress, StargatePos> map = network.getMap().get(symbolType);
			
			for (StargateAddress address : map.keySet()) {
				StargatePos stargatePos = network.getStargate(address);
				
				if (limitWorld && stargatePos.dimensionID != worldId)
					continue;
								
				StargateAbstractBaseTile gateTile = stargatePos.getTileEntity();
				
				if (gateTile != null) {					
					if (gateTile.getStargateState().initiating() || (force && gateTile.getStargateState().engaged())) {
						gateTile.attemptClose();
						closed++;
					}
				}
				
				else {
					toBeRemoved.add(address);
				}
			}
		}
		
		for (StargateAddress address : toBeRemoved) {
			network.removeStargate(address);
			Aunis.info("Removing address " + address);
		}
		
		notifyCommandListener(sender, this, "Closed " + closed + " gates.");
	}

}
