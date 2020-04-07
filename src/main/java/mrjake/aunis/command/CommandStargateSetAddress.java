package mrjake.aunis.command;

import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.network.StargateAddressDynamic;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CommandStargateSetAddress extends CommandBase {

	@Override
	public String getName() {
		return "sgsetaddress";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/sgsetaddress";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		
		if (args.length < 12) {
			throw new WrongUsageException("commands.sgsetaddress.usage");
		}
		
		SymbolTypeEnum symbolType = null;
		
		if (args[3].startsWith("map=")) {
			try {
				symbolType = SymbolTypeEnum.valueOf(args[3].substring(4).toUpperCase());
			}
			
			catch (IllegalArgumentException e) {}
		}
		
		if (symbolType == null) {
			throw new WrongUsageException("commands.sgsetaddress.noaddressspace");
		}
		
		StargateAddressDynamic stargateAddress = new StargateAddressDynamic(symbolType);
		int index = 1;
		
		for (int i=args.length-8; i<args.length; i++) {
			SymbolInterface symbol = symbolType.fromEnglishName(args[i].replace("-", " "));
			
			if (symbol == null) {
				throw new WrongUsageException("commands.sgsetaddress.wrongsymbol", index);
			}
						
			if (stargateAddress.contains(symbol)) {
				throw new WrongUsageException("commands.sgsetaddress.duplicatesymbol", index);
			}
			
			stargateAddress.addSymbol(symbol);
			index++;
		}
		
		if (StargateNetwork.get(world).isStargateInNetwork(stargateAddress)) {
			throw new WrongUsageException("commands.sgsetaddress.exists");
		}
		
		BlockPos pos = sender.getPosition();
		int x1 = (int) parseCoordinate(pos.getX(), args[0], false).getResult();
		int y1 = (int) parseCoordinate(pos.getY(), args[1], 0, 255, false).getResult();
		int z1 = (int) parseCoordinate(pos.getZ(), args[2], false).getResult();
		
		BlockPos gatePos = new BlockPos(x1, y1, z1);
		TileEntity tileEntity = world.getTileEntity(gatePos);
		
		if (tileEntity instanceof StargateAbstractBaseTile) {
			StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) tileEntity;
			
			gateTile.setGateAddress(symbolType, stargateAddress.toImmutable());
			notifyCommandListener(sender, this, "commands.sgsetaddress.success", gateTile.getPos().toString(), stargateAddress.toString());
		}
		
		else
			throw new WrongUsageException("commands.sgsetaddress.notstargate");
		
//		if (args.length < 1) {
//			notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.noaddressspace").getFormattedText());
//			return;
//		}
//		
//		try {
//			SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(args[0].toUpperCase());
//			
//			if (args.length != 9) {
//				notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.wrongaddress").getFormattedText());
//				return;
//			}
//			
//			StargateAddressDynamic stargateAddress = new StargateAddressDynamic(symbolType);
//					
//			for (int i=0; i<8; i++) {
//				SymbolInterface symbol = symbolType.fromEnglishName(args[i+1].replace("-", " "));
//				
//				if (symbol == null) {
//					notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.wrongsymbol").getFormattedText(), i+1);
//					return;
//				}
//				
//				stargateAddress.addSymbol(symbol);
//			}
//			
//			if (StargateNetwork.get(world).isStargateInNetwork(stargateAddress)) {
//				notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.exists").getFormattedText());
//				return;
//			}
//			
//			if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
//				TileEntity tileEntity = world.getTileEntity(rayTraceResult.getBlockPos());
//				
//				if (tileEntity instanceof StargateAbstractBaseTile) {
//					StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) tileEntity;
//					
//					gateTile.setGateAddress(symbolType, stargateAddress.toImmutable());
//					notifyCommandListener(sender, this, "commands.sgsetaddress.success", gateTile.getPos().toString(), stargateAddress.toString());
//				}
//				
//				else
//					notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.notstargate").getFormattedText());
//			}
//		}
//		
//		catch (IllegalArgumentException e) {
//			notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.wrongaddressspace").getFormattedText());
//			
//			String types = "";
//			for (SymbolTypeEnum symbolType : SymbolTypeEnum.values())
//				types += symbolType + ", ";
//			
//			notifyCommandListener(sender, this, TextFormatting.RED + types);
//			return;
//		}
	}

}
