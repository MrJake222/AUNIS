package mrjake.aunis;

import java.util.Arrays;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;

/**
 * This class holds static references to every {@link IProperty} created by The AUNIS Mod
 *
 */
public class AunisProps {
	/**
	 * Holds horizontal facing of the block
	 * 
	 * Used for ex. by Stargate blocks since only vertical Stargates are supported at the moment
	 */
	public static final PropertyDirection FACING_HORIZONTAL = PropertyDirection.create("facing", Arrays.asList(EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST)); 
	
	/**
	 * Holds rotation(something like extended facing)
	 * Calculated as ([value] * -22.5) and passed to OpenGL rotate function 
	 * 
	 * Used mainly by DHD, for now...
	 */
	public static final PropertyInteger ROTATION_HORIZONTAL = PropertyInteger.create("rotation", 0, 15);
	
	/**
	 * Indicates if the block should be a static render(normal block) or a TESR rendered one
	 * 	True: normal block model
	 * 	False: TESR
	 * 
	 * Used by Gate's blocks
	 * Indicates Stargate's merge state
	 */
	public static final PropertyBool RENDER_BLOCK = PropertyBool.create("render_block");
}
