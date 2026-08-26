package yesman.epicfight.compat.mcreator;


import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.render.ValidatePlayerModelEvent;
import yesman.epicfight.compat.ICompatModule;

public class MCreatorPlayerAnimationsCompat implements ICompatModule {
    @Override
	public void onInitialize() {}

    @Override
	public void onInitializeServer() {}

    @Override
	public void onInitializeClient() {}

    @Override
	public void onInitializeClientServer() {
        EpicFightClientEventHooks.Render.VALIDATE_PLAYER_MODEL_TO_RENDER.registerEvent(this::renderEvent);
    }

    private void renderEvent(ValidatePlayerModelEvent event) {
        // NeoForge used entity.getPersistentData().getString("PlayerCurrentAnimation") to
        // detect MCreator's custom player animations and suppress Epic Fight's model rendering.
        // Fabric has no direct equivalent of NeoForge's getPersistentData() — a free-form CompoundTag
        // attached to every entity that mods can read/write arbitrary keys to. Vanilla Minecraft's
        // Entity only exposes NBT data through save/load methods, not a live mutable CompoundTag.
        // Fabric API data attachments require a pre-registered AttachmentType, which MCreator's
        // Fabric port would need to expose. Without MCreator's Fabric port providing an API for this,
        // this compat cannot read the "PlayerCurrentAnimation" value.
        // As a result, Epic Fight's model rendering is not suppressed when MCreator animations are active.
        String animation = "";
        if (!animation.isEmpty()) event.setShouldRender(false);
    }
}
