package mrjake.aunis.item;

import mrjake.aunis.AunisProps;
import mrjake.aunis.block.stargate.StargateMemberBlock;
import mrjake.aunis.stargate.EnumMemberVariant;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public abstract class StargateMemberItemBlock extends ItemBlock {
    public StargateMemberItemBlock(StargateMemberBlock block) {
        super(block);
        setRegistryName(block.getRegistryName());
        setHasSubtypes(true);
    }

    protected abstract String getRingUnlocalizedName();

    protected abstract String getChevronUnlocalizedName();

    @Override
    public final String getUnlocalizedName(ItemStack stack) {
        EnumMemberVariant variant = block.getStateFromMeta(stack.getMetadata()).getValue(AunisProps.MEMBER_VARIANT);
        switch (variant) {
            case CHEVRON:
                return getChevronUnlocalizedName();

            case RING:
                return getRingUnlocalizedName();

            default:
                return stack.getUnlocalizedName();
        }
    }
}
