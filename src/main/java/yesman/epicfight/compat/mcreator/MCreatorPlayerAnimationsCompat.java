package yesman.epicfight.compat.mcreator;


import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.render.ValidatePlayerModelEvent;
import yesman.epicfight.compat.ICompatModule;

public class MCreatorPlayerAnimationsCompat implements ICompatModule {
    @Override
    public void onModEventBus(Object eventBus) {}

    @Override
    public void onGameEventBus(Object eventBus) {}

    @Override
    public void onModEventBusClient(Object eventBus) {}

    @Override
    public void onGameEventBusClient(Object eventBus) {
        EpicFightClientEventHooks.Render.VALIDATE_PLAYER_MODEL_TO_RENDER.registerEvent(this::renderEvent);
    }

    private void renderEvent(ValidatePlayerModelEvent event) {
        String animation = event.getPlayerPatch().getOriginal().getPersistentData().getString("PlayerCurrentAnimation");
        if (!animation.isEmpty()) event.setShouldRender(false);
    }
}
