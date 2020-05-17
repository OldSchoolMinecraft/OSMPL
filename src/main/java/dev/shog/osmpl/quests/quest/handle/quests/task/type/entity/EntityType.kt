package dev.shog.osmpl.quests.quest.handle.quests.task.type.entity

import org.bukkit.craftbukkit.entity.*

enum class EntityType(val entityClass: Class<*>, val entityName: String) {
    ZOMBIE(CraftZombie::class.java, "Zombie"),
    SKELETON(CraftSkeleton::class.java, "Skeleton"),
    SPIDER(CraftSpider::class.java, "Spider"),
    WOLF(CraftWolf::class.java, "Wolf"),
    SHEEP(CraftSheep::class.java, "Sheep"),
    COW(CraftCow::class.java, "Cow"),
    CHICKEN(CraftChicken::class.java, "Chicken"),
    CREEPER(CraftCreeper::class.java, "Creeper"),
    GHAST(CraftGhast::class.java, "Ghast"),
    PIG(CraftPig::class.java, "Pig"),
    MANLY_PIG(CraftPigZombie::class.java, "Big Pig"),
    SQUID(CraftSquid::class.java, "Squid"),
}