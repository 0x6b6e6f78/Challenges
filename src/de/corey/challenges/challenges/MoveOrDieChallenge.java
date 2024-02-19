package de.corey.challenges.challenges;

import de.corey.challenges.Main;
import de.corey.challenges.model.Challenge;
import de.corey.challenges.model.moveordie.LastMove;
import de.corey.challenges.utils.Argument;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;

public class MoveOrDieChallenge extends Challenge {

    @Argument(hidden = true)
    private int time = 3260;

    private final List<LastMove> lastMoves = new ArrayList<>();

    private int schedulerId;

    public MoveOrDieChallenge() {
        super("Move Or Die");
    }

    public void start() {
        super.start();
        Bukkit.getOnlinePlayers().forEach(player -> this.lastMoves.add(new LastMove(player)));
        this.schedulerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            if (!isActive()) {
                return;
            }
            for (LastMove lastMove : this.lastMoves) {
                BarColor color;
                lastMove.update();
                int left = time - lastMove.getStandingMillis();
                double progress = Math.min(Math.max(1.0D / this.time * left, 0.0D), 1.0D);
                progress *= progress;
                if (progress > 0.85D) {
                    color = BarColor.GREEN;
                } else {
                    if (progress > 0.5D) {
                        color = BarColor.YELLOW;
                    } else {
                        color = BarColor.RED;
                    }
                    double pitch = Math.min(Math.max(1.175D * progress, 0), 1);
                    pitch = pitch * pitch * 1.8D;
                    lastMove.getPlayer().playSound(lastMove.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, Math.min(1.0F, 1.0F - (float)pitch + 0.3F), (float)pitch);
                }
                if (lastMove.getBossBar().getColor() != color) {
                    lastMove.getBossBar().setColor(color);
                }
                lastMove.getBossBar().setProgress(progress);
                if (progress == 0) {
                    lastMove.getPlayer().damage(9);
                }
            }
        }, 1L, 1L);
    }

    public void stop() {
        super.stop();
        this.lastMoves.forEach(lastMove -> lastMove.getBossBar().removePlayer(lastMove.getPlayer()));
        Bukkit.getScheduler().cancelTask(this.schedulerId);
    }
}
