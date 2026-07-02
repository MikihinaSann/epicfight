package yesman.epicfight.compat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yesman.epicfight.client.renderer.RenderPipelineHooks;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

/**
 * Compatibility with Argon4W's AcceleratedRendering. Epic Fight draws
 * already-posed (CPU- or compute-shader-skinned) vertices, so AcceleratedRendering's
 * entity pipeline must fall back to the vanilla path around those draws to avoid
 * double-transforming them. Everything else on a patched entity (armor layers,
 * held items rendered through vanilla ModelParts) stays accelerated because the
 * override is scoped to the skinned-mesh draw calls only.
 */
public class ARCompat implements ICompatModule {
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}

	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<FMLClientSetupEvent>addListener(event -> event.enqueueWork(() -> {
			try {
				bindHooks();
			} catch (Throwable t) {
				// AcceleratedRendering is alpha-stage; an AR build without the expected pipeline
				// API must degrade to "compat disabled", not crash mod loading
				EpicFightMod.LOGGER.error("Incompatible AcceleratedRendering version detected, disabling Epic Fight's AR compat", t);
			}
		}));
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
	}

	/** Kept in a separate method so AcceleratedRendering classes only load when it's installed. */
	@OnlyIn(Dist.CLIENT)
	private static void bindHooks() {
		ClientConfig.ARCompatMode mode = ClientConfig.AR_COMPAT_MODE.get();

		if (mode == ClientConfig.ARCompatMode.OFF) {
			return;
		}

		RenderPipelineHooks.setSkinnedMeshDrawHooks(
				com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature::useVanillaPipeline,
				com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature::resetPipeline);

		if (mode == ClientConfig.ARCompatMode.FORCE_VANILLA_PIPELINE) {
			RenderPipelineHooks.setEntityRenderHooks(
					com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature::useVanillaPipeline,
					com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature::resetPipeline);
		}
	}
}
