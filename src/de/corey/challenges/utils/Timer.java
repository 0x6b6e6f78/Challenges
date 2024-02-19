package de.corey.challenges.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Timer {

    private long start, paused, pauseSum;

    private Thread thread;

    public void start() {
        start = System.currentTimeMillis();
        thread = new Thread(() -> Bukkit.getOnlinePlayers().forEach(player -> {
            try {
                while (true) {
                    String text;
                    if (paused == 0) {
                        text = toText(player);
                    } else {
                        text = "§8Timer ist pausiert";
                    }
//                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        thread.start();
    }

    public void pause() {
        paused = System.currentTimeMillis();
    }

    public void resume() {
        pauseSum += System.currentTimeMillis() - paused;
        paused = 0;
    }

    public void stop() {
        thread.stop();
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
