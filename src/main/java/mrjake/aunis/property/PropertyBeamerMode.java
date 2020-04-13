package mrjake.aunis.property;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import mrjake.aunis.beamer.BeamerModeEnum;
import net.minecraft.block.properties.PropertyHelper;

public class PropertyBeamerMode extends PropertyHelper<BeamerModeEnum> {

    private final ImmutableSet<BeamerModeEnum> allowedValues = ImmutableSet.<BeamerModeEnum>of(BeamerModeEnum.POWER, BeamerModeEnum.FLUID, BeamerModeEnum.ITEMS, BeamerModeEnum.NONE);
	
	protected PropertyBeamerMode(String name) {
		super(name, BeamerModeEnum.class);
	}
	
	public static PropertyBeamerMode create(String name) {
		return new PropertyBeamerMode(name);
	}

	@Override
	public Collection<BeamerModeEnum> getAllowedValues() {
		return allowedValues;
	}

	@Override
	public Optional<BeamerModeEnum> parseValue(String value) {
		if (value == null || value.isEmpty())
			return Optional.of(BeamerModeEnum.POWER);
		
		return Optional.of(BeamerModeEnum.valueOf(value.toUpperCase()));			
	}

	@Override
	public String getName(BeamerModeEnum value) {
		return value.name().toLowerCase();
	}

}
