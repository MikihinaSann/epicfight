# Fix double-replaced patterns from the bulk replacement scripts
$basePath = "src\main\java"
$files = Get-ChildItem -Path $basePath -Recurse -Filter "*.java"

$replacements = @(
    # Double-replaced null patterns
    @{ Old = 'null /* null /* NeoForge.EVENT_BUS removed */ removed */'; New = 'null' }

    # PartEntity — replace with just Entity (vanilla)
    # The pattern "net.minecraft.world.entity.Entity /* PartEntity removed */<?> net.minecraft.world.entity.Entity /* PartEntity removed */"
    # should become just a check for Entity
    @{ Old = 'net.minecraft.world.entity.Entity /* PartEntity removed */<?> net.minecraft.world.entity.Entity /* PartEntity removed */'; New = 'net.minecraft.world.entity.Entity' }
    @{ Old = 'net.minecraft.world.entity.Entity /* PartEntity removed */'; New = 'net.minecraft.world.entity.Entity' }

    # Double ModContainer
    @{ Old = 'Object /* ModContainer removed */ Object /* ModContainer removed */'; New = 'Object' }
    @{ Old = 'Object /* ModContainer removed */ById'; New = 'ById' }

    # Fix ModList.get().getModContainerById pattern
    @{ Old = 'ModList.get().getById'; New = 'FabricLoader.getInstance().getModContainer' }
    @{ Old = 'ModList.get()'; New = 'FabricLoader.getInstance()' }

    # Fix remaining Object /* ... removed */ patterns that create invalid Java
    # These need to be handled case by case, but let's clean up the most common ones

    # IEventBus removed — replace with Object
    @{ Old = 'Object /* IEventBus removed */'; New = 'Object' }

    # InputEvent removed
    @{ Old = 'Object /* InputEvent removed */'; New = 'Object' }

    # RenderLivingEvent removed
    @{ Old = 'Object /* RenderLivingEvent removed */'; New = 'Object' }

    # RenderNameTagEvent removed
    @{ Old = 'Object /* RenderNameTagEvent removed */'; New = 'Object' }

    # ItemTooltipEvent removed
    @{ Old = 'Object /* ItemTooltipEvent removed */'; New = 'Object' }

    # MovementInputUpdateEvent removed
    @{ Old = 'Object /* MovementInputUpdateEvent removed */'; New = 'Object' }
    @{ Old = 'MappedObject /* MovementInputUpdateEvent removed */'; New = 'MappedMovementInputEvent' }

    # IModBusEvent removed
    @{ Old = 'Object /* IModBusEvent removed */'; New = 'Object' }

    # LivingJumpEvent removed
    @{ Old = 'Object /* LivingJumpEvent removed */'; New = 'Object' }

    # ViewportEvent removed
    @{ Old = 'Object /* ViewportEvent removed */'; New = 'Object' }

    # IQuadTransformer removed
    @{ Old = 'Object /* IQuadTransformer removed */'; New = 'Object' }

    # IClientItemExtensions removed
    @{ Old = 'Object /* IClientItemExtensions removed */'; New = 'Object' }

    # IGlobalLootModifier removed
    @{ Old = 'Object /* IGlobalLootModifier removed */'; New = 'Object' }

    # IAttachmentHolder removed
    @{ Old = 'Object /* IAttachmentHolder removed */'; New = 'Object' }

    # BaseCapability removed
    @{ Old = 'Object /* BaseCapability removed */'; New = 'Object' }

    # IExtensionPoint removed
    @{ Old = 'Object /* IExtensionPoint removed */'; New = 'Object' }

    # ICapabilityProvider removed
    @{ Old = 'Object /* ICapabilityProvider removed */'; New = 'Object' }

    # FMLModContainer removed
    @{ Old = 'Object /* FMLModContainer removed */'; New = 'Object' }

    # ModContainer removed
    @{ Old = 'Object /* ModContainer removed */'; New = 'Object' }

    # ModLoadingException removed
    @{ Old = 'RuntimeException /* ModLoadingException removed */'; New = 'RuntimeException' }

    # EnumProxy removed
    @{ Old = 'Object /* EnumProxy removed */'; New = 'Object' }

    # All the event types that were replaced with Object
    @{ Old = 'Object /* FMLCommonSetupEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* FMLClientSetupEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* FMLConstructModEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* AddPackFindersEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* AddReloadListenerEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* BuildCreativeModeTabContentsEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* RegisterCommandsEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* DataPackRegistryEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* RegisterClientReloadListenersEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* IConfigScreenFactory removed */'; New = 'Object' }
    @{ Old = 'Object /* RegisterKeyMappingsEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* RegisterCapabilitiesEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* RegisterShadersEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* RegisterPayloadHandlersEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* PayloadRegistrar removed */'; New = 'Object' }
    @{ Old = 'Object /* DirectionalPayloadHandler removed */'; New = 'Object' }
    @{ Old = 'Object /* EntityJoinLevelEvent removed */'; New = 'Object' }
    @{ Old = 'Object /* BakeCallback removed */'; New = 'Object' }
    @{ Old = 'Object /* ClearCallback removed */'; New = 'Object' }
    @{ Old = 'Object /* AddCallback removed */'; New = 'Object' }
    @{ Old = 'Object /* Tags.EntityTypes removed */'; New = 'null' }
    @{ Old = 'Object /* NeoForgeConfig removed */'; New = 'null' }
    @{ Old = 'Object /* ModelData removed */'; New = 'null' }
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

Write-Host "Fixed $count files"
