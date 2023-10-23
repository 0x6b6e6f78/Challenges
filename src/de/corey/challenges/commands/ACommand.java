package de.corey.challenges.commands;

import de.corey.challenges.Main;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

@Getter
public abstract class ACommand implements TabCompleter, CommandExecutor {

    private final String name;

    public ACommand(String name) {
        this.name = name;

        PluginCommand command = Main.getInstance().getCommand(name);
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }
}
