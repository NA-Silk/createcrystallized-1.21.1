package com.nasilk.createcrystallized.util.helper;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/// BLOCK REGISTRY HELPERS, please thank them before you go, they are very nice.
public class BlockRegistryHelper {
    private static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory) {
        return registerBlockInternal(name, blockFactory, null, null, null);
    }

    public static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable Supplier<ConnectedTextureBehaviour> connectedTextureBehaviour) {
        return registerBlockInternal(name, blockFactory, connectedTextureBehaviour, null, null);
    }

    public static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable NonNullFunction<T, Item> itemFactory) {
        return registerBlockInternal(name, blockFactory, null, itemFactory, null);
    }

    public static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable Item.Properties itemProperties) {
        return registerBlockInternal(name, blockFactory, null, null, itemProperties);
    }

    public static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable Supplier<ConnectedTextureBehaviour> connectedTextureBehaviour, @Nullable NonNullFunction<T, Item> itemFactory) {
        return registerBlockInternal(name, blockFactory, connectedTextureBehaviour, itemFactory, null);
    }

    public static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable Supplier<ConnectedTextureBehaviour> connectedTextureBehaviour, @Nullable Item.Properties itemProperties) {
        return registerBlockInternal(name, blockFactory, connectedTextureBehaviour, null, itemProperties);
    }

    public static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable NonNullFunction<T, Item> itemFactory, @Nullable Item.Properties itemProperties) {
        return registerBlockInternal(name, blockFactory, null, itemFactory, itemProperties);
    }

    public static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable Supplier<ConnectedTextureBehaviour> connectedTextureBehaviour, @Nullable NonNullFunction<T, Item> itemFactory, @Nullable Item.Properties itemProperties) {
        return registerBlockInternal(name, blockFactory, connectedTextureBehaviour, itemFactory, itemProperties);
    }

    private static <T extends Block> BlockEntry<T> registerBlockInternal(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable Supplier<ConnectedTextureBehaviour> connectedTextureBehaviour, @Nullable NonNullFunction<T, Item> itemFactory, @Nullable Item.Properties itemProperties) {
        BlockBuilder<T, CreateRegistrate> builder = REGISTRATE.block(name, blockFactory);
        if (connectedTextureBehaviour != null) builder.onRegister(CreateRegistrate.connectedTextures(connectedTextureBehaviour));

        BlockEntry<T> blockEntry = builder.register();
        if (itemFactory != null) setCustomBlockItem(name, blockEntry, itemFactory);
        else setBlockItem(name, blockEntry, itemProperties);

        return blockEntry;
    }

    private static <T extends Block> void setBlockItem(String name, BlockEntry<T> blockEntry, @Nullable Item.Properties itemProperties) {
        REGISTRATE.item(name, (properties) -> new BlockItem(blockEntry.get(), Objects.requireNonNullElse(itemProperties, properties))).register();
    }

    private static <T extends Block> void setCustomBlockItem(String name, BlockEntry<T> blockEntry, NonNullFunction<T, Item> itemFactory) {
        REGISTRATE.item(name, (properties) -> itemFactory.apply(blockEntry.get())).register();
    }
}
