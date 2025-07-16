package net.MiriaUwU.AnotherTechMod.init;

import net.MiriaUwU.AnotherTechMod.block.ModBlocks;
import net.MiriaUwU.AnotherTechMod.fluid.OilFluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.material.Fluid;

public class ModFlammables {
    public static void registerflammables() {
        FireBlock fireBlock = (FireBlock) Blocks.FIRE;


        fireBlock.setFlammable(OilFluids.OIL_BLOCK.get(), 100,60);
    }
}
