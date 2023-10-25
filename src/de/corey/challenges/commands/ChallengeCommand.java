package de.corey.challenges.commands;

import de.corey.challenges.Main;
import de.corey.challenges.model.Challenge;
import de.corey.challenges.model.lists.StringList;
import de.corey.challenges.utils.Tuple;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ChallengeCommand extends ACommand {

    public ChallengeCommand() {
        super("challenge");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage("§cFalsche Eingabe");
            return false;
        }
        Challenge challenge = Main.getInstance().getSelectedChallenge();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("start")) {
                commandSender.sendMessage("§cFalsche Eingabe");
                return false;
            }
            if (challenge == null) {
                commandSender.sendMessage("§cEs läuft derzeit keine Challenge");
                return false;
            }
            if (args[0].equalsIgnoreCase("stop")) {
                challenge.stop();
            } else if (args[0].equalsIgnoreCase("pause")) {
                if (challenge.isPaused()) {
                    commandSender.sendMessage("§cDie Challenge ist bereits pausiert");
                    return false;
                }
                challenge.pause();
            } else if (args[0].equalsIgnoreCase("resume")) {
                if (!challenge.isPaused()) {
                    commandSender.sendMessage("§cDie Challenge läuft bereits");
                    return false;
                }
                challenge.resume();
            }
            return false;
        } else {
            if (args[0].equalsIgnoreCase("start")) {
                if (challenge != null) {
                    commandSender.sendMessage("§cEs läuft derzeit eine Challenge");
                    return false;
                }
                Optional<Challenge> optional = Main.getInstance().getChallenges().stream()
                        .filter(challengeListener -> args[1].toLowerCase().contains(challengeListener.getName().toLowerCase()))
                        .findFirst();
                if (optional.isEmpty()) {
                    commandSender.sendMessage("§cDiese Challenge existiert nicht");
                    return false;
                }
                String flags = String.join(" ", args).substring(optional.get().getName().length());
                optional.get().readArguments(flags.split(" "));
                optional.get().start();
                return false;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            if (Main.getInstance().getSelectedChallenge() == null) {
                list.add("start");
            } else {
                if (Main.getInstance().getSelectedChallenge().isPaused()) {
                    list.add("resume");
                } else {
                    list.add("pause");
                }
                list.add("stop");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("start")) {
                Main.getInstance().getChallenges().stream().map(Challenge::getName).forEach(list::add);
            }
        } else if (args.length > 2) { // Flags
            Optional<Challenge> optional = Main.getInstance().getChallenges().stream()
                    .filter(challengeListener -> args[1].equalsIgnoreCase(challengeListener.getName())).findAny();
            if (optional.isPresent()) {
                String lastArgument = args[args.length - 1];
                if (lastArgument.contains("=")) {
                    String[] aarg = lastArgument.split("=");
                    if (aarg.length == 1) {
                        aarg = new String[]{aarg[0], ""};
                    }
                    final String[] arg = aarg;
                    Tuple<Field, Object> tuple = optional.get().getArguments().get(arg[0]);
                    if (tuple == null) {
                        return list;
                    }
                    if (tuple.getT().getType().isEnum()) { // Enum
                        String currentValue = arg[1].toLowerCase();
                        Arrays.stream((tuple.getT().getType()).getDeclaredFields())
                                .map(Field::getName)
                                .filter(name -> !name.startsWith("$") && name.toLowerCase().contains(currentValue))
                                .map(name -> arg[0] + "=" + name)
                                .forEach(list::add);
                    } else if (tuple.getT().getType().getSuperclass() != null &&
                            tuple.getT().getType().getSuperclass().equals(StringList.class)) { // StringList
                        StringList<?> stringList = ((StringList<?>) tuple.getU());
                        String[] a = arg[1].split(",");
                        if (a.length > stringList.getLimit() || (arg[1].endsWith(",") && a.length == stringList.getLimit())) {
                            return list;
                        }
                        StringBuilder leftBuilder = new StringBuilder(arg[0] + "=");
                        for (int i = 0; i < a.length - 1; i++) {
                            leftBuilder.append(a[i]).append(",");
                        }
                        if (arg[1].endsWith(",") && a.length >= 1) {
                            leftBuilder.append(a[a.length - 1]).append(",");
                        }
                        String left = leftBuilder.toString();
                        String current = arg[1].endsWith(",") ? "" : a[a.length - 1];
                        List<String> cache = Arrays.asList(a);
                        stringList.specification()
                                .filter(element -> element.toLowerCase().contains(current.toLowerCase()) && !cache.contains(element))
                                .map(element -> left + element)
                                .forEach(list::add);
                    }
                } else {
                    optional.get().getArguments().entrySet().stream()
                            .filter(argument -> argument.getKey().toLowerCase().contains(lastArgument.toLowerCase()) &&
                                    Arrays.stream(args)
                                            .map(arg -> arg.split("=")[0])
                                            .noneMatch(name -> name.equalsIgnoreCase(argument.getKey())))
                            .map(argument -> argument.getKey() + (argument.getValue().getU() instanceof Boolean ? "" : "="))
                            .forEach(list::add);
                }
                return list;
            }
        }
        list = list.stream().filter(name -> name.toLowerCase().contains(args[args.length - 1].toLowerCase())).toList();

        return list;
    }
}
