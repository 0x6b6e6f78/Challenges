package de.corey.challenges.model.nearby;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@Getter
public enum MonsterRanking {

    OVERWORLD_BAD1(World.Environment.NORMAL, -1, EntityType.SPIDER, EntityType.ZOMBIE,
            EntityType.SKELETON, EntityType.CREEPER, EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY),
    OVERWORLD_BAD2(World.Environment.NORMAL, -2, EntityType.EVOKER, EntityType.GUARDIAN, EntityType.PILLAGER,
            EntityType.PHANTOM, EntityType.RAVAGER, EntityType.SILVERFISH, EntityType.ZOGLIN, EntityType.ELDER_GUARDIAN,
            EntityType.ENDERMITE, EntityType.VEX, EntityType.VINDICATOR, EntityType.SLIME, EntityType.ZOMBIE_VILLAGER,
            EntityType.CAVE_SPIDER, EntityType.WITCH, EntityType.ENDERMAN),
    OVERWORLD_GOOD1(World.Environment.NORMAL, 1, EntityType.BAT, EntityType.BEE, EntityType.CAT,
            EntityType.CHICKEN, EntityType.COW, EntityType.DOLPHIN, EntityType.DONKEY, EntityType.TROPICAL_FISH,
            EntityType.HORSE, EntityType.MULE, EntityType.PANDA, EntityType.PIG, EntityType.RABBIT, EntityType.SHEEP,
            EntityType.SNOWMAN, EntityType.SQUID, EntityType.VILLAGER, EntityType.BOAT, EntityType.CHEST_BOAT,
            EntityType.ARMOR_STAND, EntityType.CAMEL, EntityType.COD, EntityType.GLOW_SQUID, EntityType.IRON_GOLEM,
            EntityType.MINECART, EntityType.MINECART_CHEST, EntityType.MINECART_FURNACE, EntityType.MINECART_HOPPER,
            EntityType.MINECART_TNT, EntityType.PAINTING, EntityType.SALMON, EntityType.TRADER_LLAMA, EntityType.WOLF),
    OVERWORLD_GOOD2(World.Environment.NORMAL, 2, EntityType.AXOLOTL, EntityType.PUFFERFISH, EntityType.GOAT,
            EntityType.LLAMA, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PARROT, EntityType.POLAR_BEAR,
            EntityType.TURTLE, EntityType.WANDERING_TRADER, EntityType.FOX, EntityType.FROG, EntityType.SNIFFER),
    NETHER1(World.Environment.NETHER, -1, EntityType.SKELETON, EntityType.MAGMA_CUBE, EntityType.PIGLIN,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.ENDERMAN, EntityType.GHAST, EntityType.HOGLIN, EntityType.STRIDER),
    NETHER2(World.Environment.NETHER, -2, EntityType.BLAZE, EntityType.ZOGLIN, EntityType.WITHER_SKELETON);

    private final World.Environment environment;
    private final int goodness;
    private final EntityType[] entityTypes;

    MonsterRanking(World.Environment environment, int goodness, EntityType... entityTypes) {
        this.environment = environment;
        this.goodness = goodness;
        this.entityTypes = entityTypes;
    }

    public boolean isBegin() {
        return this == OVERWORLD_BAD1 || this == OVERWORLD_GOOD1;
    }

    public static MonsterRanking getRandom(Random random, boolean easy) {
        Stream<MonsterRanking> stream = Arrays.stream(values());
        if (easy) {
            stream = stream.filter(MonsterRanking::isBegin);
        }
        List<MonsterRanking> list = stream.toList();
        return list.get(random.nextInt(list.size() - 1));
    }

    public static EntityType getRandomEntityType(Random random, boolean easy) {
        List<EntityType> list = Arrays.asList(getRandom(random, easy).entityTypes);
        return list.get(random.nextInt(list.size() - 1));
    }
}
