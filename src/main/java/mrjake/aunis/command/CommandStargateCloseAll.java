package mrjake.aunis.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mrjake.aunis.packet.stargate.StargateRenderingUpdatePacketToServer;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
		
		if (args.length == 0) 		
			notifyCommandListener(sender, this, "Closing all Stargates in all dimensions");
		else if (args.length == 1) {
			if (args[0].equals("world")) {
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
		
		else {
			throw new WrongUsageException("commands.sgcloseall.usage", new Object[0]);
		}
		
		Map<Long, StargatePos> stargates = StargateNetwork.get(sender.getEntityWorld()).queryStargates();
		int closed = 0;
		
		List<Long> toBeRemoved = new ArrayList<Long>();
		
		for (long serialized : stargates.keySet()) {
			StargatePos stargatePos = stargates.get(serialized);
			World world = stargatePos.getWorld();
			
			if (limitWorld && world.provider.getDimension() != worldId)
				continue;
			
			BlockPos pos = stargatePos.getPos();
			
			TileEntity tileEntity = world.getTileEntity(pos);
			
			if (tileEntity instanceof StargateAbstractBaseTile) {
				StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) tileEntity;
				
				if (gateTile.getStargateState().initiating()) {
					StargateRenderingUpdatePacketToServer.closeGatePacket(gateTile, false);
					closed++;
				}
			}
			
			else {
				toBeRemoved.add(serialized);
			}
		}
		
		for (long remove : toBeRemoved)
			StargateNetwork.get(sender.getEntityWorld()).queryStargates().remove(remove);
		
		notifyCommandListener(sender, this, "Closed " + closed + " gates.");
	}

}
