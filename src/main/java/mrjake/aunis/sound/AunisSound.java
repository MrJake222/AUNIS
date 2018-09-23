package mrjake.aunis.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class AunisSound {
	protected ResourceLocation resourceLocation;
	protected SoundCategory soundCategory;
	protected boolean repeat;
	
	public AunisSound(ResourceLocation resourceLocation, SoundCategory soundCategory, boolean repeat) {
		this.resourceLocation = resourceLocation;
		this.soundCategory = soundCategory;
		this.repeat = repeat;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (repeat ? 1231 : 1237);
		result = prime * result + ((resourceLocation == null) ? 0 : resourceLocation.hashCode());
		result = prime * result + ((soundCategory == null) ? 0 : soundCategory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AunisSound other = (AunisSound) obj;
		if (repeat != other.repeat)
			return false;
		if (resourceLocation == null) {
			if (other.resourceLocation != null)
				return false;
		} else if (!resourceLocation.equals(other.resourceLocation))
			return false;
		if (soundCategory != other.soundCategory)
			return false;
		return true;
	}
}
