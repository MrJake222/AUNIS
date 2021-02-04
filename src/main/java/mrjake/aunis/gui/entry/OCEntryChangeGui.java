package mrjake.aunis.gui.entry;

import mrjake.aunis.gui.BetterButton;
import mrjake.aunis.gui.OCAddMessageGui;
import mrjake.aunis.item.dialer.UniverseDialerMode;
import mrjake.aunis.item.dialer.UniverseDialerOCMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.client.config.GuiUtils;

public class OCEntryChangeGui extends AbstractEntryChangeGui implements OCUpdatable {

	protected GuiScreen parentScreen;
	protected GuiButton backButton;
	protected GuiButton addButton;
	
	public OCEntryChangeGui(EnumHand hand, NBTTagCompound compound, GuiScreen parentScreen) {
		super(hand, compound);
		
		this.parentScreen =parentScreen;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		backButton = new BetterButton(100, 0, 0, 20, 20, "<")
				.setFgColor(GuiUtils.getColorCode('c', true))
				.setActionCallback(() -> Minecraft.getMinecraft().displayGuiScreen(parentScreen));
		
		addButton = new BetterButton(100, 0, 0, 20, 20, "+")
				.setFgColor(GuiUtils.getColorCode('a', true))
				.setActionCallback(() -> Minecraft.getMinecraft().displayGuiScreen(new OCAddMessageGui(hand, this)));
		
		buttonList.add(backButton);
		buttonList.add(addButton);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		backButton.x = dispx-AbstractEntryChangeGui.PADDING+2;
		backButton.y = dispy+guiHeight+3;
		
		addButton.x = dispx+guiWidth+3;
		addButton.y = dispy+guiHeight+3;
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void generateEntries() {
		NBTTagList list = mainCompound.getTagList(UniverseDialerMode.OC.tagListName, NBT.TAG_COMPOUND);

		for (int i=0; i<list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			
			UniverseDialerOCMessage message = new UniverseDialerOCMessage(compound);
			
			OCEntry entry = new OCEntry(mc, i, list.tagCount(), hand, message, (action, index) -> performAction(action, index));
			entries.add(entry);
		}
	}

	@Override
	protected void generateSections() {
		sections.add(new Section(100, "item.aunis.gui.name"));
		sections.add(new Section(OCEntry.ADDRESS_WIDTH, "item.aunis.gui.oc_address"));
		sections.add(new Section(OCEntry.PORT_WIDTH, "item.aunis.gui.oc_port"));
		sections.add(new Section(OCEntry.PARAM_WIDTH, "item.aunis.gui.oc_params"));
		sections.add(new Section(UniverseEntry.BUTTON_COUNT*25 - 5, ""));
	}
	
	@Override
	public void entryAdded(UniverseDialerOCMessage message) {
		entries.add(new OCEntry(mc, entries.size(), entries.size()+1, hand, message, (action, index) -> performAction(action, index)));
		calculateGuiHeight();
	}
	
	@Override
	protected int getEntryBottomMargin() {
		return 1;
	}
}
