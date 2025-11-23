package yesman.epicfight.api.client.event;

import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.event.instances.BuildCameraTransform;
import yesman.epicfight.api.client.event.instances.ItemUsedInDecoupledCamera;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.Event;

public final class EpicFightClientEvents {

    /// We will eventually put all epic fight neoforge events here to decouple the event handling system originally
    /// conducted by mod-loaders (Forge, Neoforge, Fabric) in later Minecraft ports
    /// There are a bunch of event definitions under {@link yesman.epicfight.api.neoevent} package. We
    /// plan to define each class as static fields of {@link yesman.epicfight.api.event.Event} in future API model.
    /// For now, we only have event hooks for {@link EpicFightCameraAPI} to demonstrate our API behavior.

    // Camera Events
    public static final class Camera {
        public static final CancelableEvent<BuildCameraTransform.Pre> BUILD_TRANSFORM_PRE = CancelableEvent.createCancelableEvent();
        public static final Event<BuildCameraTransform.Post> BUILD_TRANSFORM_POST = Event.createEvent();
        public static final Event<ItemUsedInDecoupledCamera> ITEM_USED_WHEN_DECOUPLED = Event.createEvent();
    }

    private EpicFightClientEvents() {}
}