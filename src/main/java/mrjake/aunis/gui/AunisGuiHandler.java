package mrjake.aunis.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class AunisGuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (GuiIdEnum.valueOf(ID)) {
			case DHD_GUI:
				return new DHDContainer(player.inventory, world, x, y ,z);
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (GuiIdEnum.valueOf(ID)) {
			case DHD_GUI:
				return new DHDContainerGui(new DHDContainer(player.inventory, world, x, y ,z));
		}
		
		return null;
	}

}
