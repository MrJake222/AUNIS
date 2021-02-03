package mrjake.aunis.gui.address;

import mrjake.aunis.item.dialer.UniverseDialerMode;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Class handles universal screen shown when editing Notebook or Universe Dialer
 * saved addresses.
 * 
 * @author MrJake222
 * TODO Add OpenComputers menu support to UniverseAddressChangeGui
 */
public class UniverseAddressChangeGui extends AbstractAddressChangeGui {

	public UniverseAddressChangeGui(EnumHand hand, ItemStack stack) {
		super(hand, stack);
	}
	
	@Override
	protected void generateAddressEntries() {
		NBTTagList list = mainCompound.getTagList(UniverseDialerMode.MEMORY.tagListName, NBT.TAG_COMPOUND);

		for (int i=0; i<list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			
			StargateAddress stargateAddress = new StargateAddress(compound);
			int maxSymbols = SymbolUniverseEnum.getMaxSymbolsDisplay(compound.getBoolean("hasUpgrade"));
			String name = "";
			
			if (compound.hasKey("name")) {
				name = compound.getString("name");
			}
			
			UniverseAddressEntry entry = new UniverseAddressEntry(mc, i, list.tagCount(), hand, name, SymbolTypeEnum.UNIVERSE, stargateAddress, maxSymbols, (action, index) -> actionPerformed(action, index));
			addressEntries.add(entry);
		}
	}
}
