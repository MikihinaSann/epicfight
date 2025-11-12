package yesman.epicfight.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;

@OnlyIn(Dist.CLIENT)
public interface AuthenticationHelper {
	boolean valid();
	
	void initialize(
		ConfigValue<String> accessToken,
		ConfigValue<String> refreshToken,
		EnumValue<AuthenticationProvider> provider
	);
	
	default Screen getAvatarEditorScreen(Screen parentScreen) {
		return null;
	}

    Status status();

    /**
     * Load the player's skin texture
     * This method must be called after some point that {@link Minecraft#skinManager} is loaded
     */
    void loadPlayerSkin();

	@OnlyIn(Dist.CLIENT)
	enum Status {
		UNAUTHENTICATED, AUTHENTICATED, OFFLINE_MODE;
	}
	
	@OnlyIn(Dist.CLIENT)
	enum AuthenticationProvider {
		NULL("null"), DISCORD("discord"), PATREON("patreon");
		
		final String signature;
		
		AuthenticationProvider(String signature) {
			this.signature = signature;
		}
		
		@Override
		public String toString() {
			return this.signature;
		}
	}
}
