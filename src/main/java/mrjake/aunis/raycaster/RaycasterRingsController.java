package mrjake.aunis.raycaster;

import java.util.Arrays;
import java.util.List;

import mrjake.aunis.AunisProps;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.transportrings.TRControllerActivatedToServer;
import mrjake.aunis.raycaster.util.Ray;
import mrjake.aunis.renderer.transportrings.TRControllerRenderer;
import mrjake.vector.Vector3f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RaycasterRingsController extends Raycaster {
	public static final RaycasterRingsController INSTANCE = new RaycasterRingsController();
	
	private static final List<Vector3f> vertices = Arrays.asList(
			new Vector3f(0.314399f, -0.96172f, 0.201255f),
			new Vector3f(0.3144f, -0.961707f, 0.283443f),
			new Vector3f(0.315226f, -0.961707f, 0.382972f),
			new Vector3f(0.315226f, -0.961707f, 0.468873f),

			new Vector3f(0.431277f, -0.96172f, 0.200429f),
			new Vector3f(0.430864f, -0.96172f, 0.282202f),
			new Vector3f(0.432101f, -0.961707f, 0.386276f),
			new Vector3f(0.431275f, -0.961707f, 0.46846f),

			new Vector3f(0.566167f, -0.96172f, 0.199477f),
			new Vector3f(0.564515f, -0.96172f, 0.28662f),
			new Vector3f(0.566581f, -0.961707f, 0.387802f),
			new Vector3f(0.565342f, -0.961707f, 0.468747f),

			new Vector3f(0.682219f, -0.96172f, 0.201129f),
			new Vector3f(0.682219f, -0.96172f, 0.285381f),
			new Vector3f(0.683045f, -0.96172f, 0.387804f),
			new Vector3f(0.684695f, -0.961707f, 0.468747f)
	);
	
	@Override
	protected List<Vector3f> getVertices() {
		return vertices;
	}
	
	@Override
	protected int getRayGroupCount() {
		return 4;
	}
	
	public void onActivated(World world, BlockPos pos, EntityPlayer player) {
		EnumFacing facing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
		float rotation = TRControllerRenderer.getRotation(facing);
		
		super.onActivated(world, pos, player, rotation, EnumHand.MAIN_HAND);
	}
	
	@Override
	protected Vector3f getTranslation(World world, BlockPos pos) {
		EnumFacing facing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
		
		return TRControllerRenderer.getTranslation(facing);
	}
	
	@Override
	protected void check(World world, BlockPos pos, EntityPlayer player, int x, int i) {
		if (x == 2)
			return; // Middle part
		
		int num = (2-i)*2 + 1;
		num += (x == 1 ? 1 : 0);
				
		player.swingArm(EnumHand.MAIN_HAND);
		AunisPacketHandler.INSTANCE.sendToServer(new TRControllerActivatedToServer(pos, num));
	}
	
	@Override
	protected boolean brbCheck(List<Ray> brbRayList, Vec3d lookVec, EntityPlayer player, BlockPos pos, EnumHand hand) { return false; }
}
