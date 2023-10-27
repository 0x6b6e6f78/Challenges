package de.corey.challenges.model;

import de.corey.challenges.Main;
import de.corey.challenges.utils.Argument;
import de.corey.challenges.model.lists.StringList;
import de.corey.challenges.utils.Timer;
import de.corey.challenges.utils.Tuple;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.*;

public abstract class Challenge implements Listener {

    @Getter
    private String name;

    @Getter
    private boolean enderDragonEnd;

    protected Timer timer;

    @Getter
    private Map<String, Tuple<Field, Object>> arguments;

    @Getter
    private String displayName;

    public Challenge(String displayName) {
        this(displayName.replaceAll("\\W+", ""), displayName, true);
    }

    public Challenge(String name, String displayName) {
        this(name, displayName, true);
    }

    public Challenge(String name, String displayName, boolean enderDragonEnd) {
        this.name = name;
        this.displayName = displayName;
        this.enderDragonEnd = enderDragonEnd;

        new Thread(() -> {
            try {
                Thread.sleep(50);
                saveArgumentDefaults();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void readArguments(String[] args) {
        resetArguments();
        readInputArguments(args);
    }

    public void start() {
        if (timer == null) {
            timer = new Timer();
        }
        Main.getInstance().setSelectedChallenge(this);
        timer.start();
        Bukkit.broadcastMessage("§7Die §e" + displayName + " Challenge §7beginnt!");
    }

    private void saveArgumentDefaults() throws IllegalAccessException {
        arguments = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Argument.class)) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(this);
            String label = field.getAnnotation(Argument.class).label();
            if (label.equals("")) {
                label = field.getName();
            }
            arguments.put(label, new Tuple<>(field, value));
        }
    }

    private void resetArguments() {
        try {
            for (Tuple<Field, Object> tuple : arguments.values()) {
                tuple.getT().set(this, tuple.getU());
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void readInputArguments(String[] args) {
        try {
            for (String argument : args) {
                if (!arguments.containsKey(argument.split("=")[0])) {
                    continue;
                }
                String[] arg = argument.split("=");
                Optional<Object> optional = convertFromArgument(arg);
                if (optional.isPresent()) {
                    arguments.get(arg[0]).getT().set(this, optional.get());
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Object> convertFromArgument(String[] arg) {
        Tuple<Field, Object> tuple = arguments.get(arg[0]);
        Object value = tuple.getU();

        if (value instanceof Boolean) {
            return Optional.of(true);
        }
        if (arg.length == 2) {
            try {
                if (value instanceof Float) {
                    return Optional.of(Float.parseFloat(arg[1]));
                } else if (value instanceof Double) {
                    return Optional.of(Double.parseDouble(arg[1]));
                } else if (value instanceof Integer) {
                    return Optional.of(Integer.parseInt(arg[1]));
                } else if (value instanceof String) {
                    return Optional.of(arg[1]);
                }
                Class<?> clazz = tuple.getT().getType();
                if (clazz.isEnum()) {
                    return Optional.of(Enum.valueOf((Class<? extends Enum>) clazz, arg[1]));
                }
                if (clazz.getSuperclass().equals(StringList.class)) {
                    StringList<?> stringList = (StringList<?>) tuple.getU();
                    String[] names = arg[1].split(",");
                    stringList.addAll(Arrays.asList(names));
                    return Optional.of(stringList);
                }
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public void pause() {
        timer.pause();
    }

    public void resume() {
        timer.resume();
    }

    public void stop() {
        timer.stop();
        Bukkit.broadcastMessage(
                "§6§m-------------------" +
                "\n§r " +
                "\n§7Die §e" + displayName + " Challenge §7wurde beendet!" +
                "\n§r " +
                "\n§7Zeit: §e" + timer.text() +
                "\n§r " +
                "\n§6§m-------------------"
        );
        Main.getInstance().setSelectedChallenge(null);
    }

    public String info() {
        return "§6" + displayName + " Challenge:\n";
    }

    public String infoNoColor() {
        return info().replaceAll("§.", "");
    }

    public boolean isActive() {
        return isSelected() && !isPaused();
    }

    public boolean isSelected() {
        return Main.getInstance().getSelectedChallenge() == this;
    }

    public boolean isPaused() {
        return timer.isPaused();
    }
}
