package yesman.epicfight.mixin.client;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ContainerObjectSelectionList.Entry.class)
public interface ContainerObjectSelectionListEntryAccessor {
    @Accessor("focused")
    void epicfight$setFocused(Object entry);
}
