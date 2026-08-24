package net.neoforged.neoforge.common.loot;
import com.mojang.serialization.MapCodec;
public class LootModifier {
    public LootModifier() {}
    public LootModifier(Object[] conditions) {}
    public Object apply(Object generatedLoot, Object context) { return generatedLoot; }
    public static <T> com.mojang.serialization.codecs.RecordCodecBuilder<T, T> codecStart(com.mojang.serialization.codecs.RecordCodecBuilder.Instance<T> instance) { return null; }
    public MapCodec<? extends LootModifier> codec() { return null; }
}
