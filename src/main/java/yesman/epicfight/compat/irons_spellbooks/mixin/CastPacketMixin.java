package yesman.epicfight.compat.irons_spellbooks.mixin;

import io.redspace.ironsspellbooks.network.casting.CastPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.compat.irons_spellbooks.IronsSpellbooksCompat;

@Mixin(CastPacket.class)
public class CastPacketMixin {
    /// Assumes that [CastPacket] is instantiated when the player presses the key to cast a spell using a spellbook.
    /// This behavior is accurate at the time of writing, and while future updates to Iron Spells may change it, such changes are unlikely.
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onCastSpellUsingSpellBook(CallbackInfo ci) {
        IronsSpellbooksCompat.onCastSpellUsingSpellBook();
    }
}