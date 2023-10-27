package de.corey.challenges.challenges;

import de.corey.challenges.Main;
import de.corey.challenges.model.Challenge;
import de.corey.challenges.model.lists.PlayerStringList;
import de.corey.challenges.model.superhot.LastPlayerState;
import de.corey.challenges.model.superhot.MovingState;
import de.corey.challenges.utils.Argument;
import de.corey.challenges.utils.Timer;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.reflect.Field;
import java.util.*;

public class SuperHotChallenge extends Challenge {

    @Argument
    private boolean showTps;

    @Argument(label = "bestimmer")
    private PlayerStringList determinerList = new PlayerStringList(1);

    //

    private final List<LastPlayerState> determiners = new ArrayList<>();

    private boolean running;

    private Field nextTickTimeField;
    private MinecraftServer server;

    private long nextTickTime, currentTickTime;
    private MovingState movingState = MovingState.STAYING;
    private long millisecondPerTick = 1000 / movingState.tps, lastMillisecondPerTick;

    private long tickCount, now;
    private double currentTps;

    public SuperHotChallenge() {
        super("Super Hot");

        timer = new Timer() {
            @Override
            public String toText(Player player) {
                return super.toText(player) + (showTps ? " §6" + currentTps + "tps" : "");
            }
        };
    }

    @Override
    public void start() {
        super.start();
        running = true;

        if (!determineDeterminers()) {
            Bukkit.broadcastMessage("§cEs ist kein Spieler auf dem Server!");
            return;
        }

        initReflection();
        startLoopThread();
        calculateAndSetTps();
    }

    public void initReflection() {
        try {
            nextTickTimeField = MinecraftServer.class.getDeclaredField("ah");
            nextTickTimeField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        server = ((CraftServer) Main.getInstance().getServer()).getServer();
    }

    public boolean determineDeterminers() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            return false;
        }

        determinerList.tStream().findAny()
                .ifPresentOrElse(player -> determiners.add(new LastPlayerState(player)), () ->
                        Bukkit.getOnlinePlayers().stream()
                                .map(LastPlayerState::new)
                                .forEach(determiners::add));
        return true;
    }

    public void startLoopThread() {
        Thread thread = new Thread(() -> {
            try {
                while (isSelected() && running) {
                    if (!isActive()) {
                        Thread.sleep(1000);
                        continue;
                    }
                    determineMovingState();
                    Thread.sleep(5);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    @Override
    public void stop() {
        running = false;
        super.stop();
    }
    
    //

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getTo() == null) {
            return;
        }
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return;
        }
        if (event.getFrom().distance(event.getTo()) < .01) {
            return;
        }

        double distance = event.getFrom().distance(event.getTo()) * 2;
        double fallDistance = (event.getFrom().getY() - event.getTo().getY()) * 2;
        determiners.stream()
                .filter(lastPlayerState -> lastPlayerState.getPlayer() == event.getPlayer())
                .forEach(lastPlayerState -> {
                    lastPlayerState.setMovedDistance(distance);
                    lastPlayerState.setFallDistance(fallDistance + lastPlayerState.getFallDistance());
                });
    }

    //

    public void calculateAndSetTps() {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (showTps) {
                long now = System.currentTimeMillis();
                tickCount++;
                if (this.now + 1000 < now) {
                    currentTps = Math.round((tickCount / ((now - this.now) / 1000d)) * 1000) / 1000d;
                    this.now = now;
                    tickCount = 0;
                }
            }

            try {
                currentTickTime = nextTickTimeField.getLong(server) - 50;
                nextTickTime = currentTickTime + (int) ((millisecondPerTick + millisecondPerTick + lastMillisecondPerTick) / 3);
                nextTickTimeField.setLong(server, nextTickTime);
                lastMillisecondPerTick = millisecondPerTick;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (isSelected()) {
                calculateAndSetTps();
            }
        }, 1);
    }

    public void determineMovingState() throws Exception {
        MovingState highestMovingState = MovingState.STAYING;
        for (LastPlayerState lastPlayerState : determiners) {
            if (lastPlayerState.getMovedDistance() == 0) {
                continue;
            }
            lastPlayerState.setMovedDistance(Math.max(lastPlayerState.getMovedDistance() - .007, 0));
            lastPlayerState.setFallDistance(Math.max(lastPlayerState.getFallDistance() - .2, 0));
            if (lastPlayerState.getLastFallDistance() == lastPlayerState.getFallDistance()) {
                lastPlayerState.setFallDistance(0);
            }
            lastPlayerState.setLastFallDistance(lastPlayerState.getFallDistance());

            MovingState currentMovingState = getMovingState(lastPlayerState.getPlayer(), lastPlayerState.getFallDistance());
            if (currentMovingState.tps > highestMovingState.tps) {
                highestMovingState = currentMovingState;
            }
        }
        setMovingState(highestMovingState);
    }

    //

    public void setMovingState(MovingState movingState) throws Exception {
        if (this.movingState != movingState) {
            long tpsOffset = this.movingState.tps - movingState.tps;
            long sptOffset = tpsOffset == 0 ? 0 : 1000 / tpsOffset;
            this.movingState = movingState;
            this.millisecondPerTick = 1000 / movingState.tps;
            this.nextTickTimeField.set(server, currentTickTime + sptOffset);
        }
    }

    public MovingState getMovingState(Player player, double fallDistance) {
        if (fallDistance > .4) {
            return MovingState.FALLING;
        } else if (player.isSneaking()) {
            return MovingState.SNEAKING;
        } else if (player.isSprinting()) {
            return MovingState.SPRINTING;
        }
        return MovingState.WALKING;
    }
}
