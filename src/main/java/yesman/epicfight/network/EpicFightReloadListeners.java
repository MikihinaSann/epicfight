package yesman.epicfight.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillReloadListener;
import yesman.epicfight.api.ex_cap.listeners.ConditionalReloadListener;
import yesman.epicfight.api.ex_cap.listeners.ItemPresetReloadListener;
import yesman.epicfight.api.ex_cap.listeners.MovesetReloadListener;
import yesman.epicfight.api.ex_cap.listeners.WeaponModifierReloadListener;
import yesman.epicfight.api.ex_cap.listeners.ExCapDataCreationReloadListener;
import yesman.epicfight.api.ex_cap.listeners.ExCapDataReloadListener;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Registers all EpicFight server-side reload listeners with Fabric's {@link ResourceManagerHelper}.
 *
 * <p>Fabric requires reload listeners to implement {@link IdentifiableResourceReloadListener},
 * which adds a {@code getFabricId()} method. This class wraps the existing
 * {@link SimpleJsonResourceReloadListener} instances in adapter wrappers.</p>
 */
public final class EpicFightReloadListeners {

    private EpicFightReloadListeners() {}

    /**
     * Registers all server-side reload listeners with Fabric's resource manager.
     * Must be called from {@code onInitialize} (common side).
     */
    public static void register() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(net.minecraft.server.packs.PackType.SERVER_DATA);

        // Core datapack reload listeners
        helper.registerReloadListener(wrap("skill_parameters", SkillReloadListener.getInstance()));
        helper.registerReloadListener(wrap("epicfight_mobpatch", new MobPatchReloadListener()));
        helper.registerReloadListener(wrap("capabilities", new ItemCapabilityReloadListener()));
        helper.registerReloadListener(wrap("weapon_types", new WeaponTypeReloadListener()));
        helper.registerReloadListener(wrap("item_keywords", new ItemKeywordReloadListener()));
        helper.registerReloadListener(wrap("animation_manager", AnimationManager.getInstance()));

        // ExCap reload listeners
        helper.registerReloadListener(wrap("weapon_modifiers", new WeaponModifierReloadListener()));
        helper.registerReloadListener(wrap("movesets", new MovesetReloadListener()));
        helper.registerReloadListener(wrap("item_presets", new ItemPresetReloadListener()));
        helper.registerReloadListener(wrap("conditional", new ConditionalReloadListener()));
        helper.registerReloadListener(wrap("excap_data_creation", new ExCapDataCreationReloadListener()));
        helper.registerReloadListener(wrap("excap_data", new ExCapDataReloadListener()));

        EpicFight.LOGGER.info("EpicFight reload listeners registered (12)");
    }

    /**
     * Wraps a {@link PreparableReloadListener} in an {@link IdentifiableResourceReloadListener}
     * with the given fabric ID.
     */
    private static IdentifiableResourceReloadListener wrap(String id, PreparableReloadListener delegate) {
        return new IdentifiableReloadListenerWrapper(ResourceLocation.fromNamespaceAndPath(EpicFight.MODID, id), delegate);
    }

    /**
     * Adapter that wraps any {@link PreparableReloadListener} as an {@link IdentifiableResourceReloadListener}.
     * Catches exceptions during reload to prevent crashes from inter-listener dependency ordering issues.
     */
    private record IdentifiableReloadListenerWrapper(ResourceLocation fabricId, PreparableReloadListener delegate)
            implements IdentifiableResourceReloadListener {

        @Override
        public ResourceLocation getFabricId() {
            return fabricId;
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager,
                                               ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                               Executor backgroundExecutor, Executor gameExecutor) {
            return delegate.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor)
                    .exceptionally(throwable -> {
                        EpicFight.LOGGER.warn("Reload listener {} failed: {}", fabricId, throwable.getMessage());
                        return null;
                    });
        }
    }
}
