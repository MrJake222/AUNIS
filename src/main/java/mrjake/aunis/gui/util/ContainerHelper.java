package mrjake.aunis.gui.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerHelper {
	
	public static List<Slot> generatePlayerSlots(IInventory playerInventory, int yInventory) {		
		List<Slot> slots = new ArrayList<Slot>(39);
		
		slots.addAll(generateSlotRow(playerInventory, 0, 8, 18, yInventory+58)); // 144
				
		for (int row=0; row<3; row++) {
			slots.addAll(generateSlotRow(playerInventory, 9*(row+1), 8, 18, 86 + 18*row)); // 86
		}
		
		return slots;
	}
	
	public static List<Slot> generateSlotRow(IInventory playerInventory, int firstIndex, int xStart, int xOffset, int y) {				
		List<Slot> slots = new ArrayList<Slot>(9);
		
		for (int col=0; col<9; col++) {
			slots.add(new Slot(playerInventory, firstIndex+col, 18*col + 8, y));
		}
		
		return slots;
	}
}
