package com.nasilk.createcrystallized.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

public class AerolitePickaxeBlockEntity extends BlockEntity {
    private ItemStack stack = ItemStack.EMPTY;
    public ItemStack getStack() {
        return this.stack;
    }
    public void setStack(ItemStack stack) {
        this.stack = stack.copy();
        setChanged();
    }
    public ItemStack popStack() {
        ItemStack result = this.stack.copy();
        this.stack = ItemStack.EMPTY;
        setChanged();
        return result;
    }

    private final Vector3d relPosition = new Vector3d();
    public Vector3d getRelPosition() {
        return this.relPosition;
    }
    public void setRelPosition(Vector3d relPosition) {
        this.relPosition.set(relPosition);
    }

    private final Vector3d normal = new Vector3d();
    public Vector3d getNormal() {
        return this.normal;
    }
    public void setNormal(Vector3d normal) {
        this.normal.set(normal);
    }

    private float roll = 0.0f;
    public float getRoll() {
        return this.roll;
    }
    public void setRoll(float roll) {
        this.roll = roll;
    }


    public AerolitePickaxeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    // TICK BEHAVIOR
    public void tick() {
        //CreateCrystallized.LOGGER.info("AerolitePickaxeBlockEntity is ticking");
    }


    // DATA PERSISTENCE
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.stack.isEmpty()) tag.put("Stack", this.stack.save(registries));
        tag.putDouble("RelPositionX", this.relPosition.x);
        tag.putDouble("RelPositionY", this.relPosition.y);
        tag.putDouble("RelPositionZ", this.relPosition.z);
        tag.putDouble("NormalX", this.normal.x);
        tag.putDouble("NormalY", this.normal.y);
        tag.putDouble("NormalZ", this.normal.z);
        tag.putFloat("Roll", this.roll);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.stack = tag.contains("Stack") ?
            ItemStack.parse(registries, tag.getCompound("Stack")).orElse(ItemStack.EMPTY) :
            ItemStack.EMPTY;
        this.relPosition.set(tag.getDouble("RelPositionX"), tag.getDouble("RelPositionY"), tag.getDouble("RelPositionZ"));
        this.normal.set(tag.getDouble("NormalX"), tag.getDouble("NormalY"), tag.getDouble("NormalZ"));
        this.roll = tag.getFloat("Roll");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
