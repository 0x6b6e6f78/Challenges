package de.corey.challenges.model.moveordie;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

@Data
public class LastMove {

    private Player player;
    private long millis;
    private Location location;
    private BossBar bossBar;

    public LastMove(Player player) {
        this.player = player;
        this.bossBar = Bukkit.createBossBar("Move Or Die", BarColor.GREEN, BarStyle.SOLID, new org.bukkit.boss.BarFlag[0]);
        this.bossBar.addPlayer(player);
        update();
    }

    public void update() {
        if (this.location != null && this.player.getLocation().distance(this.location) < 0.1D)
            return;
        this.millis = System.currentTimeMillis();
        this.location = this.player.getLocation();
    }

    public int getStandingMillis() {
        return (int)(System.currentTimeMillis() - this.millis);
    }
}
