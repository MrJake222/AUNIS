package mrjake.aunis.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.StargateClosedReasonEnum;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
	public int getRequiredPermissionLevel() {
		return 2;
	}


	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		AxisAlignedBB queryBox = null;
		
		if (args.length >= 1 && args[0].equals("help")) {
			throw new WrongUsageException("commands.sgcloseall.usage");
		}
		
		boolean force = false;
		boolean checkDim = false;
		int dimId = 0;
		
		try {
			if (args.length >= 6) {
				BlockPos pos = sender.getPosition();
				int x1 = (int) parseCoordinate(pos.getX(), args[0], false).getResult();
				int y1 = (int) parseCoordinate(pos.getY(), args[1], 0, 255, false).getResult();
				int z1 = (int) parseCoordinate(pos.getZ(), args[2], false).getResult();
				int x2 = (int) parseCoordinate(pos.getX(), args[3], false).getResult();
				int y2 = (int) parseCoordinate(pos.getY(), args[4], 0, 255, false).getResult();
				int z2 = (int) parseCoordinate(pos.getZ(), args[5], false).getResult();
				
				BlockPos sPos = new BlockPos(x1, y1, z1);
				BlockPos tPos = new BlockPos(x2, y2, z2);
				
				queryBox = new AxisAlignedBB(sPos, tPos);
			
			}
			
			for (int i=0; i<args.length; i++) {
				if (args[i].startsWith("dim=")) {
					checkDim = true;
					String sub = args[i].substring(4);
					
					if (sub.equals("current"))
						dimId = sender.getEntityWorld().provider.getDimension();
					else
						dimId = Integer.valueOf(sub);
					
					break;
				}
				
				else if (args[i].startsWith("force=")) {
					if (Boolean.valueOf(args[i].substring(6))) {
						force = true;
					}
				}
			}
			
		}
		
		catch (NumberFormatException e) {
			throw new WrongUsageException("commands.sgquery.number_expected");
		}
		
		Aunis.info("force : " +force);
		
		notifyCommandListener(sender, this, "commands.sgcloseall.closing", checkDim ? dimId : "any", (queryBox != null ? queryBox.toString() : "box=infinite"));
		
		StargateNetwork network = StargateNetwork.get(sender.getEntityWorld());
		Map<StargateAddress, StargatePos> map = network.getMap().get(SymbolTypeEnum.MILKYWAY);
		List<StargateAddress> toBeRemoved = new ArrayList<StargateAddress>();
		
		int closed = 0;
		
		for (StargateAddress address : map.keySet()) {
			StargatePos stargatePos = network.getStargate(address);
			
			if (checkDim && stargatePos.dimensionID != dimId)
				continue;
			
			if (queryBox != null && !queryBox.contains(new Vec3d(stargatePos.gatePos)))
				continue;
							
			StargateAbstractBaseTile gateTile = stargatePos.getTileEntity();
			
			if (gateTile != null) {					
				if (gateTile.getStargateState().initiating() || (force && gateTile.getStargateState().engaged())) {
					gateTile.attemptClose(StargateClosedReasonEnum.COMMAND);
					closed++;
				}
			}
			
			else {
				toBeRemoved.add(address);
			}
		}
		
		for (StargateAddress address : toBeRemoved) {
			network.removeStargate(address);
			Aunis.logger.warn("Removing address " + address);
		}
		
		notifyCommandListener(sender, this, "commands.sgcloseall.closed", closed);
	}

}
