package de.corey.challenges.model.lists;

import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

@NoArgsConstructor
public class PlayerStringList extends StringList<Player> {

    public PlayerStringList(int limit) {
        super(limit);
    }

    @Override
    public Stream<String> specification() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName);
    }

    @Override
    public Stream<Player> tStream() {
        return stream().map(Bukkit::getPlayer).filter(Bukkit.getOnlinePlayers()::contains);
    }
}
