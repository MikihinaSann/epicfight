package yesman.epicfight.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.compat.ICompatModule;

// For more details about this compatibility, refer to:
// https://github.com/EchoEllet/shoulder-surfing-iron-spells-integration#technical-implementation
public final class IronsSpellbooksCompat implements ICompatModule {
    @Override
    public void onModEventBus(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBus(IEventBus eventBus) {

    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {
        eventBus.addListener(this::onClientTick);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        if (shouldAimAtTarget()) {
            lookAtCrosshairTarget();
        }
    }

    private static void lookAtCrosshairTarget() {
        EpicFightCameraAPI.getInstance().alignPlayerLookToCrosshair(true);
    }

    private static void lookAtCrosshairTargetIfEpicFightTps() {
        if (EpicFightCameraAPI.getInstance().isTPSMode()) {
            lookAtCrosshairTarget();
        }
    }

    private static boolean isContinuousSpell(CastType castType) {
        return castType == CastType.CONTINUOUS;
    }

    private static boolean isCastingContinuousSpell() {
        return ClientMagicData.isCasting() && isContinuousSpell(ClientMagicData.getCastType());
    }

    public static void onCastSpellUsingSpellBook() {
        lookAtCrosshairTargetIfEpicFightTps();
    }

    public static void onUseScrollItem() {
        lookAtCrosshairTargetIfEpicFightTps();
    }

    public static boolean shouldAimAtTarget() {
        return isCastingContinuousSpell();
    }
}
