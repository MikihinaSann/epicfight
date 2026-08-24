package yesman.epicfight.data.tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.client.Minecraft;
import yesman.epicfight.EpicFight;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;


import yesman.epicfight.main.EpicFightMod;

public class EpicFightBlockTagsProvider extends BlockTagsProvider {
	public EpicFightBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, EpicFight.MODID, existingFileHelper);
    }
	
	protected void addTags(Provider provider) {
	}
}
