package mrjake.aunis.fluid;

import mrjake.aunis.Aunis;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class AunisBlockFluid extends BlockFluidClassic {
	
	public AunisBlockFluid(Fluid fluid, String name) {
		super(fluid, Material.LAVA);
		
		setRegistryName(new ResourceLocation(Aunis.ModID, name));
		setUnlocalizedName(getRegistryName().toString());
	}	
}
