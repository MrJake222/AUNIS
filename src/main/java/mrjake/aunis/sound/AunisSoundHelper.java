package mrjake.aunis.sound;

import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.SoundPositionedPlayToClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

@EventBusSubscriber
public class AunisSoundHelper {
	
	public static void playPositionedSound(World world, BlockPos pos, SoundPositionedEnum soundEnum, boolean play) {
		AunisPacketHandler.INSTANCE.sendToAllTracking(new SoundPositionedPlayToClient(pos, soundEnum, play), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
	}
	
	public static void playSoundEventClientSide(World world, BlockPos pos, SoundEventEnum soundEventEnum) {		
		world.playSound(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, soundEventEnum.soundEvent, Aunis.soundCategory, soundEventEnum.volume, 1.0f, false);
	}	
	
	public static void playSoundEvent(World world, BlockPos pos, SoundEventEnum soundEventEnum) {		
		world.playSound(null, pos, soundEventEnum.soundEvent, Aunis.soundCategory, soundEventEnum.volume, 1.0f);
	}
	
	public static void playSoundToPlayer(EntityPlayerMP player, SoundEventEnum soundEventEnum, BlockPos pos) {
		player.connection.sendPacket(new SPacketSoundEffect(soundEventEnum.soundEvent, Aunis.soundCategory, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, soundEventEnum.volume, 1.0f));
	}
	
	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for (SoundEventEnum soundEventEnum : SoundEventEnum.values()) {
			event.getRegistry().register(soundEventEnum.soundEvent);
		}
	}
}
