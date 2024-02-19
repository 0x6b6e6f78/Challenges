package de.corey.challenges.challenges;

import de.corey.challenges.model.Challenge;
import de.corey.challenges.model.nearby.MonsterRanking;
import de.corey.challenges.utils.Argument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class NearByChallenge extends Challenge {

    public static final Set<Material> EASY_ITEMS = new HashSet<>();

    public static final ItemStack ERROR_ITEM = new ItemStack(Material.BARRIER);

    static {
        EASY_ITEMS.add(Material.WOODEN_PICKAXE);
        EASY_ITEMS.add(Material.STONE_PICKAXE);
        EASY_ITEMS.add(Material.BUCKET);
        EASY_ITEMS.add(Material.ACACIA_PLANKS);
        EASY_ITEMS.add(Material.BAMBOO_PLANKS);
        EASY_ITEMS.add(Material.BIRCH_PLANKS);
        EASY_ITEMS.add(Material.JUNGLE_PLANKS);
        EASY_ITEMS.add(Material.CHERRY_PLANKS);
        EASY_ITEMS.add(Material.DARK_OAK_PLANKS);
        EASY_ITEMS.add(Material.MANGROVE_PLANKS);
        EASY_ITEMS.add(Material.OAK_PLANKS);
        EASY_ITEMS.add(Material.SPRUCE_PLANKS);
        EASY_ITEMS.add(Material.STICK);
        EASY_ITEMS.add(Material.CRAFTING_TABLE);

        ItemMeta errorItemMeta = ERROR_ITEM.getItemMeta();
        assert errorItemMeta != null;
        errorItemMeta.setDisplayName("§fDu kannst dieses Item nicht craften");
        ERROR_ITEM.setItemMeta(errorItemMeta);
    }

    @Argument
    public int nearByDistance = 6;

    private Map<Material, EntityType> specification;
    private Random random;

    public NearByChallenge() {
        super("NearBy");
    }

    @Override
    public void start() {
        specification = new HashMap<>();
        random = new Random();

        randomizeRecipes();
        super.start();
    }

    public void randomizeRecipes() {
        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
                specification.put(recipe.getResult().getType(), MonsterRanking.getRandomEntityType(random, EASY_ITEMS.contains(recipe.getResult().getType())));
            }
        });
    }

    @EventHandler
    public void onPrepareCrafting(PrepareItemCraftEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getRecipe() == null) {
            return;
        }
        if (!specification.containsKey(event.getRecipe().getResult().getType())) {
            return;
        }
        EntityType type = specification.get(event.getRecipe().getResult().getType());
        if (event.getViewers().stream().noneMatch(humanEntity -> humanEntity.getWorld().getEntities().stream()
                .filter(entity -> entity.getType() == type)
                .anyMatch(entity -> entity.getLocation().distance(humanEntity.getLocation()) <= nearByDistance))) {
            ItemStack errorItem = ERROR_ITEM.clone();
            ItemMeta errorItemMeta = errorItem.getItemMeta();
            assert errorItemMeta != null;
            errorItemMeta.setLore(Arrays.asList("Du benötigst das Entity " + type.name(), "in " + nearByDistance + " Blöcken Entfernung"));
            errorItem.setItemMeta(errorItemMeta);
            event.getInventory().setResult(errorItem);
        }
    }

    @EventHandler
    public void onClickError(InventoryClickEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) {
            return;
        }
        if (ERROR_ITEM.getType().equals(event.getCurrentItem().getType())) {
            event.setCancelled(true);
        }
    }
}
