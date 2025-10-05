package net.MiriaUwU.AnotherTechMod.fluid;

import java.util.ArrayList;
import java.util.List;

public class SmeltableFluidsRegistry {

    // List of all smeltable input fluids
    public static final List<String> INPUT_FLUIDS = List.of(
            "molten_copper",
            "molten_tin",
            "molten_iron"


            // Add more here -
    );

    // Output/alloy fluids
    public static final List<String> OUTPUT_FLUIDS = List.of(
            "molten_bronze"

            // Add more alloys here
    );

    /**
     * Get all registered fluid names (input + output)
     */
    public static List<String> getAllFluidNames() {
        List<String> all = new ArrayList<>();
        all.addAll(INPUT_FLUIDS);
        all.addAll(OUTPUT_FLUIDS);
        return all;
    }
}
