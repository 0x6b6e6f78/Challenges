package de.corey.challenges.model.superhot;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MovingState {
    STAYING(1, 0),
    SNEAKING(10, 1),
    WALKING(20, 3),
    SWIMMING(50, 10),
    JUMPING(60, 15),
    SPRINTING(100, 30),
    FALLING(SPRINTING.tps, SPRINTING.rts);
    public final int tps;
    public final int rts;

    public MovingState getHighest(MovingState movingState) {
        return tps > movingState.tps ? this : movingState;
    }

    public double getMillisPerTick() {
        return 1000d / tps;
    }
}
