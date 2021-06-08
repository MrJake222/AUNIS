package mrjake.aunis.gui.entry;

import mrjake.aunis.Aunis;
import mrjake.aunis.gui.BetterButton;
import mrjake.aunis.item.dialer.UniverseDialerActionEnum;
import mrjake.aunis.item.dialer.UniverseDialerActionPacketToServer;
import mrjake.aunis.item.dialer.UniverseDialerMode;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * Class handles universal screen shown when editing Notebook or Universe Dialer
 * saved addresses.
 * 
 * @author MrJake222
 */
public class UniverseEntryChangeGui extends AbstractEntryChangeGui {

	public UniverseEntryChangeGui(EnumHand hand, NBTTagCompound compound) {
		super(hand, compound);
	}
	
	protected GuiButton ocButton;
	protected GuiButton abortButton;
	
	@Override
	public void initGui() {
		super.initGui();
		
		if (Aunis.ocWrapper.isModLoaded()) {
			ocButton = new BetterButton(100, 0, 0, 20, 20, ">")
					.setFgColor(GuiUtils.getColorCode('a', true))
					.setActionCallback(() -> Minecraft.getMinecraft().displayGuiScreen(new OCEntryChangeGui(hand, mainCompound, this)));
						
			buttonList.add(ocButton);
		}
		
		abortButton = new BetterButton(100, 0, 0, 50, 20, "Abort")
				.setFgColor(GuiUtils.getColorCode('c', true))
				.setActionCallback(() -> AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerActionPacketToServer(UniverseDialerActionEnum.ABORT, hand, false)));
		
		buttonList.add(abortButton);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		ocButton.x = dispx+guiWidth+3;
		ocButton.y = dispy+guiHeight+3;

		abortButton.visible = mainCompound.hasKey("linkedGate");
		abortButton.x = dispx-AbstractEntryChangeGui.PADDING+2;
		abortButton.y = dispy+guiHeight+3;
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void generateEntries() {
		NBTTagList list = mainCompound.getTagList(UniverseDialerMode.MEMORY.tagListName, NBT.TAG_COMPOUND);

		for (int i=0; i<list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			
			StargateAddress stargateAddress = new StargateAddress(compound);
			int maxSymbols = SymbolUniverseEnum.getMaxSymbolsDisplay(compound.getBoolean("hasUpgrade"));
			String name = "";
			
			if (compound.hasKey("name")) {
				name = compound.getString("name");
			}
			
			UniverseEntry entry = new UniverseEntry(mc, i, list.tagCount(), hand, name, (action, index) -> performAction(action, index), SymbolTypeEnum.UNIVERSE, stargateAddress, maxSymbols);
			entries.add(entry);
		}
	}

	@Override
	protected void generateSections() {
		sections.add(new Section(UniverseEntry.ADDRESS_WIDTH, "item.aunis.gui.address"));
		sections.add(new Section(100, "item.aunis.gui.name"));
		sections.add(new Section(UniverseEntry.BUTTON_COUNT*25 - 5, ""));
	}
	
	@Override
	protected int getEntryBottomMargin() {
		return 2;
	}
}
