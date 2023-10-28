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
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

public class SuperHotChallenge extends Challenge {

    @Argument
    private boolean showTps;

    @Argument
    private boolean debug;

    @Argument(label = "bestimmer")
    private PlayerStringList determinerList = new PlayerStringList(1);

    //

    private final List<LastPlayerState> determiners = new ArrayList<>();
    private final Map<World, Integer> randomTickSpeeds = new HashMap<>();

    private boolean running;

    private Field nextTickTimeField, overLoadTimeField;
    private MinecraftServer server;

    private long nextTickTime, currentTickTime;
    private MovingState movingState = MovingState.STAYING, lastMovingState = movingState;
    private long millisecondPerTick = 1000 / movingState.tps, lastMillisecondPerTick;

    private long tickCount, now;
    private double currentTps;

    public SuperHotChallenge() {
        super("Super Hot");

        timer = new Timer() {
            @Override
            public String toText(Player player) {
                return super.toText(player) + (showTps || debug ? " §6" + currentTps + "tps" : "")
                        + (debug ? " §7" + movingState : "");
            }
        };
    }

    @Override
    public void start() {
        super.start();
        running = true;

        if (!initDeterminers()) {
            Bukkit.broadcastMessage("§cEs ist kein Spieler auf dem Server!");
            return;
        }

        Bukkit.getWorlds().forEach(world -> randomTickSpeeds.put(world, world.getGameRuleValue(GameRule.RANDOM_TICK_SPEED)));

        initReflection();
        startLoopThread();
        calculateAndSetTps();
    }

    public void initReflection() {
        try {
            nextTickTimeField = MinecraftServer.class.getDeclaredField("ah");
            nextTickTimeField.setAccessible(true);
            overLoadTimeField = MinecraftServer.class.getDeclaredField("ae");
            overLoadTimeField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        server = ((CraftServer) Main.getInstance().getServer()).getServer();
    }

    public boolean initDeterminers() {
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
                    Thread.sleep(50);
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
        randomTickSpeeds.forEach((world, randomTickSpeed) -> world.setGameRule(GameRule.RANDOM_TICK_SPEED, randomTickSpeed));
        super.stop();
    }

    //

    public void calculateAndSetTps() {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (showTps || debug) {
                long now = System.currentTimeMillis();
                tickCount++;
                if (this.now + 1000 < now) {
                    currentTps = Math.round((tickCount / ((now - this.now) / 1000d)) * 1000) / 1000d;
                    this.now = now;
                    tickCount = 0;
                }
            }

            if (isActive()) {
                try {
                    currentTickTime = nextTickTimeField.getLong(server) - 50;
                    nextTickTime = currentTickTime + (int) ((millisecondPerTick + millisecondPerTick + lastMillisecondPerTick) / 3);
                    nextTickTimeField.setLong(server, nextTickTime);
                    overLoadTimeField.set(server, nextTickTime);
                    lastMillisecondPerTick = millisecondPerTick;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (isSelected()) {
                calculateAndSetTps();
            }
        }, 1);
    }

    public void determineMovingState() throws Exception {
        MovingState highest = MovingState.STAYING;
        for (LastPlayerState lastPlayerState : determiners) {
            Location location = lastPlayerState.getPlayer().getLocation();
            if (lastPlayerState.getLocation() != null) {
                MovingState movingState = toMovingState(lastPlayerState);
                if (movingState.tps > highest.tps) {
                    highest = movingState;
                }
            }
            lastPlayerState.setLocation(location);
        }
        setMovingState(highest);
    }

    public MovingState toMovingState(LastPlayerState lastPlayerState) {
        Location from = lastPlayerState.getLocation();
        Player player = lastPlayerState.getPlayer();
        Location to = player.getLocation();
        double movingXZ = getDistanceXZ(from, to);
        double movingY = to.getY() - from.getY();
        double startFallY = lastPlayerState.getStartFallY();
        if (movingY == 0) {
            lastPlayerState.setStartFallY(0);
        } else {
            lastPlayerState.setStartFallY(startFallY + movingY);
        }
        return getMovingState(startFallY, movingXZ, player);
    }

    private MovingState getMovingState(double startFallY, double movingXZ, Player player) {
        MovingState movingState = MovingState.STAYING;
        if (startFallY < -2) {
            movingState = movingState.getHighest(MovingState.FALLING);
        } else if (startFallY > 0) {
            movingState = movingState.getHighest(MovingState.JUMPING);
        }
        if (movingXZ > 0) {
            if (player.isSwimming()) {
                movingState = movingState.getHighest(MovingState.SWIMMING);
            } else if (player.isSneaking()) {
                movingState = movingState.getHighest(MovingState.SNEAKING);
            } else if (player.isSprinting()) {
                movingState = movingState.getHighest(MovingState.SPRINTING);
            } else {
                movingState = movingState.getHighest(MovingState.WALKING);
            }
        }
        return movingState;
    }

    //

    public void setMovingState(MovingState movingState) throws Exception {
        if (this.movingState == movingState && movingState != this.lastMovingState) {
            long tpsOffset = this.lastMovingState.tps - movingState.tps;
            long sptOffset = tpsOffset == 0 ? 0 : 1000 / tpsOffset;
            this.millisecondPerTick = 1000 / movingState.tps;
            this.nextTickTimeField.setLong(server, currentTickTime + sptOffset);
            Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.RANDOM_TICK_SPEED, movingState.rts));
            this.lastMovingState = movingState;
        }
        if (this.movingState != movingState) {
            this.movingState = movingState;
        }
    }

    public double getDistanceXZ(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
