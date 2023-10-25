package de.corey.challenges.challenges;

import de.corey.challenges.model.Challenge;
import de.corey.challenges.utils.Argument;
import de.corey.challenges.utils.Timer;
import de.corey.challenges.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DamageHealthChallenge extends Challenge {

    @Argument
    private boolean hidePercentage;

    public DamageHealthChallenge() {
        super("Schaden = Leben");

        timer = new Timer() {
            @Override
            public String toText(Player player) {
                int percentage = (int) (getDamageMultiplier(player.getHealth(), player.getHealthScale()) * 100);
                return super.toText(player) + (hidePercentage ? "" : " §6" + percentage + "%");
            }
        };
    }

    @EventHandler
    public void onPlayerDamageEvent(EntityDamageByEntityEvent event) {
        if (!isSelected()) {
            return;
        }
        if (event.getDamager() instanceof Player player) {
            event.setDamage(event.getDamage() * getDamageMultiplier(player.getHealth(), player.getHealthScale()));
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            event.setDamage(event.getDamage() * getDamageMultiplier(player.getHealth(), player.getHealthScale()));
        }
    }

    public double getDamageMultiplier(double health, double maxHealth) {
        double x = 2 - (1 / (maxHealth / 2) * (health));
        double y = Math.pow(x, 2 - x / 3) * 1.005;
        return Math.min(2, Math.max(0, y));
    }

    @Override
    public String info() {
        int maxHealth = 20;
        return super.info() + IntStream.range(1, maxHealth + 1)
                .mapToObj(i ->
                        "§c" + Utils.fillLeft(' ', 5, (i / 2d) + "♥") + " §r= §7" +
                        Utils.fillRight('0', 4,((int) (getDamageMultiplier(i, maxHealth) * 100) / 100d) + "") + "⚔")
                .collect(Collectors.joining("\n"));
    }
}
