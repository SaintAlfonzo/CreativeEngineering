package com.SaintAlfonzo.creativedelight.hearth.worldgen.structure;

import com.SaintAlfonzo.creativedelight.hearth.worldgen.HHWorldgen;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The maze: walls of mature Corn Delight corn on farmland (a dirt floor would break the corn on the next neighbour
 * update), 1.4.1's hay, pumpkin and scarecrow decor, chests in dead ends and in the centre, a gate at each end.
 * Corn Delight's corn is two blocks high (1.4.1's stalk three) and has no collision.
 */
public class CornMazePiece extends StructurePiece {
    private static final ResourceLocation CORN_ID = new ResourceLocation("corn_delight", "corn_crop");
    private static final ResourceLocation SCARECROW_ID = new ResourceLocation("hearthandharvest", "scarecrow");
    private static final ResourceLocation LOOT = new ResourceLocation("hearthandharvest", "chests/corn_maze");
    private static final ResourceLocation LOOT_CENTER = new ResourceLocation("hearthandharvest", "chests/corn_maze_center");
    private static final int MATURE_AGE = 7;
    private static final Direction[] HORIZONTALS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private final int width;
    private final int height;
    private final int originX;
    private final int originY;
    private final int originZ;
    private final long mazeSeed;

    public CornMazePiece(BlockPos origin, int width, int height, long mazeSeed) {
        super(HHWorldgen.CORN_MAZE_PIECE.get(), 0, BoundingBox.fromCorners(origin.offset(-2, 0, -2), origin.offset(width + 2, 10, height + 2)));
        this.width = width;
        this.height = height;
        this.originX = origin.getX();
        this.originY = origin.getY();
        this.originZ = origin.getZ();
        this.mazeSeed = mazeSeed;
    }

    public CornMazePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(HHWorldgen.CORN_MAZE_PIECE.get(), tag);
        this.width = tag.getInt("Width");
        this.height = tag.getInt("Height");
        this.originX = tag.getInt("OriginX");
        this.originY = tag.getInt("OriginY");
        this.originZ = tag.getInt("OriginZ");
        this.mazeSeed = tag.contains("MazeSeed") ? tag.getLong("MazeSeed") : BlockPos.asLong(originX, originY, originZ);
    }

    @Nullable
    static Block corn() {
        Block block = ForgeRegistries.BLOCKS.getValue(CORN_ID);
        return block == null || block == Blocks.AIR ? null : block;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("Width", width);
        tag.putInt("Height", height);
        tag.putInt("OriginX", originX);
        tag.putInt("OriginY", originY);
        tag.putInt("OriginZ", originZ);
        tag.putLong("MazeSeed", mazeSeed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        BlockPos origin = new BlockPos(originX, originY, originZ);
        boolean[][] maze = MazeGenerator.generate(width, height, RandomSource.create(mazeSeed));
        maze[1][height / 2] = false;
        maze[width - 2][height / 2] = false;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                BlockPos pos = origin.offset(x, 0, z);
                if (maze[x][z]) {
                    boolean interior = x > 1 && z > 1 && x < width - 2 && z < height - 2;
                    if (interior) {
                        placeWallVariant(level, box, pos, random);
                    } else {
                        placeCorn(level, box, pos);
                    }
                } else {
                    for (int y = 0; y < 3; y++) {
                        BlockPos airPos = pos.above(y);
                        if (box.isInside(airPos)) {
                            clearAir(level, airPos);
                        }
                    }
                    setFloor(level, box, pos.below(), pathFloor(random));
                }
            }
        }

        BlockPos entrance = origin.offset(0, 0, height / 2);
        BlockPos exit = origin.offset(width - 1, 0, height / 2);
        carveOpening(level, box, entrance);
        carveOpening(level, box, exit);
        maze[0][height / 2] = false;
        maze[width - 1][height / 2] = false;
        for (int i = 1; i <= 2; i++) {
            extendPath(level, box, entrance.relative(Direction.WEST, i));
            extendPath(level, box, exit.relative(Direction.EAST, i));
        }

        buildFringe(level, box, origin, random);
        placeDeadEndDecor(level, box, origin, random, maze);

        BlockPos center = origin.offset(width / 2, 0, height / 2);
        if (box.isInside(center)) {
            placeChest(level, center, Direction.NORTH, LOOT_CENTER, random);
        }
        buildGate(level, box, entrance);
        buildGate(level, box, exit);
    }

    private void buildFringe(WorldGenLevel level, BoundingBox box, BlockPos origin, RandomSource random) {
        for (int x = -2; x <= width + 1; x++) {
            for (int z = -2; z <= height + 1; z++) {
                if (x >= 0 && z >= 0 && x < width && z < height) {
                    continue;
                }
                BlockPos p = origin.offset(x, 0, z);
                boolean nearWestEntry = x < 0 && Math.abs(z - height / 2) <= 1;
                boolean nearEastEntry = x >= width && Math.abs(z - height / 2) <= 1;
                if (nearWestEntry || nearEastEntry) {
                    extendPath(level, box, p);
                    continue;
                }
                float f = random.nextFloat();
                setFloor(level, box, p.below(), f < 0.55F ? Blocks.GRASS_BLOCK.defaultBlockState()
                        : f < 0.8F ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.DIRT.defaultBlockState());
                if (!box.isInside(p)) {
                    continue;
                }
                float roll = random.nextFloat();
                if (roll < 0.3F) {
                    placeCorn(level, box, p);
                } else if (roll < 0.5F) {
                    level.setBlock(p, Blocks.GRASS.defaultBlockState(), 2);
                } else if (roll < 0.6F) {
                    level.setBlock(p, Blocks.TALL_GRASS.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), 2);
                    BlockPos upper = p.above();
                    if (box.isInside(upper)) {
                        level.setBlock(upper, Blocks.TALL_GRASS.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 2);
                    }
                } else if (roll < 0.65F) {
                    level.setBlock(p, Blocks.PUMPKIN.defaultBlockState(), 2);
                } else {
                    clearAir(level, p);
                }
            }
        }
    }

    private void placeDeadEndDecor(WorldGenLevel level, BoundingBox box, BlockPos origin, RandomSource random, boolean[][] maze) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                if (maze[x][z] || (x == 0 && z == height / 2) || (x == width - 1 && z == height / 2)) {
                    continue;
                }
                int open = 0;
                Direction face = null;
                if (x > 0 && !maze[x - 1][z]) {
                    open++;
                    face = Direction.WEST;
                }
                if (x < width - 1 && !maze[x + 1][z]) {
                    open++;
                    face = Direction.EAST;
                }
                if (z > 0 && !maze[x][z - 1]) {
                    open++;
                    face = Direction.NORTH;
                }
                if (z < height - 1 && !maze[x][z + 1]) {
                    open++;
                    face = Direction.SOUTH;
                }
                if (open != 1) {
                    continue;
                }
                BlockPos placePos = origin.offset(x, 0, z);
                if (!box.isInside(placePos)) {
                    continue;
                }
                float roll = random.nextFloat();
                if (roll < 0.3F) {
                    placeChest(level, placePos, face, LOOT, random);
                } else if (roll < 0.6F) {
                    level.setBlock(placePos, scarecrow(face), 2);
                } else if (roll < 0.9F) {
                    level.setBlock(placePos, Blocks.HAY_BLOCK.defaultBlockState(), 2);
                }
            }
        }
    }

    private static BlockState scarecrow(Direction face) {
        Block block = ForgeRegistries.BLOCKS.getValue(SCARECROW_ID);
        if (block == null || block == Blocks.AIR) {
            return Blocks.HAY_BLOCK.defaultBlockState();
        }
        BlockState state = block.defaultBlockState();
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.setValue(BlockStateProperties.HORIZONTAL_FACING, face) : state;
    }

    private static void placeChest(WorldGenLevel level, BlockPos pos, Direction facing, ResourceLocation table, RandomSource random) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(table, random.nextLong());
        }
    }

    private static void extendPath(WorldGenLevel level, BoundingBox box, BlockPos pos) {
        for (int y = 0; y < 3; y++) {
            BlockPos p = pos.above(y);
            if (box.isInside(p)) {
                clearAir(level, p);
            }
        }
        setFloor(level, box, pos.below(), Blocks.DIRT_PATH.defaultBlockState());
    }

    private static void carveOpening(WorldGenLevel level, BoundingBox box, BlockPos pos) {
        extendPath(level, box, pos);
    }

    private static BlockState pathFloor(RandomSource random) {
        float f = random.nextFloat();
        return f < 0.6F ? Blocks.DIRT_PATH.defaultBlockState() : f < 0.85F ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static void setFloor(WorldGenLevel level, BoundingBox box, BlockPos pos, BlockState floor) {
        if (box.isInside(pos)) {
            level.setBlock(pos, floor, 2);
        }
    }

    private void placeWallVariant(WorldGenLevel level, BoundingBox box, BlockPos pos, RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.08F) {
            setIfInside(level, box, pos, Blocks.HAY_BLOCK.defaultBlockState());
            setIfInside(level, box, pos.above(), Blocks.HAY_BLOCK.defaultBlockState());
            clearIfInside(level, box, pos.above(2));
            setFloor(level, box, pos.below(), Blocks.COARSE_DIRT.defaultBlockState());
        } else if (roll < 0.15F) {
            Direction facing = HORIZONTALS[random.nextInt(4)];
            Block pumpkin = roll < 0.12F ? Blocks.CARVED_PUMPKIN : Blocks.JACK_O_LANTERN;
            setIfInside(level, box, pos, pumpkin.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
            clearIfInside(level, box, pos.above());
            clearIfInside(level, box, pos.above(2));
            setFloor(level, box, pos.below(), Blocks.COARSE_DIRT.defaultBlockState());
        } else {
            placeCorn(level, box, pos);
        }
    }

    private static void setIfInside(WorldGenLevel level, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private static void clearIfInside(WorldGenLevel level, BoundingBox box, BlockPos pos) {
        if (box.isInside(pos)) {
            clearAir(level, pos);
        }
    }

    private static void clearAir(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    /** A mature two-block corn plant on moist farmland; the third block of a 1.4.1 stalk stays air. */
    private void placeCorn(WorldGenLevel level, BoundingBox box, BlockPos pos) {
        Block corn = corn();
        if (corn == null) {
            return;
        }
        setFloor(level, box, pos.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
        if (box.isInside(pos)) {
            level.setBlock(pos, cornState(corn, false), 2);
        }
        BlockPos top = pos.above();
        if (box.isInside(top)) {
            level.setBlock(top, cornState(corn, true), 2);
        }
        clearIfInside(level, box, pos.above(2));
    }

    private static BlockState cornState(Block corn, boolean upper) {
        BlockState state = corn.defaultBlockState();
        state = setInt(state, corn, "age", MATURE_AGE);
        return setBool(state, corn, "upper", upper);
    }

    private static BlockState setInt(BlockState state, Block block, String name, int value) {
        Property<?> property = block.getStateDefinition().getProperty(name);
        return property instanceof IntegerProperty ip ? state.setValue(ip, value) : state;
    }

    private static BlockState setBool(BlockState state, Block block, String name, boolean value) {
        Property<?> property = block.getStateDefinition().getProperty(name);
        return property instanceof BooleanProperty bp ? state.setValue(bp, value) : state;
    }

    private static void buildGate(WorldGenLevel level, BoundingBox box, BlockPos center) {
        BlockPos left = center.relative(Direction.NORTH);
        BlockPos right = center.relative(Direction.SOUTH);
        for (int y = 0; y <= 4; y++) {
            setIfInside(level, box, left.above(y), Blocks.OAK_FENCE.defaultBlockState());
            setIfInside(level, box, right.above(y), Blocks.OAK_FENCE.defaultBlockState());
        }
        BlockState log = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        for (BlockPos pos : new BlockPos[]{left.above(4), center.above(4), right.above(4)}) {
            setIfInside(level, box, pos, log);
        }
        setIfInside(level, box, center.above(3), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }
}
