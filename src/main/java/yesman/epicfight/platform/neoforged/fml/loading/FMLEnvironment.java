package yesman.epicfight.platform.neoforged.fml.loading;

import yesman.epicfight.platform.neoforged.api.distmarker.Dist;

/// Stub for NeoForge's FMLEnvironment.
public class FMLEnvironment {
    public static final Dist dist = net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    public static final boolean production = false;
}
