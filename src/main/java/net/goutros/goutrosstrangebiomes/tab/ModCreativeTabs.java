package net.goutros.goutrosstrangebiomes.tab;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.item.ModItems;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Comparator;
import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goutrosstrangebiomes.main"))
                    .icon(() -> new ItemStack(ModItems.YARN_BALL.get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((params, output) -> {
                        ModItems.ITEMS.getEntries().stream()
                                .sorted(Comparator.comparing(e -> e.getId().getPath())) // or custom tag order
                                .map(Supplier::get)
                                .map(ItemStack::new)
                                .forEach(output::accept);

                        ModBlocks.ITEMS.getEntries().stream()
                                .sorted(Comparator.comparing(e -> e.getId().getPath()))
                                .map(Supplier::get)
                                .map(ItemStack::new)
                                .forEach(output::accept);
                    })
                    .build());
}
