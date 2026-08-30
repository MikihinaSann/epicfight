package yesman.epicfight.platform.neoforged.data.event;

import net.minecraft.data.DataGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import yesman.epicfight.platform.neoforged.common.data.ExistingFileHelper;
import java.util.concurrent.CompletableFuture;

/// Stub for NeoForge's GatherDataEvent.
public class GatherDataEvent {
    public DataGenerator getGenerator() { return null; }
    public PackOutput getPackOutput() { return null; }
    public CompletableFuture<HolderLookup.Provider> getLookupProvider() { return null; }
    public ExistingFileHelper getExistingFileHelper() { return null; }
    public boolean includeServer() { return true; }
    public boolean includeClient() { return true; }
}
