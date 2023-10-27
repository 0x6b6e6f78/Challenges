package de.corey.challenges.model.superhot;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MovingState {
    STAYING(1), SNEAKING(10), WALKING(20), SPRINTING(100), FALLING(SPRINTING.tps);
    public final int tps;
}