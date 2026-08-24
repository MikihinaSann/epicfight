package yesman.epicfight.registry.entries;

import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;

/// On Fabric, entity attachments are handled via a mixin-injected field on Entity,
/// not via a registry. This class is kept for API compatibility but is mostly empty.
public final class EpicFightAttachmentTypes {
	private EpicFightAttachmentTypes() {}

	/// Creates the entity patch provider for an entity.
	/// On NeoForge this was an AttachmentType; on Fabric it's a simple factory.
	public static AttachmentEntityPatchProvider createEntityPatchProvider() {
		return new AttachmentEntityPatchProvider();
	}
}
