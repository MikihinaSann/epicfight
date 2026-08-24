# Bulk NeoForge → Fabric replacement script
# Replaces NeoForge imports and API calls with Fabric equivalents across all Java files

$basePath = "src\main\java"
$files = Get-ChildItem -Path $basePath -Recurse -Filter "*.java"

$replacements = @(
    # DeferredRegister / DeferredHolder
    @{ Old = 'import net.neoforged.neoforge.registries.DeferredHolder;'; New = 'import yesman.epicfight.registry.deferred_shim.DeferredHolderShim;' }
    @{ Old = 'import net.neoforged.neoforge.registries.DeferredRegister;'; New = 'import yesman.epicfight.registry.deferred_shim.DeferredRegisterShim;' }
    @{ Old = 'DeferredRegister.create('; New = 'new DeferredRegisterShim<>(' }
    @{ Old = 'DeferredHolder<'; New = 'DeferredHolderShim<' }
    @{ Old = 'DeferredRegister<'; New = 'DeferredRegisterShim<' }
    @{ Old = '.register(modEventBus)'; New = '.accept()' }

    # Mod ID references
    @{ Old = 'EpicFightMod.MODID'; New = 'EpicFight.MODID' }
    @{ Old = 'EpicFightMod.EPICSKINS_MODID'; New = 'EpicFight.EPICSKINS_MODID' }
    @{ Old = 'EpicFightMod.LOGGER'; New = 'EpicFight.LOGGER' }

    # NeoForge Registries
    @{ Old = 'import net.neoforged.neoforge.registries.NeoForgeRegistries;'; New = '' }
    @{ Old = 'NeoForgeRegistries.ATTACHMENT_TYPES'; New = 'null /* ATTACHMENT_TYPES removed in Fabric */' }

    # RegistryBuilder → direct registry
    @{ Old = 'import net.neoforged.neoforge.registries.RegistryBuilder;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.NewRegistryEvent;'; New = '' }

    # EventBus / SubscribeEvent
    @{ Old = 'import net.neoforged.bus.api.SubscribeEvent;'; New = '' }
    @{ Old = 'import net.neoforged.fml.common.EventBusSubscriber;'; New = '' }
    @{ Old = '@EventBusSubscriber(modid = EpicFight.MODID)'; New = '' }
    @{ Old = '@EventBusSubscriber(modid = EpicFightMod.MODID)'; New = '' }
    @{ Old = '@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)'; New = '' }
    @{ Old = '@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.DEDICATED_SERVER)'; New = '' }
    @{ Old = '@EventBusSubscriber(modid = EpicFight.MODID, value = Dist.CLIENT)'; New = '' }
    @{ Old = '@EventBusSubscriber(modid = EpicFight.MODID, value = Dist.DEDICATED_SERVER)'; New = '' }
    @{ Old = '@SubscribeEvent'; New = '' }

    # Dist / OnlyIn
    @{ Old = 'import net.neoforged.api.distmarker.Dist;'; New = 'import net.fabricmc.api.EnvType;' }
    @{ Old = 'import net.neoforged.api.distmarker.OnlyIn;'; New = '' }
    @{ Old = 'Dist.CLIENT'; New = 'EnvType.CLIENT' }
    @{ Old = 'Dist.DEDICATED_SERVER'; New = 'EnvType.SERVER' }
    @{ Old = '@OnlyIn(Dist.CLIENT)'; New = '' }
    @{ Old = '@OnlyIn(Dist.DEDICATED_SERVER)'; New = '' }

    # FMLEnvironment / FMLLoader
    @{ Old = 'import net.neoforged.fml.loading.FMLEnvironment;'; New = 'import net.fabricmc.loader.api.FabricLoader;' }
    @{ Old = 'import net.neoforged.fml.loading.FMLLoader;'; New = 'import net.fabricmc.loader.api.FabricLoader;' }
    @{ Old = 'FMLEnvironment.dist == Dist.CLIENT'; New = 'FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT' }
    @{ Old = 'FMLEnvironment.production'; New = 'FabricLoader.getInstance().isDevelopmentEnvironment()' }
    @{ Old = 'FMLLoader.isProduction()'; New = 'FabricLoader.getInstance().isDevelopmentEnvironment()' }

    # ModList → FabricLoader
    @{ Old = 'import net.neoforged.fml.ModList;'; New = 'import net.fabricmc.loader.api.FabricLoader;' }
    @{ Old = 'ModList.get().isLoaded('; New = 'FabricLoader.getInstance().isModLoaded(' }
    @{ Old = 'ModList.get().getModFileById('; New = 'FabricLoader.getInstance().getModContainer(' }

    # LoadingModList → FabricLoader
    @{ Old = 'import net.neoforged.fml.loading.LoadingModList;'; New = 'import net.fabricmc.loader.api.FabricLoader;' }
    @{ Old = 'LoadingModList.get().getModFileById('; New = 'FabricLoader.getInstance().getModContainer(' }

    # IEventBus
    @{ Old = 'import net.neoforged.bus.api.IEventBus;'; New = '' }
    @{ Old = 'import net.neoforged.bus.api.EventPriority;'; New = '' }

    # NeoForge EVENT_BUS
    @{ Old = 'import net.neoforged.neoforge.common.NeoForge;'; New = '' }
    @{ Old = 'NeoForge.EVENT_BUS'; New = 'null /* NeoForge.EVENT_BUS removed */' }

    # PacketDistributor
    @{ Old = 'import net.neoforged.neoforge.network.PacketDistributor;'; New = '' }
    @{ Old = 'PacketDistributor.sendToServer('; New = 'ClientPlayNetworking.send(' }
    @{ Old = 'PacketDistributor.sendToAllPlayers('; New = 'EpicFightNetworkManager.sendToAll(' }
    @{ Old = 'PacketDistributor.sendToPlayer('; New = 'EpicFightNetworkManager.sendToPlayer(' }
    @{ Old = 'PacketDistributor.sendToPlayersTrackingEntity('; New = 'EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(' }
    @{ Old = 'PacketDistributor.sendToPlayersTrackingEntityAndSelf('; New = 'EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(' }
    @{ Old = 'PacketDistributor.sendToPlayersTrackingChunk('; New = 'EpicFightNetworkManager.sendToAllPlayerTrackingThisChunkWithSelf(' }

    # PayloadRegistrar / networking
    @{ Old = 'import net.neoforged.neoforge.network.registration.PayloadRegistrar;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;'; New = '' }

    # Capabilities
    @{ Old = 'import net.neoforged.neoforge.capabilities.ItemCapability;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.attachment.AttachmentType;'; New = '' }

    # Config
    @{ Old = 'import net.neoforged.neoforge.common.ModConfigSpec;'; New = 'import fuzs.forgeconfigapiport.api.config.v3.ModConfigSpec;' }
    @{ Old = 'import net.neoforged.neoforge.common.ModConfigSpec.*;'; New = 'import fuzs.forgeconfigapiport.api.config.v3.ModConfigSpec.*;' }
    @{ Old = 'import net.neoforged.fml.config.ModConfig;'; New = 'import fuzs.forgeconfigapiport.api.config.v3.ModConfig;' }
    @{ Old = 'import net.neoforged.fml.event.config.ModConfigEvent;'; New = '' }

    # Mod / ModContainer
    @{ Old = 'import net.neoforged.fml.common.Mod;'; New = '' }
    @{ Old = 'import net.neoforged.fml.ModContainer;'; New = '' }
    @{ Old = 'import net.neoforged.fml.ModLoader;'; New = '' }

    # FML Lifecycle events
    @{ Old = 'import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;'; New = '' }
    @{ Old = 'import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;'; New = '' }
    @{ Old = 'import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;'; New = '' }

    # NeoForge events
    @{ Old = 'import net.neoforged.neoforge.event.AddPackFindersEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.AddReloadListenerEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.RegisterCommandsEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.DataPackRegistryEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.gui.IConfigScreenFactory;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.client.event.RegisterShadersEvent;'; New = '' }

    # Enum extension
    @{ Old = 'import net.neoforged.fml.common.asm.enumextension.EnumProxy;'; New = '' }

    # PartEntity
    @{ Old = 'import net.neoforged.neoforge.entity.PartEntity;'; New = '' }

    # ClientHooks
    @{ Old = 'import net.neoforged.neoforge.client.ClientHooks;'; New = '' }

    # KeyConflictContext
    @{ Old = 'import net.neoforged.client.settings.KeyConflictContext;'; New = '' }
    @{ Old = 'KeyConflictContext.GUI'; New = 'KeyConflictContext.UNIVERSAL' }
    @{ Old = 'KeyConflictContext.IN_GAME'; New = 'KeyConflictContext.IN_GAME' }

    # ModLoadingException
    @{ Old = 'import net.neoforged.fml.ModLoadingException;'; New = '' }

    # ExistingFileHelper
    @{ Old = 'import net.neoforged.neoforge.data.event.GatherDataEvent;'; New = '' }
    @{ Old = 'import net.neoforged.data.loot.ExistingFileHelper;'; New = '' }

    # ModelData
    @{ Old = 'import net.neoforged.neoforge.client.model.data.ModelData;'; New = '' }

    # EventHooks
    @{ Old = 'import net.neoforged.neoforge.event.EventHooks;'; New = '' }
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

Write-Host "Processed $count files"
