package mrjake.aunis.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRenderer {
	
	public static void render(World world, BlockPos pos, IBlockState state) {
		BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBakedModel model = blockRendererDispatcher.getModelForState(state);

        GlStateManager.disableLighting();
		
		Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        blockRendererDispatcher.getBlockModelRenderer().renderModelFlat(world, model, state, pos, tessellator.getBuffer(), false, 0);
		tessellator.draw();
		
        GlStateManager.enableLighting();
	}
}
