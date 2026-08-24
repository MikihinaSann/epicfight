# Second pass — catch remaining NeoForge imports
$basePath = "src\main\java"
$files = Get-ChildItem -Path $basePath -Recurse -Filter "*.java"

$replacements = @(
    # NeoForge config
    @{ Old = 'import net.neoforged.neoforge.common.NeoForgeConfig;'; New = '' }
    @{ Old = 'NeoForgeConfig.IO'; New = 'null /* NeoForgeConfig removed */' }

    # ILevelReaderExtension — vanilla has LevelReader
    @{ Old = 'import net.neoforged.neoforge.common.extensions.ILevelReaderExtension;'; New = '' }
    @{ Old = 'ILevelReaderExtension'; New = 'LevelReader' }

    # Data generation
    @{ Old = 'import net.neoforged.neoforge.common.data.BlockTagsProvider;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.data.ExistingFileHelper;'; New = '' }

    # Loot
    @{ Old = 'import net.neoforged.neoforge.event.LootTableLoadEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.loot.IGlobalLootModifier;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.common.loot.LootModifier;'; New = '' }
    @{ Old = 'IGlobalLootModifier'; New = 'Object /* IGlobalLootModifier removed */' }

    # Capability provider
    @{ Old = 'import net.neoforged.neoforge.capabilities.ICapabilityProvider;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.attachment.IAttachmentHolder;'; New = '' }
    @{ Old = 'IAttachmentHolder'; New = 'Object /* IAttachmentHolder removed */' }

    # Entity attribute events
    @{ Old = 'import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;'; New = '' }

    # Payload context
    @{ Old = 'import net.neoforged.neoforge.network.handling.IPayloadContext;'; New = 'import net.fabricmc.fabric.api.networking.v1.PayloadContextRegistry;' }
    @{ Old = 'IPayloadContext'; New = 'net.fabricmc.fabric.api.networking.v1.PayloadContextRegistry' }

    # Extension point
    @{ Old = 'import net.neoforged.fml.IExtensionPoint;'; New = '' }
    @{ Old = 'IExtensionPoint'; New = 'Object /* IExtensionPoint removed */' }

    # ModConfigSpec remaining
    @{ Old = 'import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;'; New = 'import fuzs.forgeconfigapiport.api.config.v3.ModConfigSpec.ConfigValue;' }
    @{ Old = 'import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;'; New = 'import fuzs.forgeconfigapiport.api.config.v3.ModConfigSpec.EnumValue;' }

    # Registry callbacks
    @{ Old = 'import net.neoforged.neoforge.registries.callback.BakeCallback;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.callback.ClearCallback;'; New = '' }
    @{ Old = 'import net.neoforged.neoforge.registries.callback.AddCallback;'; New = '' }
    @{ Old = 'BakeCallback'; New = 'Object /* BakeCallback removed */' }
    @{ Old = 'ClearCallback'; New = 'Object /* ClearCallback removed */' }
    @{ Old = 'AddCallback'; New = 'Object /* AddCallback removed */' }

    # BaseCapability
    @{ Old = 'import net.neoforged.neoforge.capabilities.BaseCapability;'; New = '' }
    @{ Old = 'BaseCapability'; New = 'Object /* BaseCapability removed */' }

    # KeyConflictContext
    @{ Old = 'import net.neoforged.neoforge.client.settings.KeyConflictContext;'; New = '' }
    @{ Old = 'KeyConflictContext.GUI'; New = 'net.minecraft.client.KeyMapping.CATEGORY' }
    @{ Old = 'KeyConflictContext.IN_GAME'; New = 'net.minecraft.client.KeyMapping.CATEGORY' }
    @{ Old = 'KeyConflictContext.UNIVERSAL'; New = 'net.minecraft.client.KeyMapping.CATEGORY' }

    # InputEvent
    @{ Old = 'import net.neoforged.neoforge.client.event.InputEvent;'; New = '' }
    @{ Old = 'InputEvent'; New = 'Object /* InputEvent removed */' }

    # EntityJoinLevelEvent
    @{ Old = 'import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;'; New = '' }
    @{ Old = 'EntityJoinLevelEvent'; New = 'Object /* EntityJoinLevelEvent removed */' }

    # LivingEvent
    @{ Old = 'import net.neoforged.neoforge.event.entity.living.LivingEvent;'; New = '' }
    @{ Old = 'LivingEvent.LivingJumpEvent'; New = 'Object /* LivingJumpEvent removed */' }

    # LivingEquipmentChangeEvent
    @{ Old = 'import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;'; New = '' }

    # PlayerInteractEvent
    @{ Old = 'import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;'; New = '' }
    @{ Old = 'PlayerInteractEvent'; New = 'Object /* PlayerInteractEvent removed */' }

    # ItemTooltipEvent
    @{ Old = 'import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;'; New = '' }
    @{ Old = 'ItemTooltipEvent'; New = 'Object /* ItemTooltipEvent removed */' }

    # LevelTickEvent
    @{ Old = 'import net.neoforged.neoforge.event.tick.LevelTickEvent;'; New = '' }
    @{ Old = 'LevelTickEvent'; New = 'Object /* LevelTickEvent removed */' }

    # GatherDataEvent
    @{ Old = 'import net.neoforged.neoforge.data.event.GatherDataEvent;'; New = '' }
    @{ Old = 'GatherDataEvent'; New = 'Object /* GatherDataEvent removed */' }

    # RegisterShadersEvent
    @{ Old = 'import net.neoforged.neoforge.client.event.RegisterShadersEvent;'; New = '' }

    # ClientHooks
    @{ Old = 'net.neoforged.neoforge.client.ClientHooks'; New = 'null /* ClientHooks removed */' }

    # EventHooks
    @{ Old = 'net.neoforged.neoforge.event.EventHooks'; New = 'null /* EventHooks removed */' }

    # PartEntity
    @{ Old = 'import net.neoforged.neoforge.entity.PartEntity;'; New = '' }
    @{ Old = 'PartEntity'; New = 'net.minecraft.world.entity.Entity /* PartEntity removed */' }

    # ModelData
    @{ Old = 'import net.neoforged.neoforge.client.model.data.ModelData;'; New = '' }
    @{ Old = 'ModelData'; New = 'Object /* ModelData removed */' }

    # FMLLoader.isProduction
    @{ Old = 'FMLLoader.isProduction()'; New = 'FabricLoader.getInstance().isDevelopmentEnvironment()' }

    # ModContainer
    @{ Old = 'import net.neoforged.fml.ModContainer;'; New = '' }
    @{ Old = 'ModContainer'; New = 'Object /* ModContainer removed */' }

    # ModLoader.postEvent
    @{ Old = 'ModLoader.postEvent('; New = 'null /* ModLoader.postEvent removed */(' }

    # IEventBus references
    @{ Old = 'IEventBus'; New = 'Object /* IEventBus removed */' }

    # NeoForge.EVENT_BUS
    @{ Old = 'NeoForge.EVENT_BUS'; New = 'null /* NeoForge.EVENT_BUS removed */' }
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

Write-Host "Processed $count files in pass 2"
