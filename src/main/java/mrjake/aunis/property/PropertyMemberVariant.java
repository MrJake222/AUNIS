package mrjake.aunis.property;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import mrjake.aunis.stargate.EnumMemberVariant;
import net.minecraft.block.properties.PropertyHelper;

public class PropertyMemberVariant extends PropertyHelper<EnumMemberVariant> {

    private final ImmutableSet<EnumMemberVariant> allowedValues = ImmutableSet.<EnumMemberVariant>of(EnumMemberVariant.RING, EnumMemberVariant.CHEVRON);
	
	protected PropertyMemberVariant(String name) {
		super(name, EnumMemberVariant.class);
	}
	
	public static PropertyMemberVariant create(String name) {
		return new PropertyMemberVariant(name);
	}

	@Override
	public Collection<EnumMemberVariant> getAllowedValues() {
		return allowedValues;
	}

	@Override
	public Optional<EnumMemberVariant> parseValue(String value) {
		if (value == null || value.isEmpty())
			return Optional.of(EnumMemberVariant.RING);
		
		return Optional.of(EnumMemberVariant.byName(value));			
	}

	@Override
	public String getName(EnumMemberVariant value) {
		return value.toString();
	}

}
