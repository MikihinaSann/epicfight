# Generate stub classes for AzureLibArmor (Fabric uses different package names)
$stubBase = "src\main\java\mod\azure\azurelibarmor\common"

$classes = @(
    @{ Pkg = "cache\object"; Name = "GeoCube" },
    @{ Pkg = "cache\object"; Name = "GeoQuad" },
    @{ Pkg = "cache\object"; Name = "GeoVertex" },
    @{ Pkg = "model"; Name = "AzBakedModel" },
    @{ Pkg = "model"; Name = "AzBone" },
    @{ Pkg = "model"; Name = "AzBoneSnapshot" },
    @{ Pkg = "render\armor"; Name = "AzArmorRenderer" },
    @{ Pkg = "render\armor"; Name = "AzArmorRendererRegistry" },
    @{ Pkg = "render\armor\bone"; Name = "AzArmorBoneProvider" },
    @{ Pkg = "util\client"; Name = "RenderUtils" }
)

foreach ($cls in $classes) {
    $dir = "$stubBase\$($cls.Pkg)"
    $pkg = "mod.azure.azurelibarmor.common.$($cls.Pkg -replace '\\','.')"
    $file = "$dir\$($cls.Name).java"

    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

    $content = @"
package $pkg;

/// Stub class for AzureLibArmor — the Fabric version uses different package names.
public class $($cls.Name) {
}
"@

    Set-Content -Path $file -Value $content -NoNewline
}

Write-Host "Generated $($classes.Count) AzureLibArmor stub classes"
