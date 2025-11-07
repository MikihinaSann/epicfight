package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.client.ClientEngine;

@OnlyIn(Dist.CLIENT)
public class CombatKeyMapping extends KeyMapping {
    public CombatKeyMapping(String description, int code, String category) {
        this(description, InputConstants.Type.KEYSYM, code, category);
    }

    /**
     * This key mapping only applies {@link KeyConflictContext#IN_GAME} since it represents player moves
     */
	public CombatKeyMapping(String description, InputConstants.Type type, int code, String category) {
		super(description, KeyConflictContext.IN_GAME, type, code, category);
	}
	
	@Override
	public boolean isActiveAndMatches(@NotNull InputConstants.Key keyCode) {
        return super.isActiveAndMatches(keyCode) && ClientEngine.getInstance().isEpicFightMode();
    }
}