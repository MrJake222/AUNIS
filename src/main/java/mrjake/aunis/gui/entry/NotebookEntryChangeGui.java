package mrjake.aunis.gui.entry;

import mrjake.aunis.item.notebook.PageNotebookItem;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
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
public class NotebookEntryChangeGui extends AbstractEntryChangeGui {

	public NotebookEntryChangeGui(EnumHand hand, NBTTagCompound compound) {
		super(hand, compound);
	}

	@Override
	protected void generateEntries() {
		NBTTagList list = mainCompound.getTagList("addressList", NBT.TAG_COMPOUND);
		
		for (int i=0; i<list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			
			String name = PageNotebookItem.getNameFromCompound(compound);
			SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(compound.getInteger("symbolType"));
			StargateAddress stargateAddress = new StargateAddress(compound.getCompoundTag("address"));
			int maxSymbols = symbolType.getMaxSymbolsDisplay(compound.getBoolean("hasUpgrade"));			
			
			NotebookEntry entry = new NotebookEntry(mc, i, list.tagCount(), hand, name, (action, index) -> performAction(action, index), symbolType, stargateAddress, maxSymbols);
			entries.add(entry);
		}
	}

	@Override
	protected void generateSections() {
		sections.add(new Section(NotebookEntry.ADDRESS_WIDTH, "item.aunis.gui.address"));
		sections.add(new Section(100, "item.aunis.gui.name"));
		sections.add(new Section(NotebookEntry.BUTTON_COUNT*25 - 5, ""));
	}
	
	@Override
	protected int getEntryBottomMargin() {
		return 1; // 5
	}
}
