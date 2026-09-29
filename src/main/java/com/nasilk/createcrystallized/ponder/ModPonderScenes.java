package com.nasilk.createcrystallized.ponder;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.CustomAnimateWorldSectionInstruction;
import dev.simulated_team.simulated.ponder.instructions.PullTheAssemblerKronkInstruction;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModPonderScenes {
    public ModPonderScenes() {}

    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> registry) {
        final PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registry.withKeyFunction(DeferredHolder::getId);

        helper.forComponents(ModBlocks.DENSITE_WELL)
            .addStoryBoard("physics_behavior/densite_well", ModPonderScenes::redstoneMagnet);
    }

    // Thanks Simulated RedstoneScenes.class
    public static void redstoneMagnet(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("redstone_magnet", "Using Redstone Magnets");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.scaleSceneView(0.8F);
        scene.idle(10);
        BlockPos magnet1Pos = new BlockPos(7, 3, 4);
        BlockPos lever1Pos = new BlockPos(7, 2, 3);
        BlockPos magnet2Pos = new BlockPos(4, 3, 4);
        BlockPos lever2Pos = new BlockPos(3, 3, 4);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(new BlockPos(1, 3, 4), true, true));
        scene.world().showSection(util.select().fromTo(7, 1, 3, 8, 2, 5), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(magnet1Pos), Direction.DOWN);
        scene.idle(7);
        ElementLink<WorldSectionElement> cart = scene.world().showIndependentSection(util.select().fromTo(1, 1, 3, 4, 2, 5), Direction.DOWN);
        scene.idle(5);
        for(int i = 4; i >= 1; --i) {
            scene.world().showSectionAndMerge(util.select().position(i, 3, 4), Direction.DOWN, cart);
            scene.idle(3);
        }
        scene.world().showSectionAndMerge(util.select().fromTo(1, 3, 3, 3, 3, 3), Direction.DOWN, cart);
        scene.world().showSectionAndMerge(util.select().fromTo(1, 3, 5, 3, 3, 5), Direction.DOWN, cart);
        scene.idle(5);
        scene.overlay().showText(60).attachKeyFrame().text("Redstone Magnets can attract or repel other Redstone Magnets").pointAt(util.vector().topOf(magnet2Pos)).placeNearTarget();
        scene.idle(70);
        scene.effects().indicateRedstone(lever1Pos);
        scene.world().toggleRedstonePower(util.select().fromTo(magnet1Pos, lever1Pos));
        scene.idle(10);
        scene.effects().indicateRedstone(lever2Pos);
        scene.world().toggleRedstonePower(util.select().fromTo(magnet2Pos, lever2Pos));
        scene.idle(10);
        Vec3 m1 = Vec3.atCenterOf(magnet1Pos).add(-0.5F, 0.0F, 0.0F);
        Vec3 m2 = Vec3.atCenterOf(magnet2Pos).add(0.5F, 0.0F, 0.0F);
        AABB bb1 = new AABB(m1, m1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb1, bb1, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb1, bb1.expandTowards(-0.96, 0.0F, 0.0F), 20);
        AABB bb2 = new AABB(m2, m2);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2.expandTowards(0.96, 0.0F, 0.0F), 20);
        scene.idle(10);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cart, new Vec3(2.0F, 0.0F, 0.0F), 20, SmoothMovementUtils.cubicRise()));
        scene.idle(25);
        scene.overlay().showText(50).attachKeyFrame().text("Opposite poles attract each other").pointAt(util.vector().topOf(magnet2Pos.offset(2, 0, 0))).placeNearTarget();
        scene.idle(40);
        scene.world().toggleRedstonePower(util.select().fromTo(magnet1Pos, lever1Pos));
        scene.world().toggleRedstonePower(util.select().fromTo(magnet2Pos, lever2Pos));
        scene.world().moveSection(cart, new Vec3(-2.0F, 0.0F, 0.0F), 15);
        scene.idle(20);
        ElementLink<WorldSectionElement> magnetSection = scene.world().makeSectionIndependent(util.select().position(magnet1Pos));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(magnetSection, new Vec3(0.0F, 0.5F, 0.0F), 20, SmoothMovementUtils.quadraticJump()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(magnetSection, new Vec3(0.0F, 0.0F, 180.0F), 20, SmoothMovementUtils.cubicSmoothing()));
        scene.idle(25);
        scene.effects().indicateRedstone(lever1Pos);
        scene.world().toggleRedstonePower(util.select().fromTo(magnet1Pos, lever1Pos));
        scene.idle(10);
        scene.effects().indicateRedstone(lever2Pos);
        scene.world().toggleRedstonePower(util.select().fromTo(magnet2Pos, lever2Pos));
        scene.idle(10);
        m1 = Vec3.atCenterOf(magnet1Pos).add(Vec3.atCenterOf(magnet2Pos)).scale(0.5F);
        m1 = m1.add(0.03, 0.0F, 0.0F);
        m2 = m1.add(-0.03, 0.0F, 0.0F);
        bb1 = new AABB(m1, m1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb1, bb1, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb1, bb1.expandTowards(0.96, 0.0F, 0.0F), 20);
        bb2 = new AABB(m2, m2);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2.expandTowards(-0.96, 0.0F, 0.0F), 20);
        scene.idle(10);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cart, new Vec3(-10.0F, 0.0F, 0.0F), 60, SmoothMovementUtils.asymptoticAcceleration(3.0F)));
        scene.idle(18);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cart, new Vec3(0.0F, -5.0F, 0.0F), 40, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(cart, new Vec3(0.0F, 0.0F, 170.0F), 40, SmoothMovementUtils.asymptoticAcceleration(2.0F)));
        scene.overlay().showText(50).attachKeyFrame().text("Similar poles repel each other").pointAt(util.vector().topOf(magnet2Pos.offset(-3, 0, 0))).placeNearTarget();
        scene.idle(20);
        //noinspection DataFlowIssue
        scene.world().hideIndependentSection(cart, null);
        scene.idle(30);
        cart = scene.world().showIndependentSection(util.select().fromTo(1, 1, 3, 4, 3, 5), Direction.DOWN);
        scene.world().moveSection(cart, new Vec3(0.5F, 0.0F, 1.5F), 0);
        scene.world().rotateSection(cart, 0.0F, 90.0F, 0.0F, 0);
        scene.world().toggleRedstonePower(util.select().fromTo(magnet1Pos, lever1Pos));
        scene.world().toggleRedstonePower(util.select().fromTo(magnet2Pos, lever2Pos));
        scene.idle(5);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(magnetSection, new Vec3(0.0F, 0.5F, 0.0F), 20, SmoothMovementUtils.quadraticJump()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(magnetSection, new Vec3(0.0F, 0.0F, 180.0F), 20, SmoothMovementUtils.cubicSmoothing()));
        scene.idle(25);
        scene.effects().indicateRedstone(lever1Pos);
        scene.world().toggleRedstonePower(util.select().fromTo(magnet1Pos, lever1Pos));
        scene.idle(10);
        scene.effects().indicateRedstone(lever2Pos.offset(0, 0, 1));
        scene.world().toggleRedstonePower(util.select().fromTo(magnet2Pos, lever2Pos));
        scene.idle(10);
        m1 = Vec3.atCenterOf(magnet1Pos).add(-0.5F, 0.0F, 0.0F);
        m2 = Vec3.atCenterOf(magnet2Pos).add(-1.0F, 0.0F, -0.5F);
        bb1 = new AABB(m1, m1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb1, bb1, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb1, bb1.expandTowards(-0.96, 0.0F, 0.0F), 20);
        bb2 = new AABB(m2, m2);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2.expandTowards(0.0F, 0.0F, -0.96), 20);
        scene.idle(10);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cart, new Vec3(0.0F, 0.0F, -1.5F), 30, SmoothMovementUtils.cubicSmoothing()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(cart, new Vec3(0.0F, -90.0F, 0.0F), 30, SmoothMovementUtils.cubicSmoothing()));
        scene.idle(15);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cart, new Vec3(1.5F, 0.0F, 0.0F), 20, SmoothMovementUtils.cubicRise()));
        scene.idle(25);
        scene.overlay().showText(50).attachKeyFrame().text("Attracting magnets attempt to align with each other").pointAt(util.vector().topOf(magnet2Pos.offset(2, 0, 0))).placeNearTarget();
        scene.idle(50);
    }
}
