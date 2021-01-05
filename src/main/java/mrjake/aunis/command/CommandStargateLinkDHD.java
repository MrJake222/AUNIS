package mrjake.aunis.command;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.util.LinkingHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public final class CommandStargateLinkDHD extends CommandBase {
    @Override
    public final String getName() {
        return "sglinkdhd";
    }

    @Override
    public final String getUsage(ICommandSender sender) {
        return "/sglinkdhd [radius] [vertical radius]";
    }

    @Override
    public final void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        final int radius = args.length > 0 ? parseInt(args[0]) : AunisConfig.dhdConfig.rangeFlat;
        final int verticalRadius = args.length > 1 ? parseInt(args[1]) : AunisConfig.dhdConfig.rangeVertical;

        final BlockPos radiusPos = new BlockPos(radius, verticalRadius, radius);

        final BlockPos gatePos = LinkingHelper.findClosestUnlinked(sender.getEntityWorld(), sender.getPosition(), radiusPos, AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK);
        final BlockPos dhdPos = LinkingHelper.findClosestUnlinked(sender.getEntityWorld(), sender.getPosition(), radiusPos, AunisBlocks.DHD_BLOCK);

        if(gatePos != null && dhdPos != null) {
            LinkingHelper.updateLinkedGate(sender.getEntityWorld(), gatePos, dhdPos);
        } else {
            notifyCommandListener(sender, this, TextFormatting.RED + "Unable to link to nearest gate: no gates found in radius %s from pos %s", radiusPos.toString(), sender.getPosition().toString());
        }
    }
}
