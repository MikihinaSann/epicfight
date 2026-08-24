package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.placement.StructureSpawnOverride;

/// Stub for NeoForge's RegisterSpawnPlacementsEvent.
public class RegisterSpawnPlacementsEvent {
    public enum Operation {
        OR, AND, REPLACE
    }

    public enum SpawnPlacementType {
        ON_GROUND, IN_WATER, IN_LAVA, IN_AIR
    }

    public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap, net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {}
}
