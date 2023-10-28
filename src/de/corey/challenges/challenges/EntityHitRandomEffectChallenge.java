package de.corey.challenges.challenges;

import de.corey.challenges.model.Challenge;
import de.corey.challenges.utils.Argument;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class EntityHitRandomEffectChallenge extends Challenge {

    private static final int MAX_RANDOM_DURATION = 60;

    @Argument
    private boolean randomDuration;
    @Argument
    private boolean durationStack;

    private final Random random;
    private final Map<EntityDamageEvent.DamageCause, PotionEffectType> specification = new HashMap<>();
    private final Map<EntityType, PotionEffectType> entitySpecification = new HashMap<>();
    private final Map<PotionEffectType, PotionEffectType> potionEffectSpecification = new HashMap<>();

    public EntityHitRandomEffectChallenge() {
        super("Random Effekt");
        random = new Random();
    }

    @Override
    public void start() {
        initSpecification();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        specification.clear();
    }

    public void initSpecification() {
        PotionEffectType[] potionEffectTypes = PotionEffectType.values();
        Arrays.stream(EntityDamageEvent.DamageCause.values())
                .filter(cause -> cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                .forEach(cause -> {
                    int rnd = random.nextInt(potionEffectTypes.length - 1);
                    specification.put(cause, potionEffectTypes[rnd]);
                });
        Arrays.stream(EntityType.values()).filter(EntityType::isAlive).forEach(entityType -> {
            int rnd = random.nextInt(potionEffectTypes.length - 1);
            entitySpecification.put(entityType, potionEffectTypes[rnd]);
        });
        Arrays.stream(PotionEffectType.values()).forEach(potionEffectType -> {
            int rnd = random.nextInt(potionEffectTypes.length - 1);
            potionEffectSpecification.put(potionEffectType, potionEffectTypes[rnd]);
        });
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (!specification.containsKey(event.getCause())) {
            return;
        }
        apply(event.getEntity(), specification.get(event.getCause()), event.getDamage());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Entity entity = event.getDamager();
        if (entity instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            entity = shooter;
        }
        if (!entitySpecification.containsKey(entity.getType())) {
            return;
        }
        apply(event.getEntity(), entitySpecification.get(entity.getType()), event.getDamage());
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getCause() == EntityPotionEffectEvent.Cause.PLUGIN) {
            return;
        }
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED && event.getAction() != EntityPotionEffectEvent.Action.CHANGED) {
            return;
        }
        PotionEffectType potionEffectType;
        if (event.getNewEffect() == null) {
            return;
        }
        potionEffectType = event.getNewEffect().getType();
        if (!potionEffectSpecification.containsKey(potionEffectType)) {
            return;
        }
        event.setCancelled(true);
        apply(event.getEntity(), potionEffectSpecification.get(potionEffectType), random.nextInt(10));
    }

    public int getDuration(double damage, PotionEffectType potionEffectType) {
        if (potionEffectType == PotionEffectType.HARM) {
            return 3;
        }
        if (randomDuration) {
            return (int) (Math.random() * MAX_RANDOM_DURATION * 20);
        } else {
            return (int) (damage * 6 * 20 + (Math.random() - .5) * 3 * 20);
        }
    }

    public void apply(Entity entity, PotionEffectType potionEffectType, double damage) {
        if (entity instanceof LivingEntity livingEntity) {
            int amplifier = (int) (Math.random() * 3) - 1;
            int duration = getDuration(damage, potionEffectType);
            if (livingEntity.hasPotionEffect(potionEffectType)) {
                PotionEffect effect = Objects.requireNonNull(livingEntity.getPotionEffect(potionEffectType));
                amplifier = Math.max((effect).getAmplifier(), amplifier);
                if (durationStack) {
                    duration += effect.getDuration();
                }
                livingEntity.removePotionEffect(potionEffectType);
            }
            livingEntity.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
        }
    }

    @Override
    public String info() {
        return "§cDamage Cause:\n" + specification.entrySet().stream()
                .map(entry -> "§e" + entry.getKey().name() + " §r-> §6" + entry.getValue().getName())
                .collect(Collectors.joining("\n")) + "\n§cEntity Damage:\n" +
                entitySpecification.entrySet().stream()
                        .map(entry -> "§e" + entry.getKey().name() + " §r-> §6" + entry.getValue().getName())
                        .collect(Collectors.joining("\n"));
    }
}
