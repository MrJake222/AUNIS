package mrjake.aunis.item.dialer;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import mrjake.aunis.gui.OCMessageGui;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT)
public class ChangeEvent {
	
	private static final KeyBinding MODE_SCROLL = new KeyBinding("config.aunis.universe_dialer.mode_scroll", Keyboard.KEY_LCONTROL, "Aunis");
	private static final KeyBinding ADDRESS_SCROLL = new KeyBinding("config.aunis.universe_dialer.address_scroll", Keyboard.KEY_LSHIFT, "Aunis");

	private static final KeyBinding MODE_UP = new KeyBinding("config.aunis.universe_dialer.mode_up", 0, "Aunis");
	private static final KeyBinding MODE_DOWN = new KeyBinding("config.aunis.universe_dialer.mode_down", 0, "Aunis");
	private static final KeyBinding ADDRESS_UP = new KeyBinding("config.aunis.universe_dialer.address_up", 0, "Aunis");
	private static final KeyBinding ADDRESS_DOWN = new KeyBinding("config.aunis.universe_dialer.address_down", 0, "Aunis");
	private static final KeyBinding ADDRESS_REMOVE = new KeyBinding("config.aunis.universe_dialer.address_remove", Keyboard.KEY_DELETE, "Aunis");
	private static final KeyBinding ABORT = new KeyBinding("config.aunis.universe_dialer.abort", Keyboard.KEY_K, "Aunis");
	private static final KeyBinding OC_PROGRAM = new KeyBinding("config.aunis.universe_dialer.oc_program", Keyboard.KEY_O, "Aunis");
	
	@SubscribeEvent
	public static void onMouseEvent(MouseEvent event) {
		if (event.getDwheel() == 0)
			return;
		
		EnumHand hand = getHand();
		
		if (hand != null) {
			if (MODE_SCROLL.isKeyDown()) {
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerModeChangeToServer(hand, (byte) event.getDwheel()));
				event.setCanceled(true);
			} 
			
			else if (ADDRESS_SCROLL.isKeyDown()) {
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerAddressChangeToServer(hand, (byte) event.getDwheel()));
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onKeyboardEvent(ClientTickEvent event) {
		if (event.phase != Phase.END)
			return;
		
		EnumHand hand = getHand();
		
		if (hand != null) {
			if (MODE_UP.isPressed())
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerModeChangeToServer(hand, (byte) 1));
			
			if (MODE_DOWN.isPressed())
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerModeChangeToServer(hand, (byte) -1));
			
			else if (ADDRESS_UP.isPressed())
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerAddressChangeToServer(hand, (byte) 1));
			
			else if (ADDRESS_DOWN.isPressed())
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerAddressChangeToServer(hand, (byte) -1));
			
			else if (ADDRESS_REMOVE.isPressed())
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerAddressRemoveToServer(hand));
			
			else if (ABORT.isPressed())
				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerAbortToSever(hand));
			
			else if (OC_PROGRAM.isPressed())
				Minecraft.getMinecraft().displayGuiScreen(new OCMessageGui());
//				AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerOCProgramToServer(hand));
		}
	}
	
	@Nullable
	public static EnumHand getHand() {
		EntityPlayer player = Minecraft.getMinecraft().player;
		EnumHand hand = null;
		
		if (player == null)
			return null;
		
		if (player.getHeldItemMainhand().getItem() == AunisItems.UNIVERSE_DIALER)
			hand = EnumHand.MAIN_HAND;
		else if (player.getHeldItemOffhand().getItem() == AunisItems.UNIVERSE_DIALER)
			hand = EnumHand.OFF_HAND;
		
		return hand;
	}
	
	public static void registerKeybindings() {
		ClientRegistry.registerKeyBinding(MODE_SCROLL);
		ClientRegistry.registerKeyBinding(MODE_UP);
		ClientRegistry.registerKeyBinding(MODE_DOWN);
		ClientRegistry.registerKeyBinding(ADDRESS_SCROLL);
		ClientRegistry.registerKeyBinding(ADDRESS_UP);
		ClientRegistry.registerKeyBinding(ADDRESS_DOWN);
		ClientRegistry.registerKeyBinding(ADDRESS_REMOVE);
		ClientRegistry.registerKeyBinding(ABORT);
	}
}
