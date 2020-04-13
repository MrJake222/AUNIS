package mrjake.aunis;

import java.util.Arrays;

import mrjake.aunis.block.BeamerBlock;
import mrjake.aunis.block.CapacitorBlock;
import mrjake.aunis.block.InvisibleBlock;
import mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import mrjake.aunis.block.stargate.StargateOrlinMemberBlock;
import mrjake.aunis.property.PropertyBeamerMode;
import mrjake.aunis.property.PropertyMemberVariant;
import mrjake.aunis.property.PropertyUnlistedCamoBlockState;
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
	
	/**
	 * Defines {@link StargateMilkyWayMemberBlock}'s variant.
	 */
	public static final PropertyMemberVariant MEMBER_VARIANT = PropertyMemberVariant.create("member_variant");
	
	
	/**
	 * Contains camouflage blockstate. Used later in rendering.
	 */
	public static final PropertyUnlistedCamoBlockState CAMO_BLOCKSTATE = new PropertyUnlistedCamoBlockState();

	/**
	 * Used to generate collision boxes in {@link StargateOrlinMemberBlock}.
	 */
	public static final PropertyDirection ORLIN_VARIANT = PropertyDirection.create("orlin_variant");
	
	/**
	 * {@link InvisibleBlock} uses it to determine it's collisions boxes.
	 */
	public static final PropertyBool HAS_COLLISIONS = PropertyBool.create("collisions");
	
	/**
	 * Used by {@link CapacitorBlock} to display energy level.
	 */
	public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 10);

	/**
	 * Used by {@link BeamerBlock} to display textures.
	 */
	public static final PropertyBeamerMode BEAMER_MODE = PropertyBeamerMode.create("mode");
}
