package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.common.ModCreativeModeTabs;
import dev.simulated_team.simulated.client.sections.SimulatedSection;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.registrate.simulated_tab.SimulatedCreativeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.List;

@Mixin(value = SimulatedCreativeTab.class, remap = false)
public class SimulatedCreativeTabMixin {
    @ModifyVariable(method = "renderBanners", at = @At("STORE"), name = "sections")
    private static List<SimulatedSection> hideSection(List<SimulatedSection> sections) {
        return sections.stream().filter(
            section -> !SimResourceManagers.SIMULATED_SECTION.getId(section).equals(ModCreativeModeTabs.CRYSTALLIZED_SECTION)
        ).toList();
    }
}
