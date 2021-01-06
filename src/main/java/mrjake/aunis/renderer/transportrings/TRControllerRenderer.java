package mrjake.aunis.renderer.transportrings;

import mrjake.aunis.AunisProps;
import mrjake.aunis.loader.ElementEnum;
import mrjake.aunis.tesr.RendererInterface;
import mrjake.aunis.tileentity.TRControllerTile;
import mrjake.vector.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TRControllerRenderer implements RendererInterface {

	private EnumFacing facing;
	
	public TRControllerRenderer(TRControllerTile controllerTile) {
		World world = controllerTile.getWorld();
		BlockPos pos = controllerTile.getPos();
		
		IBlockState blockState = world.getBlockState(pos);
		facing = blockState.getValue(AunisProps.FACING_HORIZONTAL);
	}

	private static final Vector3f NORTH_TRANSLATION = new Vector3f(0, 0, 0);
	private static final Vector3f EAST_TRANSLATION = new Vector3f(1, 0, 0);
	private static final Vector3f SOUTH_TRANSLATION = new Vector3f(1, 0, 1);
	private static final Vector3f WEST_TRANSLATION = new Vector3f(0, 0, 1);
	
	public static Vector3f getTranslation(EnumFacing facing) {
		switch (facing) {
			case NORTH:
				return NORTH_TRANSLATION;
				
			case EAST:
				return EAST_TRANSLATION;
				
			case SOUTH:
				return SOUTH_TRANSLATION;
				
			case WEST:
				return WEST_TRANSLATION;
				
			default:
				return null;
		}
	}
	
	public static int getRotation(EnumFacing facing) {
		switch (facing) {
			case NORTH:
				return 0;
				
			case EAST:
				return 270;
				
			case SOUTH:
				return 180;
				
			case WEST:
				return 90;
	
			default:
				return 0;
		}
	}
	
	@Override
	public void render(double x, double y, double z, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		Vector3f tr = getTranslation(facing);
		int rot = getRotation(facing);
		
		GlStateManager.translate(tr.x, tr.y, tr.z);
		GlStateManager.rotate(rot, 0, 1, 0);
		
		ElementEnum.RINGSCONTROLLER_GOAULD.bindTextureAndRender();
		ElementEnum.RINGSCONTROLLER_GOAULD_BUTTONS.bindTextureAndRender();
		
		GlStateManager.popMatrix();
	}
}
