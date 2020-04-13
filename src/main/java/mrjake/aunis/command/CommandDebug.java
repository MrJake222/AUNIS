package mrjake.aunis.command;

import mrjake.aunis.tileentity.BeamerTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class CommandDebug extends CommandBase {

	@Override
	public String getName() {
		return "sgdebug";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/sgdebug";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {	
		World world = sender.getEntityWorld();
		EntityPlayerMP player = (EntityPlayerMP) sender;
		RayTraceResult rayTraceResult = player.rayTrace(8, 0);
		
		if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
			TileEntity tileEntity = world.getTileEntity(rayTraceResult.getBlockPos());

			if (tileEntity instanceof BeamerTile) {
				BeamerTile beamerTile = (BeamerTile) tileEntity;
				
				notifyCommandListener(sender, this, "linkedGate: " + beamerTile.getLinkedGate());
			}
		}
	}
}
