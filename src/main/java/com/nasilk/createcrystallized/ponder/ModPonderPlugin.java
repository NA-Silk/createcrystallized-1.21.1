package com.nasilk.createcrystallized.ponder;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.IndexExclusionHelper;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModPonderPlugin extends CreatePonderPlugin {
    public ModPonderPlugin() {}

    @Override
    public String getModId() {
        return CreateCrystallized.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ModPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        ModPonderTags.register(helper);
    }

    @Override
    public void registerSharedText(@NotNull SharedTextRegistrationHelper helper) {}

    @Override
    public void onPonderLevelRestore(PonderLevel ponderLevel) {}

    @Override
    public void indexExclusions(IndexExclusionHelper helper) {}
}
