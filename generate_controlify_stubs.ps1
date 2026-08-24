# Generate stub classes for Controlify API
$stubBase = "src\main\java\dev\isxander\controlify"

$classes = @(
    @{ Pkg = "api"; Name = "ControlifyApi" },
    @{ Pkg = "api\bind"; Name = "ControlifyBindApi" },
    @{ Pkg = "api\bind"; Name = "InputBinding" },
    @{ Pkg = "api\bind"; Name = "InputBindingBuilder" },
    @{ Pkg = "api\bind"; Name = "InputBindingSupplier" },
    @{ Pkg = "api\buttonguide"; Name = "ButtonGuideApi" },
    @{ Pkg = "api\buttonguide"; Name = "ButtonGuidePredicate" },
    @{ Pkg = "api\entrypoint"; Name = "ControlifyEntrypoint" },
    @{ Pkg = "api\entrypoint"; Name = "InitContext" },
    @{ Pkg = "api\entrypoint"; Name = "PreInitContext" },
    @{ Pkg = "api\event"; Name = "ControlifyEvents" },
    @{ Pkg = "api\guide"; Name = "ContainerCtx" },
    @{ Pkg = "api\guide"; Name = "Fact" },
    @{ Pkg = "api\guide"; Name = "GuideDomainRegistry" },
    @{ Pkg = "api\guide"; Name = "InGameCtx" },
    @{ Pkg = "bindings"; Name = "BindContext" },
    @{ Pkg = "bindings"; Name = "ControlifyBindings" },
    @{ Pkg = "bindings"; Name = "RadialIcons" },
    @{ Pkg = "controller"; Name = "ControllerEntity" },
    @{ Pkg = "screenop"; Name = "ScreenProcessor" },
    @{ Pkg = "screenop"; Name = "ScreenProcessorProvider" },
    @{ Pkg = "utils\render"; Name = "Blit" },
    @{ Pkg = "utils\render"; Name = "CGuiPose" }
)

foreach ($cls in $classes) {
    $dir = "$stubBase\$($cls.Pkg)"
    $pkg = "dev.isxander.controlify.$($cls.Pkg -replace '\\','.')"
    $file = "$dir\$($cls.Name).java"

    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

    $content = @"
package $pkg;

/// Stub class for Controlify API — generated to allow compilation
/// without the actual Controlify mod (which has a malformed access widener in CurseMaven).
public class $($cls.Name) {
}
"@

    Set-Content -Path $file -Value $content -NoNewline
}

Write-Host "Generated $($classes.Count) Controlify stub classes"
