package mrjake.aunis.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import mrjake.aunis.Aunis;
import mrjake.aunis.gui.OCMessageGui;
import mrjake.aunis.gui.PageRenameGui;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.dialer.UniverseDialerActionEnum;
import mrjake.aunis.item.dialer.UniverseDialerActionPacketToServer;
import mrjake.aunis.item.notebook.NotebookActionEnum;
import mrjake.aunis.item.notebook.NotebookActionPacketToServer;
import mrjake.aunis.item.notebook.NotebookItem;
import mrjake.aunis.item.notebook.PageNotebookItem;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
	private static final KeyBinding ADDRESS_REMOVE	= new KeyBinding("config.aunis.address_remove", Keyboard.KEY_DELETE, "Aunis");
	
	// Universe dialer bindings
	private static final KeyBinding DIALER_ABORT		= new KeyBinding("config.aunis.universe_dialer.abort", Keyboard.KEY_K, "Aunis");
	private static final KeyBinding DIALER_OC_PROGRAM	= new KeyBinding("config.aunis.universe_dialer.oc_program", Keyboard.KEY_O, "Aunis");
	
	// Notebook page bindings
	private static final KeyBinding PAGE_RENAME = new KeyBinding("config.aunis.page_notebook.rename", Keyboard.KEY_R, "Aunis");
	
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
			ADDRESS_REMOVE,
			
			// Universe dialer bindings
			DIALER_ABORT,
			DIALER_OC_PROGRAM,
			
			// Notebook page bindings
			PAGE_RENAME
	};
	
	// Init function, call from preInit
	public static void registerKeybindings() {
		ClientRegistry.registerKeyBinding(MODE_SCROLL);
		ClientRegistry.registerKeyBinding(ADDRESS_SCROLL);
		
		ClientRegistry.registerKeyBinding(MODE_UP);
		ClientRegistry.registerKeyBinding(MODE_DOWN);
		ClientRegistry.registerKeyBinding(ADDRESS_UP);
		ClientRegistry.registerKeyBinding(ADDRESS_DOWN);
		ClientRegistry.registerKeyBinding(ADDRESS_REMOVE);
		
		ClientRegistry.registerKeyBinding(DIALER_ABORT);
		ClientRegistry.registerKeyBinding(DIALER_OC_PROGRAM);
		
		ClientRegistry.registerKeyBinding(PAGE_RENAME);
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
	
	// Check for item in both hands
	public static boolean checkForItem(Item item) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		if (player == null)
			return false;
		
		if (player.getHeldItemMainhand().getItem() == item)
			return true;
		else if (player.getHeldItemOffhand().getItem() == item)
			return true;
		
		return false;
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
			
			else if (ADDRESS_REMOVE.isPressed())
				action = UniverseDialerActionEnum.ADDRESS_REMOVE;
			
			else if (DIALER_ABORT.isPressed())
				action = UniverseDialerActionEnum.ABORT;
			
			else if (DIALER_OC_PROGRAM.isPressed())
				Minecraft.getMinecraft().displayGuiScreen(new OCMessageGui());
			
			
			// ---------------------------------------------
			if (action != null) {
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerActionPacketToServer(action, hand, next));
			}
		}
		
		else if (checkForItem(AunisItems.NOTEBOOK_ITEM)) {
			EnumHand hand = getHand(AunisItems.NOTEBOOK_ITEM);
			ItemStack stack = Minecraft.getMinecraft().player.getHeldItem(hand);
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
			
			else if (ADDRESS_REMOVE.isPressed())
				action = NotebookActionEnum.ADDRESS_REMOVE;
			
			else if (PAGE_RENAME.isPressed()) {
				String name = "";
				
				if (stack.hasTagCompound()) {
					NBTTagCompound pageTag = NotebookItem.getSelectedPageFromCompound(stack.getTagCompound());
					name = PageNotebookItem.getNameFromCompound(pageTag);
				}
				
				Minecraft.getMinecraft().displayGuiScreen(new PageRenameGui(name, hand, true));
			}
			
			// ---------------------------------------------
			if (action != null) {
				AunisPacketHandler.INSTANCE.sendToServer(new NotebookActionPacketToServer(action, hand, next));
			}
		}
		
		else if (checkForItem(AunisItems.PAGE_NOTEBOOK_ITEM)) {
			EnumHand hand = getHand(AunisItems.PAGE_NOTEBOOK_ITEM);
			ItemStack stack = Minecraft.getMinecraft().player.getHeldItem(hand);
			
			if (PAGE_RENAME.isPressed() && stack.getMetadata() == 1) {
				String name = "";
				
				if (stack.hasTagCompound()) {
					name = PageNotebookItem.getNameFromCompound(stack.getTagCompound());
				}
				
				Minecraft.getMinecraft().displayGuiScreen(new PageRenameGui(name, hand, false));
			}
		}
		
		for (KeyBinding keyBinding : KEY_BINDINGS) {
			// Skip modifiers
			if (keyBinding == MODE_SCROLL || keyBinding == ADDRESS_SCROLL)
				continue;
			
			METHOD_UNPRESS.invoke(keyBinding);
		}
	}
}
