package de.corey.challenges;

import com.google.common.collect.Sets;
import de.corey.challenges.challenges.SuperHotChallenge;
import de.corey.challenges.commands.ChallengeCommand;
import de.corey.challenges.commands.InfoCommand;
import de.corey.challenges.challenges.EntityHitRandomEffectChallenge;
import de.corey.challenges.model.Challenge;
import de.corey.challenges.challenges.DamageHealthChallenge;
import de.corey.challenges.listeners.EnderDragonEndListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

@Getter
public class Main extends JavaPlugin {

    @Getter
    private static Main instance;

    private Set<Challenge> challenges;

    @Setter
    private Challenge selectedChallenge = null;

    @Override
    public void onEnable() {
        instance = this;

        register();
    }

    @Override
    public void onDisable() {
        selectedChallenge = null;
        challenges.forEach(Challenge::onDisable);
    }

    public void register() {
        getServer().getPluginManager().registerEvents(new EnderDragonEndListener(), this);

        challenges = allChallenges();
        challenges.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        new ChallengeCommand();
        new InfoCommand();
    }

    public static Set<Challenge> allChallenges() {
        return Sets.newHashSet(new DamageHealthChallenge(), new EntityHitRandomEffectChallenge(), new SuperHotChallenge());
    }

    public static void main(String[] args) {
        Main.allChallenges().stream()
                .map(Challenge::infoNoColor)
                .forEach(System.out::println);
    }
}
