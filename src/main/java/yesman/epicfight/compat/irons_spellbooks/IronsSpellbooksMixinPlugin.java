package yesman.epicfight.compat.irons_spellbooks;

import org.jetbrains.annotations.NotNull;
import yesman.epicfight.compat.MinecraftMod;
import yesman.epicfight.compat.ModMixinPlugin;

public final class IronsSpellbooksMixinPlugin extends ModMixinPlugin {
    @Override
    public @NotNull String getModId() {
        return MinecraftMod.IRONS_SPELLBOOKS.getModId();
    }
}
