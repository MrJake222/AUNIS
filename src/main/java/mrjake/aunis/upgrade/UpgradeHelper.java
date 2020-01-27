package mrjake.aunis.upgrade;

import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.upgrade.EnumUpgradeAction;
import mrjake.aunis.packet.upgrade.UpgradeSlotInteractToClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class UpgradeHelper {
	
	/**
	 * This code is run server-side by onBlockActivated.
	 * 
	 * It 
	 * 
	 * @param upgradeable
	 * @param heldItem
	 */
	public static boolean upgradeInteract(EntityPlayerMP player, ITileEntityUpgradeable upgradeable, ItemStack heldItemStack) {
		if (heldItemStack.getItem() != upgradeable.getAcceptedUpgradeItem() && heldItemStack.getItem() != Items.AIR)
			return false;
		
		BlockPos pos = ((TileEntity) upgradeable).getPos();
		World world = ((TileEntity) upgradeable).getWorld();
		
		TargetPoint target = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		
		if (upgradeable.hasUpgrade()) {
			if (upgradeable.getUpgradeRendererState().doUpgradeRender) {
				// Removing upgrade from slot
				
				upgradeable.getUpgradeRendererState().doUpgradeRender = false;
				upgradeable.setUpgrade(false);
				
				AunisPacketHandler.INSTANCE.sendToAllTracking(new UpgradeSlotInteractToClient(pos, EnumUpgradeAction.POP_UPGRADE), target);
				
				if (heldItemStack.getItem() == upgradeable.getAcceptedUpgradeItem())
					player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(heldItemStack.getItem(), heldItemStack.getCount() + 1));
				else
					player.addItemStackToInventory(new ItemStack(upgradeable.getAcceptedUpgradeItem(), 1));
			}
			
			else {
				// Sliding out upgrade
				
				upgradeable.getUpgradeRendererState().doUpgradeRender = true;
				
				AunisPacketHandler.INSTANCE.sendToAllTracking(new UpgradeSlotInteractToClient(pos, EnumUpgradeAction.REMOVE_UPGRADE), target);
			}
		}
		
		else {
			if (upgradeable.getUpgradeRendererState().doUpgradeRender) {
				// Inserting upgrade
								
				upgradeable.getUpgradeRendererState().doUpgradeRender = false;
				upgradeable.setUpgrade(true);
				
				AunisPacketHandler.INSTANCE.sendToAllTracking(new UpgradeSlotInteractToClient(pos, EnumUpgradeAction.INSERT_UPGRADE), target);
			}
			
			else {
				// Putting upgrade in slot
				
				if (heldItemStack.getItem() == upgradeable.getAcceptedUpgradeItem()) {
					upgradeable.getUpgradeRendererState().doUpgradeRender = true;
					
					AunisPacketHandler.INSTANCE.sendToAllTracking(new UpgradeSlotInteractToClient(pos, EnumUpgradeAction.PUT_UPGRADE), target);
					
					player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(heldItemStack.getItem(), heldItemStack.getCount() - 1) );
				}
				
				else
					return false;
			}
		}
		
		upgradeable.markDirty();
		
		return true;
	}
}
