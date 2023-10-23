package de.corey.challenges.listeners;

import de.corey.challenges.Main;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EnderDragonEndListener implements Listener {

    @EventHandler
    public void onEnderDragonKilled(EntityDeathEvent event) {
        if (Main.getInstance().getSelectedChallenge() == null || !Main.getInstance().getSelectedChallenge().isEnderDragonEnd()) {
            return;
        }
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            Main.getInstance().getSelectedChallenge().stop();
        }
    }
}
