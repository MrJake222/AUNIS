package mrjake.aunis.command;

import mrjake.aunis.stargate.network.StargateAddressDynamic;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
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
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		EntityPlayerMP player = (EntityPlayerMP) sender;
		RayTraceResult rayTraceResult = player.rayTrace(8, 0);
		
		if (args.length < 1) {
			notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.noaddressspace").getFormattedText());
			return;
		}
		
		try {
			SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(args[0].toUpperCase());
			
			if (args.length != 9) {
				notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.wrongaddress").getFormattedText());
				return;
			}
			
			StargateAddressDynamic stargateAddress = new StargateAddressDynamic(symbolType);
					
			for (int i=0; i<8; i++) {
				SymbolInterface symbol = symbolType.fromEnglishName(args[i+1].replace("-", " "));
				
				if (symbol == null) {
					notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.wrongsymbol").getFormattedText(), i+1);
					return;
				}
				
				stargateAddress.addSymbol(symbol);
			}
			
			if (StargateNetwork.get(world).isStargateInNetwork(stargateAddress)) {
				notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.exists").getFormattedText());
				return;
			}
			
			if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
				TileEntity tileEntity = world.getTileEntity(rayTraceResult.getBlockPos());
				
				if (tileEntity instanceof StargateAbstractBaseTile) {
					StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) tileEntity;
					
					gateTile.setGateAddress(symbolType, stargateAddress.toImmutable());
					notifyCommandListener(sender, this, "commands.sgsetaddress.success", gateTile.getPos().toString(), stargateAddress.toString());
				}
				
				else
					notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.notstargate").getFormattedText());
			}
		}
		
		catch (IllegalArgumentException e) {
			notifyCommandListener(sender, this, TextFormatting.RED + new TextComponentTranslation("commands.sgsetaddress.wrongaddressspace").getFormattedText());
			
			String types = "";
			for (SymbolTypeEnum symbolType : SymbolTypeEnum.values())
				types += symbolType + ", ";
			
			notifyCommandListener(sender, this, TextFormatting.RED + types);
			return;
		}
	}

}
