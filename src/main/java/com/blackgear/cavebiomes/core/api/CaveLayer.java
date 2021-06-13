package com.blackgear.cavebiomes.core.api;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.LazyAreaLayerContext;
import net.minecraft.world.gen.area.IAreaFactory;
import net.minecraft.world.gen.area.LazyArea;
import net.minecraft.world.gen.layer.Layer;
import net.minecraft.world.gen.layer.ZoomLayer;
import net.minecraft.world.gen.layer.traits.IAreaTransformer0;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

//<>

public class CaveLayer {
    public static final List<RegistryKey<Biome>> caveBiomeKeys = new ArrayList<>();
    public static final List<Biome> caveBiomes = new ArrayList<>(); // For biome spawning
    public static final Set<Biome> caveBiomeSet = new HashSet<>(); // For quick checking if a biome is a cave biome

    public static Layer generateCaveLayers(Registry<Biome> biomeRegistry, long seed, int biomeSize) {
        // Clear set and list of cave biomes so we can get the correct biome instance from the world's dynamic registry.
        caveBiomes.clear();
        caveBiomeSet.clear();
        caveBiomes.addAll(caveBiomeKeys.stream().map(biomeRegistry::getValueForKey).collect(Collectors.toList()));
        caveBiomeSet.addAll(caveBiomes);

        LongFunction<IExtendedNoiseRandom<LazyArea>> context = salt -> new LazyAreaLayerContext(25, seed, salt);

        IAreaFactory<LazyArea> factory = new CaveBiomeLayer(biomeRegistry, caveBiomes).apply(context.apply(200L));

        for (int size = 0; size < biomeSize; size++) {
            if ((size + 2) % 3 != 0) {
                factory = ZoomLayer.NORMAL.apply(context.apply(2001L + size), factory);
            } else {
                factory = ZoomLayer.NORMAL.apply(context.apply(2000L + (size * 31L)), factory);
            }
        }

        return new Layer(factory);
    }

    public static class CaveBiomeLayer implements IAreaTransformer0 {
        public final Registry<Biome> dynamicBiomeRegistry;
        private final List<Biome> biomes;

        public CaveBiomeLayer(Registry<Biome> biomeRegistry, List<Biome> biomes) {
            this.dynamicBiomeRegistry = biomeRegistry;
            this.biomes = biomes;
        }

        @Override
        public int apply(INoiseRandom random, int x, int z) {
            return this.dynamicBiomeRegistry.getId(this.biomes.get(random.random(this.biomes.size())));
        }
    }
}