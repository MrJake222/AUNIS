package mrjake.aunis.item;

import mrjake.aunis.Aunis;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class ItemBase extends Item {
	protected String name;
	
	public ItemBase(String registryName) {
		this.name = registryName;
		
		setRegistryName(Aunis.ModID + ":" + name);
		setUnlocalizedName(Aunis.ModID + "." + name);
		
		setCreativeTab(Aunis.aunisCreativeTab);
	}
	
	public void registerItemRenderer() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));

	}
}
