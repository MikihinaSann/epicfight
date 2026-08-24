package yesman.epicfight.mixin.common;
import net.minecraft.client.Minecraft;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.IdMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Accessor mixin for IdMapper private fields.
@Mixin(IdMapper.class)
public interface IdMapperAccessor<T> {
    @Accessor("tToId")
    Object2IntMap<T> epicfight$getTToId();

    @Accessor("idToT")
    Int2ObjectMap<T> epicfight$getIdToT();

    @Accessor("nextId")
    int epicfight$getNextId();

    @Accessor("nextId")
    void epicfight$setNextId(int value);
}
