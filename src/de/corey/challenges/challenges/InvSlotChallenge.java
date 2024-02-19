package de.corey.challenges.challenges;

import de.corey.challenges.Main;
import de.corey.challenges.model.Challenge;
import de.corey.challenges.utils.Argument;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.IntStream;

public class InvSlotChallenge extends Challenge {

    public static final ItemStack ERROR_ITEM = new ItemStack(Material.BLACK_DYE);

    static {
        ItemMeta errorItemMeta = ERROR_ITEM.getItemMeta();
        assert errorItemMeta != null;
        errorItemMeta.setDisplayName("§f");
        ERROR_ITEM.setItemMeta(errorItemMeta);
    }

    @Argument
    private int slots = 5;

    public InvSlotChallenge() {
        super("Inv Slot");
    }

    @Override
    public void start() {
        super.start();

        Bukkit.getOnlinePlayers().stream()
                .map(Player::getInventory)
                .forEach(inventory ->
                        IntStream.range(slots, 4 * 9)
                                .forEach(i -> inventory.setItem(i, ERROR_ITEM)));
    }

    @Override
    public void stop() {
        super.stop();
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getInventory)
                .forEach(inventory ->
                        IntStream.range(slots, inventory.getSize())
                                .forEach(i -> {
                                    if (ERROR_ITEM.isSimilar(inventory.getItem(i))) {
                                        inventory.setItem(i, null);
                                    }
                                }));
    }

    @EventHandler
    public void onClickError(InventoryClickEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (ERROR_ITEM.isSimilar(event.getCurrentItem())
                || (event.getHotbarButton() != -1 && ERROR_ITEM.isSimilar(event.getClickedInventory().getItem(event.getHotbarButton())))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        if (!isActive()) {
            return;
        }
        if (ERROR_ITEM.isSimilar(event.getMainHandItem()) || ERROR_ITEM.isSimilar(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getPlayer().getInventory().getItemInMainHand().getType().isBlock()) {
            if (ERROR_ITEM.isSimilar(event.getPlayer().getInventory().getItemInMainHand())) {
                event.setCancelled(true);
            }
        } else {
            if (ERROR_ITEM.isSimilar(event.getPlayer().getInventory().getItemInOffHand())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSlotSwap(PlayerItemHeldEvent event) {
        if (!isActive()) {
            return;
        }
        ItemStack newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (newItem == null) {
            return;
        }
        if (newItem.getType().isBlock()) {
            if (ERROR_ITEM.isSimilar(newItem)) {
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
            } else {
                event.getPlayer().setGameMode(GameMode.SURVIVAL);
            }
        } else {
            if (ERROR_ITEM.isSimilar(event.getPlayer().getInventory().getItemInOffHand())) {
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
            } else {
                event.getPlayer().setGameMode(GameMode.SURVIVAL);
            }
        }
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> event.getPlayer().updateInventory(), 1);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (ERROR_ITEM.isSimilar(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }
}
