package de.corey.challenges.challenges;

import de.corey.challenges.Main;
import de.corey.challenges.model.Challenge;
import de.corey.challenges.model.lists.PlayerStringList;
import de.corey.challenges.utils.Argument;
import de.corey.challenges.utils.Tuple;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.reflect.Field;
import java.util.HashMap;

public class SuperHotChallenge extends Challenge {

    public static final int STILL_TICKS = 1;
    public static final int WALK_TICKS = 20;
    public static final int SPRINT_TICKS = 40;

    @Argument
    private int multiplier = 1;

    @Argument(label = "bestimmer")
    private PlayerStringList determinerList = new PlayerStringList(1);

    private Player determiner;

    private int schedulerId = -1;

    private Thread thread;

    private Map<Player, Tuple<Location, Long>> lastPlayerState = new HashMap<>();
    private Location lastLocation;
    private long nextLocationTime;

    private int moving;
    private boolean checkMove;

    public SuperHotChallenge() {
        super("Super Hot");
    }

    @Override
    public void start() {
        super.start();

        if ((determiner = determinerList.tStream().findAny().orElse(Bukkit.getOnlinePlayers().stream().findAny().orElse(null))) == null) {
            Bukkit.broadcastMessage("§cEs ist kein Spieler auf dem Server!");
            return;
        }

        (thread = new Thread(() -> {
            while (isSelected()) {
                asyncTick();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        })).start();

        schedulerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            System.out.println("tick");
        }, 1, 1);
    }

    @Override
    public void stop() {
        super.stop();
        Bukkit.getScheduler().cancelTask(schedulerId);
        schedulerId = -1;
        onDisable();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isSelected()) {
            return;
        }
        if (event.getTo() == null) {
            return;
        }
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return;
        }
        if (event.getFrom().distance(event.getTo()) < .1) {
            return;
        }
        moving = 0;
        double fallingSpeed;
        if ((fallingSpeed = Math.max(0, event.getFrom().getY() - event.getTo().getY())) > 2.8) {

        } else {
            if (event.getPlayer().isSprinting()) {
                //setTicks(-200);
            } else if (!event.getPlayer().isSneaking()) {
                //setTicks(-20);
            }
        }
    }

    long newNextTickTime, l;
    public void addNextTickTime(long millis) {
        try {
            Field nextTickTimeField = MinecraftServer.class.getDeclaredField("ah");
            nextTickTimeField.setAccessible(true);
            MinecraftServer server = ((CraftServer) Main.getInstance().getServer()).getServer();

            long serverNextTickTime = (long) nextTickTimeField.get(server);
            if (serverNextTickTime > newNextTickTime) {
                if (System.currentTimeMillis() > newNextTickTime) {
                    newNextTickTime = millis;
                }
                nextTickTimeField.set(server, newNextTickTime);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        if (thread != null) {
            thread.stop();
        }
    }

    int allowedMoves = 10;
    public void asyncTick() {
        if (moving > allowedMoves) {
            return;
        }
        moving++;
        Location currentLocation = determiner.getLocation(); // TODO: zu hashmap
        long currentLocationTime = System.currentTimeMillis();
        if (nextLocationTime < currentLocationTime) {
            nextLocationTime = currentLocationTime + 500;
            if (lastLocation != null && checkMove) {
                l(determiner, currentLocation);
            }
            lastLocation = currentLocation;
        }

        if (moving == allowedMoves) {
            checkMove = false;
            System.out.println("end moving");
            // end
        } else if (moving < allowedMoves) {
            if (!checkMove) {
                System.out.println("start moving");
            }
            checkMove = true;
            System.out.println("moving");
            addNextTickTime(300);
        }
    }

    public void l(Player determiner, Location currentLocation) {
        System.out.println(lastLocation.distance(currentLocation));
    }
}
