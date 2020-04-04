package mrjake.aunis.stargate.power;

public class StargateEnergyRequired {
	
	public int energyToOpen;
	public int keepAlive;

	public StargateEnergyRequired(int energyToOpen, int keepAlive) {
		this.energyToOpen = energyToOpen;
		this.keepAlive = keepAlive;
	}
	
	public StargateEnergyRequired(double energyToOpen, double keepAlive) {
		this((int)energyToOpen, (int)keepAlive);
	}

	@Override
	public String toString() {
		return "[open="+energyToOpen+", keepAlive="+keepAlive+"]";
	}

	public StargateEnergyRequired mul(double mul) {
		return new StargateEnergyRequired(energyToOpen*mul, keepAlive*mul);
	}

	public StargateEnergyRequired add(StargateEnergyRequired add) {
		return new StargateEnergyRequired(energyToOpen+add.energyToOpen, keepAlive+add.keepAlive);
	}

	public StargateEnergyRequired cap(int max) {
		return new StargateEnergyRequired(Math.min(energyToOpen, max), keepAlive);
	}
}
