package de.corey.challenges.model.superhot;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Data
public class LastPlayerState {

    private final Player player;

    private Location location;

    private double startFallY;
}
