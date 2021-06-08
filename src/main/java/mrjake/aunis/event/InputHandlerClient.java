package mrjake.aunis.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import mrjake.aunis.gui.PageRenameGui;
import mrjake.aunis.gui.entry.NotebookEntryChangeGui;
import mrjake.aunis.gui.entry.UniverseEntryChangeGui;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.dialer.UniverseDialerActionEnum;
import mrjake.aunis.item.dialer.UniverseDialerActionPacketToServer;
import mrjake.aunis.item.notebook.NotebookActionEnum;
import mrjake.aunis.item.notebook.NotebookActionPacketToServer;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT)
public class InputHandlerClient {
	
	// Common bindings
	private static final KeyBinding MODE_SCROLL		= new KeyBinding("config.aunis.mode_scroll", Keyboard.KEY_LCONTROL, "Aunis");
	private static final KeyBinding ADDRESS_SCROLL 	= new KeyBinding("config.aunis.address_scroll", Keyboard.KEY_LSHIFT, "Aunis");
	
	private static final KeyBinding MODE_UP 		= new KeyBinding("config.aunis.mode_up", 0, "Aunis");
	private static final KeyBinding MODE_DOWN 		= new KeyBinding("config.aunis.mode_down", 0, "Aunis");
	private static final KeyBinding ADDRESS_UP 		= new KeyBinding("config.aunis.address_up", 0, "Aunis");
	private static final KeyBinding ADDRESS_DOWN 	= new KeyBinding("config.aunis.address_down", 0, "Aunis");
	
	// Used to open common gui on Notebook/Universe dialer 
	private static final KeyBinding ADDRESS_EDIT	= new KeyBinding("config.aunis.address_edit", Keyboard.KEY_INSERT, "Aunis");
	
	// Unpress
	private static final Method METHOD_UNPRESS = ObfuscationReflectionHelper.findMethod(KeyBinding.class, "func_74505_d", void.class);
	
	private static final KeyBinding[] KEY_BINDINGS = {
			// Common bindings
			MODE_SCROLL,
			ADDRESS_SCROLL,
			
			MODE_UP,
			MODE_DOWN,
			ADDRESS_UP,
			ADDRESS_DOWN,
			
			ADDRESS_EDIT
	};
	
	// Init function, call from preInit
	public static void registerKeybindings() {
		for (KeyBinding keyb : KEY_BINDINGS) {
			ClientRegistry.registerKeyBinding(keyb);
		}
	}
	
	
	// Get hand holding item
	@Nullable
	public static EnumHand getHand(Item item) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		EnumHand hand = null;
		
		if (player == null)
			return null;
		
		if (player.getHeldItemMainhand().getItem() == item)
			hand = EnumHand.MAIN_HAND;
		else if (player.getHeldItemOffhand().getItem() == item)
			hand = EnumHand.OFF_HAND;
		
		return hand;
	}
	
	@Nullable
	public static ItemStack getItemStack(EntityPlayer player, Item item) {
		EnumHand hand = getHand(item);
		
		if (hand != null) {
			return player.getHeldItem(hand);
		}
		
		return null;
	}
	
	// Check for item in both hands
	public static boolean checkForItem(Item item) {
		return getHand(item) != null;
	}
	
	// ------------------------------------------------------------------------------------
	// Events
	
	@SubscribeEvent
	public static void onMouseEvent(MouseEvent event) {
		if (event.getDwheel() == 0)
			return;
		
		// NBT print
//		if (Minecraft.getMinecraft().player != null)
//			Aunis.info("nbt: " + Minecraft.getMinecraft().player.getHeldItemMainhand().getTagCompound());
		
		boolean next = event.getDwheel() < 0;
				
		if (checkForItem(AunisItems.UNIVERSE_DIALER)) {
			EnumHand hand = getHand(AunisItems.UNIVERSE_DIALER);
			UniverseDialerActionEnum action = null;
			
			if (MODE_SCROLL.isKeyDown())
				action = UniverseDialerActionEnum.MODE_CHANGE;
			
			else if (ADDRESS_SCROLL.isKeyDown())
				action = UniverseDialerActionEnum.ADDRESS_CHANGE;
			
			
			// ---------------------------------------------
			if (action != null) {
				event.setCanceled(true);
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerActionPacketToServer(action, hand, next));
			}
		}
		
		else if (checkForItem(AunisItems.NOTEBOOK_ITEM)) {
			EnumHand hand = getHand(AunisItems.NOTEBOOK_ITEM);
			NotebookActionEnum action = null;
			
			if (ADDRESS_SCROLL.isKeyDown())
				action = NotebookActionEnum.ADDRESS_CHANGE;
			
			
			// ---------------------------------------------
			if (action != null) {
				event.setCanceled(true);
				AunisPacketHandler.INSTANCE.sendToServer(new NotebookActionPacketToServer(action, hand, next));
			}
		}
	}
	
	
	@SubscribeEvent
	public static void onKeyboardEvent(ClientTickEvent event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (event.phase != Phase.END)
			return;
		
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		if (checkForItem(AunisItems.UNIVERSE_DIALER)) {
			EnumHand hand = getHand(AunisItems.UNIVERSE_DIALER);
			UniverseDialerActionEnum action = null;
			boolean next = false;
			
			if (MODE_UP.isPressed()) {
				action = UniverseDialerActionEnum.MODE_CHANGE;
				next = false;
			}	
			
			else if (MODE_DOWN.isPressed()) {
				action = UniverseDialerActionEnum.MODE_CHANGE;
				next = true;
			}
			
			else if (ADDRESS_UP.isPressed()) {
				action = UniverseDialerActionEnum.ADDRESS_CHANGE;
				next = false;
			}
			
			else if (ADDRESS_DOWN.isPressed()) {
				action = UniverseDialerActionEnum.ADDRESS_CHANGE;
				next = true;
			}
			
			// ---------------------------------------------
			if (action != null) {
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerActionPacketToServer(action, hand, next));
			}
		}
		
		else if (checkForItem(AunisItems.NOTEBOOK_ITEM)) {
			EnumHand hand = getHand(AunisItems.NOTEBOOK_ITEM);
			NotebookActionEnum action = null;
			boolean next = false;
			
			if (ADDRESS_UP.isPressed()) {
				action = NotebookActionEnum.ADDRESS_CHANGE;
				next = false;
			}
			
			else if (ADDRESS_DOWN.isPressed()) {
				action = NotebookActionEnum.ADDRESS_CHANGE;
				next = true;
			}
			
			// ---------------------------------------------
			if (action != null) {
				AunisPacketHandler.INSTANCE.sendToServer(new NotebookActionPacketToServer(action, hand, next));
			}
		}
		
		if (ADDRESS_EDIT.isPressed()) {
			tryOpenAddressGui(player);
		}
		
		for (KeyBinding keyBinding : KEY_BINDINGS) {
			// Skip modifiers
			if (keyBinding == MODE_SCROLL || keyBinding == ADDRESS_SCROLL)
				continue;
			
			METHOD_UNPRESS.invoke(keyBinding);
		}
	}
	
	private static void tryOpenAddressGui(EntityPlayer player) {
		EnumHand hand = getHand(AunisItems.PAGE_NOTEBOOK_ITEM);
		if (hand != null) {
			ItemStack stack = player.getHeldItem(hand);
			
			if (stack.getMetadata() == 1) {
				// Full page (not empty)
				Minecraft.getMinecraft().displayGuiScreen(new PageRenameGui(hand, stack));
			}
			
			return;
		}
		
		hand = getHand(AunisItems.NOTEBOOK_ITEM);
		if (hand != null) {
			Minecraft.getMinecraft().displayGuiScreen(new NotebookEntryChangeGui(hand, player.getHeldItem(hand).getTagCompound()));
			return;
		}
		
		hand = getHand(AunisItems.UNIVERSE_DIALER);
		if (hand != null) {
			Minecraft.getMinecraft().displayGuiScreen(new UniverseEntryChangeGui(hand, player.getHeldItem(hand).getTagCompound()));
			return;
		}
	}
}
