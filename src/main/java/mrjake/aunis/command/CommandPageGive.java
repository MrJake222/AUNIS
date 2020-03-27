package mrjake.aunis.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandPageGive extends CommandBase {

	@Override
	public String getName() {
		return "sggivepage";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/sggivepage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {		
		// TODO Give page
//		if (args.length < 6 || args.length > 8) {
//			notifyCommandListener(sender, this, "commands.sgsetaddress.wrongaddress");
//			return;
//		}
//		
//		int len = args.length;
//		boolean hasBiome = false;
//		
//		// Check if the 7th argument is valid symbol,
//		// If no, it's a biome
//		if (args.length == 7) {
//			if (EnumSymbol.forEnglishName(args[6].replace("-", " ")) == null) {
//				len = 6;
//				hasBiome = true;
//			}
//		}
//		
//		else if (args.length == 8) {
//			len = 7;
//			hasBiome = true;
//		}
//		
//		List<EnumSymbol> address = new ArrayList<EnumSymbol>(7);
//		
//		for (int i=0; i<len; i++) {
//			EnumSymbol symbol = EnumSymbol.forEnglishName(args[i].replace("-", " "));
//			
//			if (symbol == null) {
//				notifyCommandListener(sender, this, "commands.sgsetaddress.wrongsymbol", i+1);
//				return;
//			}
//			
//			address.add(symbol);
//		}
//		
//		NBTTagCompound compound = new NBTTagCompound();
//		
//		compound.setLong("address", EnumSymbol.toLong(address));
//		
//		if (len == 7)
//			compound.setInteger("7th", address.get(6).id);
//		
//		int color = 0x303000;
//		
//		if (hasBiome)
//			color = PageNotebookItem.getColorForBiome(args[len]);
//		
//		compound.setInteger("color", color);
//		
//		ItemStack stack = new ItemStack(AunisItems.pageNotebookItem, 1, 1);
//		stack.setTagCompound(compound);
//		
//		if (sender instanceof EntityPlayerMP) {
//			((EntityPlayerMP) sender).addItemStackToInventory(stack);
//		}
	}

}
