package mrjake.aunis.fluid;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class AunisFluids {

	public static MoltenFluid moltenSiliconBlack = new MoltenFluid("silicon_molten_black");
	public static MoltenFluid moltenSiliconRed = new MoltenFluid("silicon_molten_red");
	public static MoltenFluid moltenSiliconBlue = new MoltenFluid("silicon_molten_blue");
	public static MoltenFluid moltenSiliconEnder = new MoltenFluid("silicon_molten_ender");
	public static MoltenFluid moltenSiliconYellow = new MoltenFluid("silicon_molten_yellow");
	public static MoltenFluid moltenSiliconWhite = new MoltenFluid("silicon_molten_white");
	
	public static MoltenFluid moltenNaquadahRaw = new MoltenFluid("naquadah_molten_raw");
	public static MoltenFluid moltenNaquadahRefined = new MoltenFluid("naquadah_molten_refined");
	public static MoltenFluid moltenNaquadahAlloy = new MoltenFluid("naquadah_molten_alloy");
	
	private static Fluid[] fluids = {
			moltenSiliconBlack,
			moltenSiliconRed,
			moltenSiliconBlue,
			moltenSiliconEnder,
			moltenSiliconYellow,
			moltenSiliconWhite,
			
			moltenNaquadahRaw,
			moltenNaquadahRefined,
			moltenNaquadahAlloy
	};
	
	public static Map<String, AunisBlockFluid> blockFluidMap = new HashMap<>();
	
	public static void registerFluids() {
		for (Fluid fluid : fluids) {
			FluidRegistry.registerFluid(fluid);
			FluidRegistry.addBucketForFluid(fluid);
			
			AunisBlockFluid blockFluid = new AunisBlockFluid(fluid, fluid.getName());
			ForgeRegistries.BLOCKS.register(blockFluid);
			blockFluidMap.put(fluid.getName(), blockFluid);
		}
	}
}
