package mrjake.aunis.config;

public enum StargateSizeEnum {
	SMALL(0, "Small", 0.75, -0.95),
	MEDIUM(1, "Medium", 0.83, -0.62),
	LARGE(2, "Large", 1, 0);
	
	public int id;
	public String name;
	public double renderScale;
	public double renderTranslationY;

	private StargateSizeEnum(int id, String name, double renderScale, double renderTranslationY) {
		this.id = id;
		this.name = name;
		this.renderScale = renderScale;
		this.renderTranslationY = renderTranslationY;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static StargateSizeEnum fromId(int id) {
		return StargateSizeEnum.values()[id];
	}
}
