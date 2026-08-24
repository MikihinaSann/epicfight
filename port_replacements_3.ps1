# Third pass — remaining NeoForge-specific imports
$basePath = "src\main\java"
$files = Get-ChildItem -Path $basePath -Recurse -Filter "*.java"

$replacements = @(
    # IQuadTransformer — vanilla has QuadTransformer? No, this is NeoForge-specific. Remove.
    @{ Old = 'import net.neoforged.neoforge.client.model.IQuadTransformer;'; New = '' }
    @{ Old = 'IQuadTransformer'; New = 'Object /* IQuadTransformer removed */' }

    # RenderEngine NeoForge client events
    @{ Old = 'import net.neoforged.neoforge.client.event.*;'; New = '' }

    # ClientTickEvent — use Fabric's
    @{ Old = 'import net.neoforged.neoforge.client.event.ClientTickEvent;'; New = '' }
    @{ Old = 'net.neoforged.neoforge.client.event.ClientTickEvent'; New = 'net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents' }

    # MovementInputUpdateEvent
    @{ Old = 'import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;'; New = '' }
    @{ Old = 'MovementInputUpdateEvent'; New = 'Object /* MovementInputUpdateEvent removed */' }

    # LivingJumpEvent leftover
    @{ Old = 'import net.neoforged.neoforge.event.entity.living.Object /* LivingJumpEvent removed */;'; New = '' }

    # Event / IModBusEvent
    @{ Old = 'import net.neoforged.bus.api.Event;'; New = '' }
    @{ Old = 'import net.neoforged.fml.event.IModBusEvent;'; New = '' }
    @{ Old = 'IModBusEvent'; New = 'Object /* IModBusEvent removed */' }

    # ViewportEvent
    @{ Old = 'import net.neoforged.neoforge.client.event.ViewportEvent;'; New = '' }
    @{ Old = 'ViewportEvent'; New = 'Object /* ViewportEvent removed */' }

    # FMLModContainer
    @{ Old = 'import net.neoforged.fml.javafmlmod.FMLObject /* ModContainer removed */;'; New = '' }
    @{ Old = 'FMLObject /* ModContainer removed */'; New = 'Object /* FMLModContainer removed */' }

    # Tags.EntityTypes — NeoForge-specific tag system
    @{ Old = 'import net.neoforged.neoforge.common.Tags.EntityTypes;'; New = '' }
    @{ Old = 'Tags.EntityTypes'; New = 'null /* Tags.EntityTypes removed */' }

    # RenderLivingEvent
    @{ Old = 'import net.neoforged.neoforge.client.event.RenderLivingEvent;'; New = '' }
    @{ Old = 'RenderLivingEvent'; New = 'Object /* RenderLivingEvent removed */' }

    # RenderNameTagEvent
    @{ Old = 'import net.neoforged.neoforge.client.event.RenderNameTagEvent;'; New = '' }
    @{ Old = 'RenderNameTagEvent'; New = 'Object /* RenderNameTagEvent removed */' }

    # IClientItemExtensions
    @{ Old = 'import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;'; New = '' }
    @{ Old = 'IClientItemExtensions'; New = 'Object /* IClientItemExtensions removed */' }

    # FMLCommonSetupEvent / FMLClientSetupEvent / FMLConstructModEvent
    @{ Old = 'FMLCommonSetupEvent'; New = 'Object /* FMLCommonSetupEvent removed */' }
    @{ Old = 'FMLClientSetupEvent'; New = 'Object /* FMLClientSetupEvent removed */' }
    @{ Old = 'FMLConstructModEvent'; New = 'Object /* FMLConstructModEvent removed */' }

    # AddPackFindersEvent
    @{ Old = 'AddPackFindersEvent'; New = 'Object /* AddPackFindersEvent removed */' }

    # AddReloadListenerEvent
    @{ Old = 'AddReloadListenerEvent'; New = 'Object /* AddReloadListenerEvent removed */' }

    # BuildCreativeModeTabContentsEvent
    @{ Old = 'BuildCreativeModeTabContentsEvent'; New = 'Object /* BuildCreativeModeTabContentsEvent removed */' }

    # RegisterCommandsEvent
    @{ Old = 'RegisterCommandsEvent'; New = 'Object /* RegisterCommandsEvent removed */' }

    # DataPackRegistryEvent
    @{ Old = 'DataPackRegistryEvent'; New = 'Object /* DataPackRegistryEvent removed */' }

    # RegisterClientReloadListenersEvent
    @{ Old = 'RegisterClientReloadListenersEvent'; New = 'Object /* RegisterClientReloadListenersEvent removed */' }

    # IConfigScreenFactory
    @{ Old = 'IConfigScreenFactory'; New = 'Object /* IConfigScreenFactory removed */' }

    # RegisterKeyMappingsEvent
    @{ Old = 'RegisterKeyMappingsEvent'; New = 'Object /* RegisterKeyMappingsEvent removed */' }

    # RegisterCapabilitiesEvent
    @{ Old = 'RegisterCapabilitiesEvent'; New = 'Object /* RegisterCapabilitiesEvent removed */' }

    # RegisterShadersEvent
    @{ Old = 'RegisterShadersEvent'; New = 'Object /* RegisterShadersEvent removed */' }

    # RegisterPayloadHandlersEvent
    @{ Old = 'RegisterPayloadHandlersEvent'; New = 'Object /* RegisterPayloadHandlersEvent removed */' }

    # PayloadRegistrar
    @{ Old = 'PayloadRegistrar'; New = 'Object /* PayloadRegistrar removed */' }

    # DirectionalPayloadHandler
    @{ Old = 'DirectionalPayloadHandler'; New = 'Object /* DirectionalPayloadHandler removed */' }

    # EnumProxy
    @{ Old = 'EnumProxy'; New = 'Object /* EnumProxy removed */' }

    # ModLoadingException
    @{ Old = 'ModLoadingException'; New = 'RuntimeException /* ModLoadingException removed */' }

    # Remaining NeoForge references
    @{ Old = 'import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.tick.LevelTickEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.data.event.GatherDataEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.RegisterShadersEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.gui.IConfigScreenFactory;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.AddPackFindersEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.AddReloadListenerEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.RegisterCommandsEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.DataPackRegistryEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.capabilities.ItemCapability;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.attachment.AttachmentType;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.network.registration.PayloadRegistrar;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.network.handling.IPayloadContext;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.network.PacketDistributor;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.ModConfigSpec;'; New = 'import fuzs.forgeconfigapiport.api.config.v3.ModConfigSpec;' }
    @{ Old = 'import net.neoforged.neoforge.common.ModConfigSpec.*;'; New = 'import fuzs.forgeconfigapiport.api.config.v3.ModConfigSpec.*;' }
    @{ Old = 'import net.neoforged.fml.config.ModConfig;'; New = 'import fuzs.forgeconfigapiport.api.config.v3.ModConfig;' }
    @{ Old = 'import net.neoforged.fml.event.config.ModConfigEvent;'; New = '' }
    @{ Old = 'import net.neoforged.fml.common.Mod;'; New = '' }
    @{ Old = 'import net.neoforged.fml.ModContainer;'; New = '' }
    @{ Old = 'import net.neoforged.fml.ModLoader;'; New = '' }
    @{ Old = 'import net.neoforged.fml.ModList;'; New = 'import net.fabricmc.loader.api.FabricLoader;' }
    @{ Old = 'import net.neoforged.fml.loading.FMLEnvironment;'; New = 'import net.fabricmc.loader.api.FabricLoader;' }
    @{ Old = 'import net.neoforged.fml.loading.FMLLoader;'; New = 'import net.fabricmc.loader.api.FabricLoader;' }
    @{ Old = 'import net.neoforged.fml.loading.LoadingModList;'; New = 'import net.fabricmc.loader.api.FabricLoader;' }
    @{ Old = 'import net.neoforged.bus.api.IEventBus;'; New = '' }
    @{ Old = 'import net.neoforged.bus.api.SubscribeEvent;'; New = '' }
    @{ Old = 'import net.neoforged.bus.api.EventPriority;'; New = '' }
    @{ Old = 'import net.neoforged.fml.common.EventBusSubscriber;'; New = '' }
    @{ Old = 'import net.neoforged.api.distmarker.Dist;'; New = 'import net.fabricmc.api.EnvType;' }
    @{ Old = 'import net.neoforged.api.distmarker.OnlyIn;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.NeoForge;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.DeferredHolder;'; New = 'import yesman.epicfight.registry.deferred_shim.DeferredHolderShim;' }
    @{ Old = 'import net.neoforged.neoforge.registries.DeferredRegister;'; New = 'import yesman.epicfight.registry.deferred_shim.DeferredRegisterShim;' }
    @{ Old = 'import net.neoforged.neoforge.registries.NeoForgeRegistries;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.RegistryBuilder;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.NewRegistryEvent;'; New = '' }
    @{ Old = 'import net.neoforged.fml.common.asm.enumextension.EnumProxy;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.entity.PartEntity;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.ClientHooks;'; New = '' }
    @{ Old = 'import net.neoforged.client.settings.KeyConflictContext;'; New = '' }
    @{ Old = 'import net.neoforged.fml.ModLoadingException;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.model.data.ModelData;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.EventHooks;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.NeoForgeConfig;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.extensions.ILevelReaderExtension;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.data.BlockTagsProvider;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.data.ExistingFileHelper;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.loot.IGlobalLootModifier;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.loot.LootModifier;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.capabilities.ICapabilityProvider;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.attachment.IAttachmentHolder;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.callback.BakeCallback;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.callback.ClearCallback;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.callback.AddCallback;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.capabilities.BaseCapability;'; New = '' }
    @{ Old = 'import net.neoforged.fml.IExtensionPoint;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.model.IQuadTransformer;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.RenderLivingEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.RenderNameTagEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.ViewportEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.Tags;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.living.LivingEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.LootTableLoadEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.InputEvent;'; New = '' }
    @{ Old = 'import net.neoforged.fml.javafmlmod.FMLModContainer;'; New = '' }
    @{ Old = 'import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;'; New = '' }
    @{ Old = 'import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;'; New = '' }
    @{ Old = 'import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;'; New = '' }
    @{ Old = 'import net.neoforged.bus.api.Event;'; New = '' }
    @{ Old = 'import net.neoforged.fml.event.IModBusEvent;'; New = '' }
)

$count = 0
foreach ($file in $files) {
    $content = $file | Get-Content -Raw
    $original = $content
    foreach ($rep in $replacements) {
        $content = $content -replace [regex]::Escape($rep.Old), $rep.New
    }
    if ($content -ne $original) {
        $file | Set-Content -Value $content -NoNewline
        $count++
    }
}

Write-Host "Processed $count files in pass 3"
