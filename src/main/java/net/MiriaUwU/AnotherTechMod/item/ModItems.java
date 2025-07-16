package net.MiriaUwU.AnotherTechMod.item;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AnotherTechMod.MOD_ID);

public static final DeferredItem<Item> Tin = ITEMS.register("tin",
        () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> Brass = ITEMS.register("brass",
   () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> RawTin = ITEMS.register("rawtin",
            () -> new Item(new Item.Properties()));










    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

      }
}