package game;

/**
 * Runtime state for effects granted by Mortis's serum.
 *
 * The serum deliberately has no ordinary power-up slot.  Keeping its timer
 * and the one selected source power together prevents the temporary synergy
 * from being mistaken for a normal collectible power.
 */
public class BuffedEffects {

    int serumTimer;
    int selectedSerumPowerType = -1;

    public void clear() {
        serumTimer = 0;
        selectedSerumPowerType = -1;
    }

    public boolean isSerumActive() {
        return serumTimer > 0;
    }

    public boolean selects(int powerType) {
        return isSerumActive() && selectedSerumPowerType == powerType;
    }
}
