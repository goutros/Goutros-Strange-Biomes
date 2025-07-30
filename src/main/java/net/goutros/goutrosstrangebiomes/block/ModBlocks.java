package net.goutros.goutrosstrangebiomes.block;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, GoutrosStrangeBiomes.MOD_ID);

    public static final Supplier<Block> PILLOW_GRASS_BLOCK =
            BLOCKS.register("pillow_grass_block", () ->
                    new PillowGrassBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GRASS)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.6F)
                            .randomTicks()
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> YELLOW_PILLOW_GRASS_BLOCK =
            BLOCKS.register("yellow_pillow_grass_block", () ->
                    new PillowGrassBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.6F)
                            .randomTicks()
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> BUTTONS =
            BLOCKS.register("buttons", () ->
                    new ButtonsBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .noCollission()
                            .strength(0.2F)
                            .sound(SoundType.CANDLE)
                    )
            );

    public static final Supplier<Block> WHITE_PILLOW =
            BLOCKS.register("white_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.SNOW)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> LIGHT_GRAY_PILLOW =
            BLOCKS.register("light_gray_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_GRAY)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> GRAY_PILLOW =
            BLOCKS.register("gray_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> BLACK_PILLOW =
            BLOCKS.register("black_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> BROWN_PILLOW =
            BLOCKS.register("brown_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BROWN)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> RED_PILLOW =
            BLOCKS.register("red_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> ORANGE_PILLOW =
            BLOCKS.register("orange_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> YELLOW_PILLOW =
            BLOCKS.register("yellow_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> LIME_PILLOW =
            BLOCKS.register("lime_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_GREEN)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> GREEN_PILLOW =
            BLOCKS.register("green_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> CYAN_PILLOW =
            BLOCKS.register("cyan_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_CYAN)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> LIGHT_BLUE_PILLOW =
            BLOCKS.register("light_blue_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> BLUE_PILLOW =
            BLOCKS.register("blue_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> PURPLE_PILLOW =
            BLOCKS.register("purple_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> MAGENTA_PILLOW =
            BLOCKS.register("magenta_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_MAGENTA)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> PINK_PILLOW =
            BLOCKS.register("pink_pillow", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Item> PILLOW_GRASS_BLOCK_ITEM =
            ITEMS.register("pillow_grass_block", () ->
                    new BlockItem(PILLOW_GRASS_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> YELLOW_PILLOW_GRASS_ITEM = ITEMS.register("yellow_pillow_grass_block",
            () -> new BlockItem(YELLOW_PILLOW_GRASS_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> BUTTONS_ITEM =
            ITEMS.register("buttons", () ->
                    new BlockItem(BUTTONS.get(), new Item.Properties()));

    public static final Supplier<Item> WHITE_PILLOW_ITEM =
            ITEMS.register("white_pillow", () ->
                    new BlockItem(WHITE_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> LIGHT_GRAY_PILLOW_ITEM =
            ITEMS.register("light_gray_pillow", () ->
                    new BlockItem(LIGHT_GRAY_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> GRAY_PILLOW_ITEM =
            ITEMS.register("gray_pillow", () ->
                    new BlockItem(GRAY_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> BLACK_PILLOW_ITEM =
            ITEMS.register("black_pillow", () ->
                    new BlockItem(BLACK_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> BROWN_PILLOW_ITEM =
            ITEMS.register("brown_pillow", () ->
                    new BlockItem(BROWN_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> RED_PILLOW_ITEM =
            ITEMS.register("red_pillow", () ->
                    new BlockItem(RED_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> ORANGE_PILLOW_ITEM =
            ITEMS.register("orange_pillow", () ->
                    new BlockItem(ORANGE_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> YELLOW_PILLOW_ITEM =
            ITEMS.register("yellow_pillow", () ->
                    new BlockItem(YELLOW_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> LIME_PILLOW_ITEM =
            ITEMS.register("lime_pillow", () ->
                    new BlockItem(LIME_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> GREEN_PILLOW_ITEM =
            ITEMS.register("green_pillow", () ->
                    new BlockItem(GREEN_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> CYAN_PILLOW_ITEM =
            ITEMS.register("cyan_pillow", () ->
                    new BlockItem(CYAN_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> LIGHT_BLUE_PILLOW_ITEM =
            ITEMS.register("light_blue_pillow", () ->
                    new BlockItem(LIGHT_BLUE_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> BLUE_PILLOW_ITEM =
            ITEMS.register("blue_pillow", () ->
                    new BlockItem(BLUE_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> PURPLE_PILLOW_ITEM =
            ITEMS.register("purple_pillow", () ->
                    new BlockItem(PURPLE_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> MAGENTA_PILLOW_ITEM =
            ITEMS.register("magenta_pillow", () ->
                    new BlockItem(MAGENTA_PILLOW.get(), new Item.Properties()));

    public static final Supplier<Item> PINK_PILLOW_ITEM =
            ITEMS.register("pink_pillow", () ->
                    new BlockItem(PINK_PILLOW.get(), new Item.Properties()));

    public static void makeFlammable(Block block, int burnChance, int spreadChance) {
        FireBlock fireBlock = (FireBlock) Blocks.FIRE;
        fireBlock.setFlammable(block, burnChance, spreadChance);
    }

    public static void registerFlammableBlocks() {
        makeFlammable(PILLOW_GRASS_BLOCK.get(), 30, 60);
        makeFlammable(YELLOW_PILLOW_GRASS_BLOCK.get(), 30, 60);

        makeFlammable(WHITE_PILLOW.get(), 30, 60);
        makeFlammable(LIGHT_GRAY_PILLOW.get(), 30, 60);
        makeFlammable(GRAY_PILLOW.get(), 30, 60);
        makeFlammable(BLACK_PILLOW.get(), 30, 60);
        makeFlammable(BROWN_PILLOW.get(), 30, 60);
        makeFlammable(RED_PILLOW.get(), 30, 60);
        makeFlammable(ORANGE_PILLOW.get(), 30, 60);
        makeFlammable(YELLOW_PILLOW.get(), 30, 60);
        makeFlammable(LIME_PILLOW.get(), 30, 60);
        makeFlammable(GREEN_PILLOW.get(), 30, 60);
        makeFlammable(CYAN_PILLOW.get(), 30, 60);
        makeFlammable(LIGHT_BLUE_PILLOW.get(), 30, 60);
        makeFlammable(BLUE_PILLOW.get(), 30, 60);
        makeFlammable(PURPLE_PILLOW.get(), 30, 60);
        makeFlammable(MAGENTA_PILLOW.get(), 30, 60);
        makeFlammable(PINK_PILLOW.get(), 30, 60);
    }
}