package mrjake.aunis.command;

import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.PageNotebookItem;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
		
		if (args.length >= 1 && args[0].equals("help")) {
			throw new WrongUsageException("commands.sgquery.usage");
		}
		
		boolean checkDim = false;
		int dimId = 0;
		int idCheck = -1;
		boolean givePage = false;
		
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
					dimId = Integer.valueOf(args[i].substring(4));
				}
				
				else if (args[i].startsWith("map=")) {
					symbolType = SymbolTypeEnum.valueOf(args[i].substring(4).toUpperCase());
				}
				
				else if (args[i].startsWith("id=")) {
					idCheck = Integer.valueOf(args[i].substring(3));
				}
				
				else if (args[i].equals("page")) {
					givePage = true;
				}
			}
			
		}
		
		catch (NumberFormatException e) {
			throw new WrongUsageException("commands.sgquery.number_expected");
		}
		
		catch (IllegalArgumentException e) {
			throw new WrongUsageException("commands.sgquery.no_map");
		}
		
		String infoString = "[dim=" + (checkDim ? dimId : "any") + ", ";
		infoString += "map=" + (symbolType != null ? symbolType.toString() : "no") + ", ";
		infoString += "id=" + (idCheck != -1 ? idCheck : "any") + ", ";
		infoString += "box=" + (queryBox != null ? queryBox.toString() : "any") + "]:";
		
		notifyCommandListener(sender, this, "commands.sgquery.stargates", TextFormatting.AQUA + infoString);
		
		StargateNetwork network = StargateNetwork.get(sender.getEntityWorld());
		Map<StargateAddress, StargatePos> map = network.getMap().get(symbolType != null ? symbolType : SymbolTypeEnum.MILKYWAY);
		
		int id = 1;
		StargateAddress selectedAddress = null;
		StargatePos selectedStargatePos = null;
		
		for (StargateAddress address : map.keySet()) {
			if (checkDim && map.get(address).dimensionID != dimId)
				continue;
			
			BlockPos pos = map.get(address).gatePos;
			
			if (queryBox != null && !queryBox.contains(new Vec3d(pos)))
				continue;
			
			if (idCheck == -1 || id == idCheck) {
				selectedStargatePos = map.get(address);
				selectedAddress = address;
				
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
		
		if (givePage) {
			Aunis.info("id: " + idCheck);
			
			if (idCheck == -1 || selectedAddress == null) {
				throw new WrongUsageException("commands.sgquery.wrong_id");
			}
			
			if (symbolType == null) {
				throw new WrongUsageException("commands.sgquery.wrong_map");
			}
			
			if (!(sender instanceof EntityPlayer)) {
				throw new WrongUsageException("commands.sgquery.wrong_sender");
			}
			
			ItemStack stack = new ItemStack(AunisItems.PAGE_NOTEBOOK_ITEM, 1, 1);
			stack.setTagCompound(PageNotebookItem.getCompoundFromAddress(selectedAddress, true, PageNotebookItem.getRegistryPathFromWorld(selectedStargatePos.getWorld(), selectedStargatePos.gatePos)));
			((EntityPlayer) sender).addItemStackToInventory(stack);
			
			notifyCommandListener(sender, this, "commands.sgquery.giving_page", sender.getName());
		}
	}

}
