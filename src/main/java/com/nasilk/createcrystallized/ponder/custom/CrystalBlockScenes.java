package com.nasilk.createcrystallized.ponder.custom;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import dev.eriksonn.aeronautics.content.ponder.instructions.ChangePropellerRotateInstruction;
import dev.eriksonn.aeronautics.content.ponder.instructions.PropellerParticleSpawningInstruction;
import dev.eriksonn.aeronautics.content.ponder.instructions.PropellerRotateInstruction;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.*;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public class CrystalBlockScenes {
    // TODO remove plagiarism
    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public static void densite(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder); // Convert to Create scene for world instructions
        CreateSceneBuilder.WorldInstructions world = scene.world();
        scene.title("levitite", "Levitating contraptions with Levitite");
        scene.configureBasePlate(0, 0, 9);
        scene.setSceneOffsetY(-1.0f);
        scene.scaleSceneView(0.8f);

        OverlayInstructions overlay = scene.overlay();
        SelectionUtil select = util.select();
        ElementLink<WorldSectionElement> ground = world.showIndependentSection(select.fromTo(0, 0, 0, 8, 0, 8), Direction.UP);
        scene.idle(10);
        world.multiplyKineticSpeed(util.select().everywhere(), 1.0f);
        BlockPos propellerPos = new BlockPos(2, 3, 4);
        BlockPos gearshiftPos = new BlockPos(3, 3, 4);
        Selection propeller = select.fromTo(1, 2, 3, 1, 4, 5);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(util.grid().at(7, 3, 4), true, true));
        ElementLink<WorldSectionElement> airship = world.showIndependentSection(select.fromTo(2, 2, 2, 7, 2, 6), Direction.DOWN);
        ElementLink<WorldSectionElement> stirlingSection = null;
        scene.idle(5);

        for(int i = 0; i < 5; ++i) {
            int k = i > 1 ? 1 : 0;
            if (i == 2) {
                stirlingSection = world.showIndependentSection(select.position(propellerPos.offset(2, 0, 0)), Direction.DOWN);
            } else {
                world.showSectionAndMerge(select.fromTo(propellerPos.offset(5 - i - k, 0, 0), propellerPos.offset(5 - i - k, i == 3 ? 1 : 0, 0)), Direction.DOWN, airship);
            }

            scene.idle(2);
        }

        ElementLink<WorldSectionElement> propellerLink = world.showIndependentSection(propeller, Direction.EAST);
        scene.idle(4);
        ElementLink<WorldSectionElement> stairs = world.showIndependentSection(util.select().position(propellerPos.offset(0, 1, 2)).add(util.select().position(propellerPos.offset(4, 1, 2))).add(util.select().position(propellerPos.offset(0, 1, -2))).add(util.select().position(propellerPos.offset(4, 1, -2))), Direction.DOWN);
        world.moveSection(stairs, new Vec3(0.0d, -1.0d, 0.0d), 0);
        scene.idle(2);
        world.showSectionAndMerge(util.select().fromTo(propellerPos.offset(1, 0, 2), propellerPos.offset(3, 0, 2)).add(util.select().fromTo(propellerPos.offset(1, 0, -2), propellerPos.offset(3, 0, -2))), Direction.DOWN, airship);
        scene.idle(10);
        BlockState state = AeroBlocks.LEVITITE.getDefaultState();
        SimpleParticleType particle = ParticleTypes.FLAME;

        for(int i = 0; i < 3; ++i) {
            BlockPos p = propellerPos.offset(3 - i, 0, 2);
            world.replaceBlocks(util.select().position(p), state, false);
            scene.effects().emitParticles(p.getCenter(), withinBlockSpace(particle, 1.5f), 10.0f, 2);
            p = propellerPos.offset(3 - i, 0, -2);
            world.replaceBlocks(util.select().position(p), state, false);
            scene.effects().emitParticles(p.getCenter(), withinBlockSpace(particle, 1.5f), 10.0f, 2);
            scene.idle(5);
        }

        overlay.showText(50).pointAt(util.vector().topOf(propellerPos.offset(1, 0, 2))).attachKeyFrame().placeNearTarget().text("When enough Levitite is attached to a simulated contraption...");
        scene.idle(50);
        double revolutions = 0.75d;
        int duration = (int)(40.0d * revolutions);
        List<ElementLink<WorldSectionElement>> links = new ArrayList<>();
        links.add(airship);
        links.add(propellerLink);
        links.add(stairs);
        links.add(stirlingSection);
        PropellerRotateInstruction propellerRotate = new PropellerRotateInstruction(propellerPos, propellerLink, Direction.WEST, -32.0f, 4.0f);
        scene.addInstruction(propellerRotate);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotate, airship, 2.0f, -5.0f, 1.5f, false));

        for(ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link, new Vec3(1.5d, 0.0d, 0.0d), duration, SmoothMovementUtils.quadraticRise()));
        }

        scene.idle(20);
        duration -= 20;
        overlay.showText(40).pointAt(util.vector().topOf(propellerPos.offset(3, 0, 2))).attachKeyFrame().placeNearTarget().text("...it will keep the contraption afloat");
        scene.idle(duration);
        world.multiplyKineticSpeed(util.select().everywhere(), -1.0f);
        revolutions *= 2.0d;
        duration = (int)(40.0d * revolutions);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetRotationRate(propellerRotate, 32.0f));

        for(ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link, new Vec3(1.5d, 0.0d, 0.0d), duration, SmoothMovementUtils.quadraticJump()));
        }

        scene.idle(duration);
        world.multiplyKineticSpeed(util.select().everywhere(), -1.0f);
        revolutions /= 2.0d;
        duration = (int)(40.0d * revolutions);
        scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotate, 20.0f));

        for(ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link, new Vec3(-1.5d, 0.0d, 0.0d), duration, SmoothMovementUtils.quadraticRiseDual()));
        }

        scene.idle(duration);
        world.toggleRedstonePower(util.select().fromTo(gearshiftPos, gearshiftPos.above()));
        scene.idle(5);
        world.hideIndependentSection(stairs, Direction.UP);
        scene.idle(16);
        Selection levititeCorners = util.select().position(propellerPos.offset(0, 0, 2)).add(util.select().position(propellerPos.offset(4, 0, 2))).add(util.select().position(propellerPos.offset(0, 0, -2))).add(util.select().position(propellerPos.offset(4, 0, -2)));

        world.showSectionAndMerge(levititeCorners, Direction.DOWN, airship);
        scene.idle(15);
        overlay.showText(60).pointAt(util.vector().topOf(propellerPos.offset(1, 0, 2))).attachKeyFrame().placeNearTarget().text("Simulated Contraptions cannot gain altitude using Levitite alone");
        scene.idle(70);
        world.moveSection(stirlingSection, new Vec3(1.0d, 0.0d, 0.0d), 10);
        scene.idle(20);
        Selection minipropSelection = select.fromTo(propellerPos.offset(2, 1, 0), propellerPos.offset(2, 2, 0));
        ElementLink<WorldSectionElement> miniProp = world.showIndependentSection(minipropSelection, Direction.DOWN);
        links.add(miniProp);
        world.moveSection(miniProp, new Vec3(0.0d, -1.0d, 0.0d), 0);
        world.setKineticSpeed(minipropSelection, -32.0f);
        scene.idle(20);
        scene.addInstruction(new PropellerParticleSpawningInstruction(miniProp, propellerPos.offset(2, 2, 0), Direction.DOWN, 25, 2.0f, 3.0f, 0.7f));

        for(ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link, new Vec3(0.0d, 0.5d, 0.0d), 25, SmoothMovementUtils.quadraticRise()));
        }

        scene.idle(25);
        overlay.showText(60).pointAt(util.vector().centerOf(propellerPos.offset(2, 2, 0))).attachKeyFrame().placeNearTarget().text("Instead, additional forces are required");
        world.multiplyKineticSpeed(select.everywhere(), -1.0f);
        scene.addInstruction(new PropellerParticleSpawningInstruction(miniProp, propellerPos.offset(2, 2, 0), Direction.UP, 50, 1.0f, 3.0f, 0.7f));

        for(ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link, new Vec3(0.0d, 0.5d, 0.0d), 50, SmoothMovementUtils.quadraticJump()));
        }

        scene.idle(50);
        world.multiplyKineticSpeed(select.everywhere(), -1.0f);
        scene.addInstruction(new PropellerParticleSpawningInstruction(miniProp, propellerPos.offset(2, 2, 0), Direction.DOWN, 25, 1.0f, 3.0f, 0.7f));

        for(ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link, new Vec3(0.0d, -0.5d, 0.0d), 25, SmoothMovementUtils.quadraticRiseDual()));
        }

        scene.idle(30);
        world.hideIndependentSection(miniProp, Direction.UP);
        scene.idle(15);
        world.moveSection(stirlingSection, new Vec3(-1.0d, 0.0d, 0.0d), 10);
        scene.idle(20);
        scene.addInstruction(new CustomToggleBaseShadowInstruction());
        world.toggleRedstonePower(util.select().fromTo(gearshiftPos, gearshiftPos.above()));
        propellerRotate = new PropellerRotateInstruction(propellerPos, propellerLink, Direction.WEST, -32.0f, 4.0f);
        scene.addInstruction(propellerRotate);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotate, airship, 2.0f, -5.0f, 1.5f, false));
        ElementLink<WorldSectionElement> ground2 = world.showIndependentSection(util.select().fromTo(9, 0, 2, 39, 1, 6), Direction.WEST);
        ElementLink<WorldSectionElement>[] grounds = new ElementLink[]{ground, ground2};

        for(ElementLink<WorldSectionElement> link : grounds) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link, new Vec3(-0.5d, 0.0d, 0.0d), 20, SmoothMovementUtils.quadraticRise()));
        }

        scene.idle(20);
        double movementDistance = 6.0d;
        int movementDuration = (int)(20.0d * movementDistance);

        for(ElementLink<WorldSectionElement> link : grounds) {
            world.moveSection(link, new Vec3((-movementDistance), 0.0d, 0.0d), movementDuration);
        }

        scene.idle(10);
        movementDuration -= 10;
        AABB aabb1 = (new AABB(propellerPos.offset(0, 0, 2))).expandTowards(4.0d, 0.0d, 0.0d);
        AABB aabb2 = (new AABB(propellerPos.offset(0, 0, -2))).expandTowards(4.0d, 0.0d, 0.0);
        overlay.chaseBoundingBoxOutline(PonderPalette.RED, aabb1, aabb1.contract(-5.0d, 0.0d, 0.0d), 1);
        overlay.chaseBoundingBoxOutline(PonderPalette.RED, aabb1, aabb1, 100);
        overlay.chaseBoundingBoxOutline(PonderPalette.RED, aabb2, aabb2.contract(-5.0d, 0.0d, 0.0d), 1);
        overlay.chaseBoundingBoxOutline(PonderPalette.RED, aabb2, aabb2, 100);
        scene.idle(10);
        movementDuration -= 10;
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.RED, aabb1, 90, Direction.WEST, 1.0f, 1.0f, false, false));
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.RED, aabb2, 90, Direction.WEST, 1.0f, 1.0f, false, false));
        scene.idle(15);
        movementDuration -= 15;
        overlay.showText(60).pointAt(util.vector().topOf(propellerPos.offset(1, 0, 2))).attachKeyFrame().placeNearTarget().text("At low movement speeds, Levitite significantly resists motion");
        scene.idle(movementDuration - 4);
        world.hideIndependentSection(ground, null);
        overlay.showControls(util.vector().topOf(gearshiftPos.east()), Pointing.DOWN, 10).withItem(new ItemStack(AllItems.BLAZE_CAKE.asItem()));
        scene.idle(4);
        world.multiplyKineticSpeed(util.select().everywhere(), 2.0f);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetRotationRate(propellerRotate, -64.0f));

        for(ElementLink<WorldSectionElement> link : grounds) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link, new Vec3(-2.0d, 0.0d, 0.0d), 40, (t) -> SmoothMovementUtils.quadraticRise().apply(t) + t));
        }

        scene.idle(20);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground2, new Vec3(-0.5d, 0.0d, 0.0d), 20, SmoothMovementUtils.quadraticRise()));
        scene.idle(20);
        movementDistance = 24.0d;
        movementDuration = (int)(5.0d * movementDistance);
        world.moveSection(ground2, new Vec3(-movementDistance, 0.0d, 0.0d), movementDuration);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, aabb1, aabb1.contract(-5.0d, 0.0d, 0.0d), 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, aabb1, aabb1, 100);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, aabb2, aabb2.contract(-5.0d, 0.0d, 0.0d), 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, aabb2, aabb2, 100);
        scene.idle(10);
        movementDuration -= 10;
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.INPUT, aabb1, 90, Direction.WEST, 4.0F, 3.0F, false, false));
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.INPUT, aabb2, 90, Direction.WEST, 4.0F, 3.0F, false, false));
        scene.idle(15);
        movementDuration -= 15;
        scene.overlay().showText(60).pointAt(util.vector().topOf(propellerPos.offset(1, 0, 2))).attachKeyFrame().placeNearTarget().text("This resistance drops at higher movement speeds");
        scene.idle(movementDuration);
        world.multiplyKineticSpeed(util.select().everywhere(), 0.0f);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground2, new Vec3(-5.0d, 0.0d, 0.0d), 50, SmoothMovementUtils.quadraticRiseDual()));
        ElementLink<WorldSectionElement> ground3 = world.showIndependentSection(util.select().fromTo(0, 0, 0, 8, 0, 8), Direction.WEST);
        world.moveSection(ground3, new Vec3(5.0d, 0.0d, 0.0d), 0);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground3, new Vec3(-5.0d, 0.0d, 0.0d), 50, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotate, 30.0f));
        scene.idle(20);
        world.hideIndependentSection(ground2, null);
        scene.markAsFinished();
        scene.idle(30);
        scene.addInstruction(new CustomToggleBaseShadowInstruction());
    }

    // TODO
    public static void propulsite(SceneBuilder builder, SceneBuildingUtil util) {
        densite(builder, util);
    }

    // TODO
    public static void oscillite(SceneBuilder builder, SceneBuildingUtil util) {
        densite(builder, util);
    }

    public static <T extends ParticleOptions> ParticleEmitter withinBlockSpace(T data, float range) {
        return (w, x, y, z) -> w.addParticle(
            data,
            Math.floor(x) + 0.5d + (w.random.nextDouble() - 0.5d) * range,
            Math.floor(y) + 0.5d + (w.random.nextDouble() - 0.5d) * range,
            Math.floor(z) + 0.5d + (w.random.nextDouble() - 0.5d) * range,
            0.0d, 0.0d, 0.0d
        );
    }
}
