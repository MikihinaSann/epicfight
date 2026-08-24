package yesman.epicfight.config;
import fuzs.forgeconfigapiport.api.config.v3.ModConfigEvent;

import net.fabricmc.api.EnvType;


import fuzs.forgeconfigapiport.api.config.v3.ModConfig;

import fuzs.forgeconfigapiport.api.config.v3.ModConfigSpec;
import yesman.epicfight.main.EpicFightMod;


public class ServerConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec.BooleanValue ALLOW_CUSTOM_ANIMATIONS = BUILDER.define("allow_custom_animations", false);
	public static final ModConfigSpec SPEC = BUILDER.build();
	
	public static boolean allowCustomAnimations;
	
	
    static void onLoad(final ModConfigEvent.Loading event) {
		if (event.getConfig().getType() != ModConfig.Type.SERVER) {
			return;
		}
		
		allowCustomAnimations = ALLOW_CUSTOM_ANIMATIONS.get();
	}
}
