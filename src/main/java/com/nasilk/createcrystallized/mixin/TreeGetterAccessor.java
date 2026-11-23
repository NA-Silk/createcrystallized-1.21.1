package com.nasilk.createcrystallized.mixin;

import com.simibubi.create.content.kinetics.saw.TreeCutter;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

@Mixin(TreeCutter.Tree.class)
public interface TreeGetterAccessor {
    @Accessor("logs")
    List<BlockPos> getLogs();

    @Accessor("leaves")
    List<BlockPos> getLeaves();

    @Accessor("attachments")
    List<BlockPos> getAttachments();
}
