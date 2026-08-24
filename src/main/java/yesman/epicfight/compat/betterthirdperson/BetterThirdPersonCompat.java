package yesman.epicfight.compat.betterthirdperson;


import yesman.epicfight.client.camera.EpicFightTpsCameraDisableState;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisabledReason;
import yesman.epicfight.compat.ICompatModule;

// Disables the Epic Fight's TPS perspective when this mod is installed,
// otherwise, both mods will make modifications to the vanilla third-person back perspective,
// which results in buggy behavior.
// Note: This does not support the "Better Third Person" mod,
// features like dodge, attack, and lock-on may not work with Epic Fight.
public final class BetterThirdPersonCompat implements ICompatModule {
    @Override
    public void onModEventBus(Object eventBus) {

    }

    @Override
    public void onGameEventBus(Object eventBus) {

    }

    @Override
    public void onModEventBusClient(Object eventBus) {
        EpicFightTpsCameraDisableState.disable(EpicFightTpsCameraDisabledReason.BetterThirdPerson);
    }

    @Override
    public void onGameEventBusClient(Object eventBus) {

    }
}
