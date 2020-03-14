package mrjake.aunis.command;

import mrjake.aunis.Aunis;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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
			DHDTile dhdTile = (DHDTile) world.getTileEntity(rayTraceResult.getBlockPos());
			IItemHandler itemHandler = dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			
			for (int i=0; i<itemHandler.getSlots(); i++)
				Aunis.info(itemHandler.getStackInSlot(i).toString());
		}
	}
}
