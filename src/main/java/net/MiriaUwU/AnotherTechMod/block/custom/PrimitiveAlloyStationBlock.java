package net.MiriaUwU.AnotherTechMod.block.custom;


import com.mojang.serialization.MapCodec;
import net.MiriaUwU.AnotherTechMod.block.base.DirectionalBaseBlock;
import net.MiriaUwU.AnotherTechMod.entity.ModBlockEntities;
import net.MiriaUwU.AnotherTechMod.entity.PrimitiveAlloyStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class PrimitiveAlloyStationBlock extends DirectionalBaseBlock {
    public static final MapCodec<PrimitiveAlloyStationBlock> CODEC = simpleCodec(PrimitiveAlloyStationBlock::new);

    public PrimitiveAlloyStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrimitiveAlloyStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null :
                createTickerHelper(type, ModBlockEntities.PRIMATIVEALLOYSTATION_BE.get(), PrimitiveAlloyStationBlockEntity::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);

            // Structure center is 2 blocks behind core (core is outside edge)
            BlockPos structureCenter = pos.relative(facing.getOpposite(), 2);
            BlockPos coreBlockPos = pos;

            if (isStructureValid(level, structureCenter, coreBlockPos)) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof PrimitiveAlloyStationBlockEntity stationEntity) {
                    ((ServerPlayer) player).openMenu(
                            new SimpleMenuProvider(stationEntity, Component.literal("Primitive Alloy Station")), pos
                    );
                }
            } else {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(Component.literal("3x3 structure not found!"));
                }
                showStructureHintParticles((ServerLevel) level, structureCenter);
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    private boolean isStructureValid(Level level, BlockPos center, BlockPos coreBlockPos) {
        BlockPos base = center.below();
        Block brickBlock = Blocks.BRICKS;
        Block glassBlock = Blocks.GLASS;
        Block magmaBlock = Blocks.MAGMA_BLOCK;

        // Y-1: solid 3x3 magma
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos check = base.offset(dx, 0, dz);
                if (!level.getBlockState(check).is(magmaBlock)) return false;
            }
        }

        // Layers Y, Y+1, Y+2
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos layer = center.above(dy);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos check = layer.offset(dx, 0, dz);
                    boolean isEdge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    boolean isCorner = Math.abs(dx) == 2 && Math.abs(dz) == 2;

                    if (isEdge) {
                        Block block = level.getBlockState(check).getBlock();
                        if (dy == 0) {
                            // Skip brick check on core block pos
                            if (!check.equals(coreBlockPos) && block != brickBlock) return false;
                        } else {
                            if (block != brickBlock && block != glassBlock) return false;
                            if (isCorner && block == glassBlock) return false;
                        }
                    } else {
                        if (!level.getBlockState(check).isAir()) return false;
                    }
                }
            }
        }

        return true;
    }

    private void showStructureHintParticles(ServerLevel level, BlockPos center) {
        Block brickBlock = Blocks.BRICKS;
        Block glassBlock = Blocks.GLASS;
        Block magmaBlock = Blocks.MAGMA_BLOCK;

        BlockPos base = center.below();

        // Y-1 magma layer
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = base.offset(dx, 0, dz);
                if (!level.getBlockState(pos).is(magmaBlock)) {
                    spawnBlockMarker(level, pos, magmaBlock.defaultBlockState());
                }
            }
        }

        // Y, Y+1, Y+2 ring layers
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos layer = center.above(dy);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = layer.offset(dx, 0, dz);
                    boolean isEdge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    boolean isCorner = Math.abs(dx) == 2 && Math.abs(dz) == 2;

                    if (isEdge) {
                        BlockState expected;
                        if (dy == 0 || isCorner) {
                            expected = brickBlock.defaultBlockState();
                        } else {
                            expected = glassBlock.defaultBlockState();
                        }
                        if (!level.getBlockState(pos).is(expected.getBlock())) {
                            spawnBlockMarker(level, pos, expected);
                        }
                    }
                }
            }
        }
    }

    private void spawnBlockMarker(ServerLevel level, BlockPos pos, BlockState state) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK_MARKER, state),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                10, 0, 0, 0, 0
        );
    }
}