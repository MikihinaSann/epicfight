package net.neoforged.neoforge.common.loot;
import com.mojang.serialization.MapCodec;
public class LootModifier {
    public LootModifier() {}
    public LootModifier(Object[] conditions) {}
    public Object apply(Object generatedLoot, Object context) { return generatedLoot; }
    public MapCodec<? extends LootModifier> codec() { return null; }
}
