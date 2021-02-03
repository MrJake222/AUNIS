package mrjake.aunis.gui.address;

import mrjake.aunis.item.notebook.PageNotebookItem;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
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
 * 
 */
public class NotebookAddressChangeGui extends AbstractAddressChangeGui {

	public NotebookAddressChangeGui(EnumHand hand, ItemStack stack) {
		super(hand, stack);
	}

	@Override
	protected void generateAddressEntries() {
		NBTTagList list = mainCompound.getTagList("addressList", NBT.TAG_COMPOUND);
		int y = dispy+20;		
		
		for (int i=0; i<list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			
			String name = PageNotebookItem.getNameFromCompound(compound);
			SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(compound.getInteger("symbolType"));
			StargateAddress stargateAddress = new StargateAddress(compound.getCompoundTag("address"));
			int maxSymbols = symbolType.getMaxSymbolsDisplay(compound.getBoolean("hasUpgrade"));			
			
			NotebookAddressEntry entry = new NotebookAddressEntry(fontRenderer, dispx, y, i, hand, name, symbolType, stargateAddress, maxSymbols, (action, index) -> reloadAddressEntries(action, index));
			addressEntries.add(entry);
			y += entry.getHeight();
		}
	}
}
