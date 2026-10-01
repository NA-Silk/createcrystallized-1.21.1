package com.nasilk.createcrystallized.ponder.custom;

import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverBlockEntity;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.CustomAnimateWorldSectionInstruction;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * See Simulated -> RedstoneScenes.redstoneMagnet
 */
public class MachinedCrystalBlockScenes {
    private static final int COMMON_DELAY = 10;
    private static final int MOVEMENT_DELAY = 20;
    private static final int FOCUS_DELAY = 40;

    private static final double WELL_POWER_INCREASE = 1.0d;

    /**
     * text_1: Densite Wells generate a gravitational field when powered.<br>
     * text_2: Use a redstone input to control the Well's power and range.<br>
     * text_3: Physics assemblies inside the field are affected by the Well.<br>
     * text_4: A low redstone signal creates a smaller field.<br>
     * text_5: The weaker field slowly pulls nearby physics assemblies toward the Well.<br>
     * text_6: Increasing the redstone signal strengthens and enlarges the field.<br>
     * text_7: The stronger field quickly pulls nearby physics assemblies toward the Well.
     */
    public static void densiteWell(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("densite_well", "Using Densite Wells");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(0.8f);

        /// INTRO
        // Set positions
        BlockPos wellPos = util.grid().at(7, 1, 4);
        BlockPos throttlePos = util.grid().at(7, 2, 4);
        BlockPos aerolitePos1 = util.grid().at(1, 2, 4);
        BlockPos aerolitePos2 = util.grid().at(1, 3, 4);

        // Set selections
        Selection base = util.select().layer(0);
        Selection well = util.select().position(wellPos);
        Selection throttle = util.select().position(throttlePos);
        Selection assembly = util.select().fromTo(aerolitePos1, aerolitePos2);

        // Show base
        scene.world().showSection(base, Direction.UP);
        scene.idle(COMMON_DELAY);

        // Show well
        scene.world().showSection(well, Direction.DOWN);
        scene.idle(COMMON_DELAY);
        scene.overlay().showText(FOCUS_DELAY).attachKeyFrame()
            .pointAt(util.vector().topOf(wellPos)).placeNearTarget()
            .text("text_1");
        scene.idle(FOCUS_DELAY);

        // Show throttle
        scene.world().showSection(throttle, Direction.DOWN);
        scene.idle(COMMON_DELAY);
        scene.overlay().showText(FOCUS_DELAY).attachKeyFrame()
            .pointAt(util.vector().topOf(throttlePos)).placeNearTarget()
            .text("text_2");
        scene.idle(FOCUS_DELAY);

        // Assemble blocks
        ElementLink<WorldSectionElement> blocks = scene.world().showIndependentSection(assembly, Direction.DOWN);
        scene.idle(COMMON_DELAY);
        scene.overlay().showText(FOCUS_DELAY).attachKeyFrame()
            .pointAt(util.vector().topOf(aerolitePos1)).placeNearTarget()
            .text("text_3");
        scene.idle(FOCUS_DELAY);

        /// LOW POWER
        // Apply redstone input
        scene.world().modifyBlockEntityNBT(throttle, ThrottleLeverBlockEntity.class, nbt -> nbt.putInt("State", 5));
        scene.idle(COMMON_DELAY);
        scene.overlay().showText(FOCUS_DELAY).attachKeyFrame()
            .pointAt(util.vector().topOf(throttlePos)).placeNearTarget()
            .text("text_4");
        scene.idle(FOCUS_DELAY);

        // Show field
        AABB lowField = new AABB(
            wellPos.getX() - 1.5d,
            0.5d,
            wellPos.getZ() - 1.5d,
            wellPos.getX() + 2.5d,
            wellPos.getY() + 3.5d,
            wellPos.getZ() + 2.5d
        );
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, lowField, lowField, MOVEMENT_DELAY);
        scene.idle(MOVEMENT_DELAY);

        // Move assembled blocks
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blocks, util.vector().of(3.5d, 0.0d, 0.0d), (int) (1.0d + WELL_POWER_INCREASE) * MOVEMENT_DELAY, SmoothMovementUtils.cubicSmoothing()));
        scene.idle(MOVEMENT_DELAY);
        scene.overlay().showText(FOCUS_DELAY).attachKeyFrame()
            .pointAt(util.vector().topOf(wellPos)).placeNearTarget()
            .text("text_5");
        scene.idle(FOCUS_DELAY);

        // Reset
        scene.world().hideIndependentSection(blocks, Direction.DOWN);
        scene.idle(COMMON_DELAY);
        blocks = scene.world().showIndependentSection(assembly, Direction.DOWN);
        scene.idle(COMMON_DELAY);

        /// HIGH POWER
        // Apply redstone input
        scene.world().modifyBlockEntityNBT(throttle, ThrottleLeverBlockEntity.class, nbt -> nbt.putInt("State", 12));
        scene.idle(COMMON_DELAY);
        scene.overlay().showText(FOCUS_DELAY).attachKeyFrame()
            .pointAt(util.vector().topOf(throttlePos)).placeNearTarget()
            .text("text_6");
        scene.idle(FOCUS_DELAY);

        // Show field
        AABB highField = new AABB(
            wellPos.getX() - 1.5d - WELL_POWER_INCREASE,
            0.5d,
            wellPos.getZ() - 1.5d - WELL_POWER_INCREASE,
            wellPos.getX() + 2.5d + WELL_POWER_INCREASE,
            wellPos.getY() + 3.5d + WELL_POWER_INCREASE,
            wellPos.getZ() + 2.5d + WELL_POWER_INCREASE
        );
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, highField, highField, MOVEMENT_DELAY);
        scene.idle(MOVEMENT_DELAY);

        // Move assembled blocks
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blocks, util.vector().of(3.5d + WELL_POWER_INCREASE, 0.0d, 0.0d), MOVEMENT_DELAY, SmoothMovementUtils.cubicSmoothing()));
        scene.idle(MOVEMENT_DELAY);
        scene.overlay().showText(FOCUS_DELAY).attachKeyFrame()
            .pointAt(util.vector().topOf(wellPos)).placeNearTarget()
            .text("text_7");
        scene.idle(FOCUS_DELAY);

        /// EXIT
        // End scene
        scene.markAsFinished();
        scene.idle(FOCUS_DELAY);
    }

    // TODO
    public static void propulsiteThruster(SceneBuilder scene, SceneBuildingUtil util) {
        densiteWell(scene, util);
    }

    // TODO
    public static void oscilliteCannon(SceneBuilder scene, SceneBuildingUtil util) {
        densiteWell(scene, util);
    }
}
