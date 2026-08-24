package yesman.epicfight.mixin.client;

import net.minecraft.client.renderer.OutlineBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OutlineBufferSource.class)
public interface OutlineBufferSourceAccessor {
    @Accessor("teamR")
    float epicfight$getTeamR();
    @Accessor("teamR")
    void epicfight$setTeamR(float value);
    @Accessor("teamG")
    float epicfight$getTeamG();
    @Accessor("teamG")
    void epicfight$setTeamG(float value);
    @Accessor("teamB")
    float epicfight$getTeamB();
    @Accessor("teamB")
    void epicfight$setTeamB(float value);
    @Accessor("teamA")
    float epicfight$getTeamA();
    @Accessor("teamA")
    void epicfight$setTeamA(float value);
}
