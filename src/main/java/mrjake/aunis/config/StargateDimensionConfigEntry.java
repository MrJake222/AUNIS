package mrjake.aunis.config;

public class StargateDimensionConfigEntry {
	
	public int energyToOpen;
	public int keepAlive;
	public String group;

	public StargateDimensionConfigEntry(int energyToOpen, int keepAlive, String group) {
		this.energyToOpen = energyToOpen;
		this.keepAlive = keepAlive;
		this.group = group;
	}

	@Override
	public String toString() {
		return "[open="+energyToOpen+", keepAlive="+keepAlive+", group: '"+group+"']";
	}

	public boolean isGroupEqual(StargateDimensionConfigEntry other) {
		if (this.group == null)
			return false;
		
		if (other.group == null)
			return false;
		
		return group.equals(other.group);
	}
}
