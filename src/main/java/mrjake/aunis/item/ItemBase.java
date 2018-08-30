package mrjake.aunis.item;

import mrjake.aunis.Aunis;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemBase extends Item {
	protected String name;
	
	public ItemBase(String registryName, CreativeTabs creativeTab) {
		this.name = registryName;
		
		setRegistryName(name);
		setUnlocalizedName(Aunis.ModID + "." + name);
		
		setCreativeTab(creativeTab);
	}
	
	public void registerItemRenderer() {
		Aunis.proxy.registerItemRenderer(this, 0, getRegistryName());
	}
}
