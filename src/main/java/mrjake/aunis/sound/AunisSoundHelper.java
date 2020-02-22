package mrjake.aunis.sound;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.SoundPositionedPlayToClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

@EventBusSubscriber
public class AunisSoundHelper {
	
	public static void playPositionedSound(World world, BlockPos pos, AunisPositionedSoundEnum soundEnum, boolean play) {
		AunisPacketHandler.INSTANCE.sendToAllTracking(new SoundPositionedPlayToClient(pos, soundEnum, play), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	private static Map<EnumAunisSoundEvent, SoundEvent> aunisSoundEvents = new HashMap<>();
	
	private static SoundEvent createSoundEvent(String name) {
		ResourceLocation resourceLocation = new ResourceLocation(Aunis.ModID, name);
		return new SoundEvent(resourceLocation).setRegistryName(resourceLocation);
	}
	
	static {
		aunisSoundEvents.put(EnumAunisSoundEvent.DHD_PRESS, createSoundEvent("dhd_press"));
		aunisSoundEvents.put(EnumAunisSoundEvent.DHD_PRESS_BRB, createSoundEvent("dhd_brb"));
		
		aunisSoundEvents.put(EnumAunisSoundEvent.GATE_OPEN, createSoundEvent("gate_open"));
		aunisSoundEvents.put(EnumAunisSoundEvent.GATE_CLOSE, createSoundEvent("gate_close"));
		aunisSoundEvents.put(EnumAunisSoundEvent.GATE_DIAL_FAILED, createSoundEvent("gate_dial_fail"));
		aunisSoundEvents.put(EnumAunisSoundEvent.GATE_DIAL_FAILED_COMPUTER, createSoundEvent("gate_dial_fail_computer"));
		
		aunisSoundEvents.put(EnumAunisSoundEvent.CHEVRON_LOCK_DHD, createSoundEvent("chevron_lock_dhd"));	
		aunisSoundEvents.put(EnumAunisSoundEvent.CHEVRON_INCOMING, createSoundEvent("chevron_incoming"));	
		aunisSoundEvents.put(EnumAunisSoundEvent.WORMHOLE_GO, createSoundEvent("wormhole_go"));
		
		aunisSoundEvents.put(EnumAunisSoundEvent.WORMHOLE_FLICKER, createSoundEvent("wormhole_flicker"));
		aunisSoundEvents.put(EnumAunisSoundEvent.RINGS_TRANSPORT, createSoundEvent("rings_transport"));
		aunisSoundEvents.put(EnumAunisSoundEvent.RINGS_CONTROLLER_BUTTON, createSoundEvent("rings_controller_button"));
		
		aunisSoundEvents.put(EnumAunisSoundEvent.CHEVRON_SHUT, createSoundEvent("chevron_shut"));
		aunisSoundEvents.put(EnumAunisSoundEvent.CHEVRON_OPEN, createSoundEvent("chevron_open"));
		aunisSoundEvents.put(EnumAunisSoundEvent.CHEVRON_LOCKING, createSoundEvent("chevron_locking"));
		
		aunisSoundEvents.put(EnumAunisSoundEvent.GATE_ORLIN_DIAL, createSoundEvent("gate_orlin_dial"));
	}
	
	public static void playSoundEventClientSide(World world, BlockPos pos, EnumAunisSoundEvent soundEvent, float volume) {		
		world.playSound(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, aunisSoundEvents.get(soundEvent), SoundCategory.AMBIENT, volume, 1.0f, false);
	}	
	
	public static void playSoundEvent(World world, BlockPos pos, EnumAunisSoundEvent soundEnum, float volume) {		
		world.playSound(null, pos, aunisSoundEvents.get(soundEnum), SoundCategory.AMBIENT, volume, 1.0f);
	}
	
	public static void playSoundToPlayer(EntityPlayerMP player, EnumAunisSoundEvent soundEvent, BlockPos pos, float volume) {
		player.connection.sendPacket(new SPacketSoundEffect(aunisSoundEvents.get(soundEvent), SoundCategory.AMBIENT, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, volume, 1.0f));
	}
	
	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for (SoundEvent soundEvent : aunisSoundEvents.values()) {
			event.getRegistry().register(soundEvent);
		}
	}
}
