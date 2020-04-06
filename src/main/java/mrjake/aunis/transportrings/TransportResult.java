package mrjake.aunis.transportrings;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

public enum TransportResult {
	OK(null),
	BUSY(new TextComponentTranslation("tile.aunis.transportrings_block.busy")),
	BUSY_TARGET(new TextComponentTranslation("tile.aunis.transportrings_block.busy_target")),
	OBSTRUCTED(new TextComponentTranslation("tile.aunis.transportrings_block.obstructed")),
	OBSTRUCTED_TARGET(new TextComponentTranslation("tile.aunis.transportrings_block.obstructed_target")),
	NO_SUCH_ADDRESS(new TextComponentTranslation("tile.aunis.transportrings_block.non_existing_address"));
	
	@Nullable
	public TextComponentTranslation textComponent;

	private TransportResult(TextComponentTranslation textComponent) {
		this.textComponent = textComponent;
		
	}
	
	public boolean ok() {
		return this == OK;
	}
	
	public void sendMessageIfFailed(EntityPlayer player) {
		if (!ok()) {
			player.sendStatusMessage(textComponent, true);
		}
	}
}