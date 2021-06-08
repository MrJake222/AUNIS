package mrjake.aunis.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRenderer {
	
	/**
	 * This method renders block using provided {@link IBlockState}.
	 * Call {@code translate(x, y, z)} before this.
	 * 
	 * @param world The world in which rendering takes place.
	 * @param relativePos Relative position of the rendered block.
	 * @param state {@link IBlockState}of the block to render.
	 * @param lightPos Position from which the light level will be taken.
	 */
	public static void render(World world, BlockPos relativePos, IBlockState state, BlockPos lightPos) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(relativePos.getX()-lightPos.getX(), relativePos.getY()-lightPos.getY(), relativePos.getZ()-lightPos.getZ());
		
		GlStateManager.disableLighting();
		
		// Render block
		BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        blockRendererDispatcher.getBlockModelRenderer().renderModelFlat(world, blockRendererDispatcher.getModelForState(state), state, lightPos, tessellator.getBuffer(), false, 0);
		tessellator.draw();
		
		GlStateManager.popMatrix();
		
		GlStateManager.enableLighting();
	}
}
