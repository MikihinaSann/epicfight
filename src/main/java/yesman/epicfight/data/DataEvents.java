package yesman.epicfight.data;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;




import yesman.epicfight.data.recipes.pack.EpicFightRecipeProvider;
import yesman.epicfight.data.tags.EpicFightBlockTagsProvider;
import yesman.epicfight.data.tags.EpicFightItemTagsProvider;
import yesman.epicfight.main.EpicFightMod;


public final class DataEvents {
	private DataEvents() {}
	
	
	public static void epicfight$gatherData(net.neoforged.neoforge.data.event.GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
        PackOutput packOutput = null; // TODO: gen.getPackOutput()
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        
        // TODO: addProvider
        // gen.addProvider(event.includeServer(), new EpicFightRecipeProvider(packOutput, lookupProvider));
        EpicFightBlockTagsProvider blockTagsProvider = new EpicFightBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);;
        // TODO: addProvider
        // gen.addProvider(event.includeServer(), blockTagsProvider);
        // TODO: addProvider
        // gen.addProvider(event.includeServer(), new EpicFightItemTagsProvider(packOutput, lookupProvider, null, existingFileHelper));
	}
}
