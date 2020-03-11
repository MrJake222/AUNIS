package mrjake.aunis.command;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
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
		
		if (args.length != 7) {
			notifyCommandListener(sender, this, "commands.sgsetaddress.wrongaddress");
			return;
		}
		
		List<EnumSymbol> newAddress = new ArrayList<EnumSymbol>(7);
		
		for (int i=0; i<7; i++) {
			EnumSymbol symbol = EnumSymbol.forEnglishName(args[i]);
			
			if (symbol == null) {
				notifyCommandListener(sender, this, "commands.sgsetaddress.wrongsymbol", i+1);
				return;
			}
			
			newAddress.add(symbol);
		}
		
		if (StargateNetwork.get(world).checkForStargate(newAddress)) {
			notifyCommandListener(sender, this, "commands.sgsetaddress.exists");
			return;
		}
		
		if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
			TileEntity tileEntity = world.getTileEntity(rayTraceResult.getBlockPos());
			
			if (tileEntity instanceof StargateAbstractBaseTile) {
				StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) tileEntity;
				
				gateTile.setGateAddress(newAddress);
			}
			
			else
				notifyCommandListener(sender, this, "commands.sgsetaddress.notstargate");
		}
	}

}
