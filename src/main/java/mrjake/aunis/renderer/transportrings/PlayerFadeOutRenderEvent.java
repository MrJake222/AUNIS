package mrjake.aunis.renderer.transportrings;

import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class PlayerFadeOutRenderEvent {

	private static World world;

	private static long tickStart;
	private static boolean fadeOut;
	
	public static double calcFog(World world, long tickStart, double partialTicks) {
		double effTick = world.getTotalWorldTime() - tickStart + partialTicks;
				
		return -(effTick * (effTick-TransportRingsTile.FADE_OUT_TOTAL_TIME)) / (20*20);
	}
	
	@SubscribeEvent
	public static void onDrawGui(RenderGameOverlayEvent.Post event) {		
		if (fadeOut) {
			float fog = (float) calcFog(world, tickStart, event.getPartialTicks());
			
			if (fog < 0) {
				fadeOut = false;
			}
			
			else {
				int alpha = (int) (fog * 255);
				
				alpha /= 3;
				alpha <<= 24;							
				
				ScaledResolution res = event.getResolution();
				Gui.drawRect(0, 0, res.getScaledWidth(), res.getScaledHeight(), 0xFFFFFF | alpha);
			}
		}
	}
	
	// Fog version of fading screen above
	// Not working with shaders
	/* @SubscribeEvent
	public static void onOverlayRender(FogDensity event) {		
		if (fadeOut) {
			float fog = (float) calcFog(world, tickStart, event.getRenderPartialTicks());
			
			if (fog < 0) {
				fadeOut = false;
				GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
			}
			
			else {
				event.setDensity(fog * 1.5f);
				GlStateManager.setFog(GlStateManager.FogMode.EXP);
				
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onFogColor(FogColors event) {
		if (fadeOut) {
			event.setRed(1.0f);
			event.setGreen(1.0f);
			event.setBlue(1.0f);
		}
	} */

	public static void startFadeOut() {
		world = Minecraft.getMinecraft().world;
		tickStart = world.getTotalWorldTime();
		
		fadeOut = true;
	}
}
