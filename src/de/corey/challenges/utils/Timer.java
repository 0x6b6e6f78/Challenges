package de.corey.challenges.utils;

import de.corey.challenges.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Timer {

    private long start, paused, pauseSum;

    private int id;

    public void start() {
        start = System.currentTimeMillis();
        id = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
            String text;
            if (paused == 0) {
                text = toText(player);
            } else {
                text = "§8Timer ist pausiert";
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
        }), 10, 10);
    }

    public void pause() {
        paused = System.currentTimeMillis();
    }

    public void resume() {
        pauseSum += System.currentTimeMillis() - paused;
        paused = 0;
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(id);
    }

    public String makePretty(long passedTime) {
        long l = passedTime - pauseSum;
        int h = 0, m = 0, s = 0;
        int _s = 1000, _m = _s * 60, _h = _m * 60;
        while (l >= _h) {
            h++;
            l -= _h;
        }
        while (l >= _m) {
            m++;
            l -= _m;
        }
        while (l >= _s) {
            s++;
            l -= _s;
        }
        if (h > 0) {
            return h + "h " + m + "m " + s + "s";
        }
        if (m > 0) {
            return m + "m " + s + "s";
        }
        return s + "s";
    }

    public String text() {
        return makePretty(System.currentTimeMillis() - start);
    }

    public String toText(Player player) {
        return "§7" + text();
    }

    public boolean isPaused() {
        return paused != 0;
    }
}
