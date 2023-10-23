package de.corey.challenges.commands;

import de.corey.challenges.Main;
import de.corey.challenges.model.Challenge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand extends ACommand {

    public InfoCommand() {
        super("info");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length != 1) {
            commandSender.sendMessage("§cFalsche Eingabe");
            return false;
        }
        Main.getInstance().getChallenges().stream()
                .filter(challenge -> challenge.getName().equalsIgnoreCase(args[0]))
                .map(Challenge::info)
                .findAny().ifPresent(commandSender::sendMessage);
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1) {
            return Main.getInstance().getChallenges().stream()
                    .map(Challenge::getName)
                    .filter(name -> name.toLowerCase().contains(args[0].toLowerCase())).toList();
        }
        return new ArrayList<>();
    }
}
